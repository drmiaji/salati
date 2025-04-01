package com.salati

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val viewModel: PrayerTimeViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var prayerAlarmScheduler: PrayerAlarmScheduler
    private lateinit var prayerNotificationService: PrayerNotificationService

    private val PERMISSION_REQUEST_CODE = 1

    private lateinit var currentPrayerText: TextView
    private lateinit var currentPrayerTime: TextView
    private lateinit var prayerTimesRecyclerView: RecyclerView
    private lateinit var prayerTimeAdapter: PrayerTimeAdapter
    private lateinit var timeUntilNextPrayerText: TextView
    private lateinit var nextPrayerNameTextView: TextView

    private var countdownTime: Long = 0 // To hold the remaining time in milliseconds

    private var mediaPlayer: MediaPlayer? = null // MediaPlayer instance
    private var isAdhanPlayed = false // To track if Adhan has already been played
    private var lastPrayerPlayed: String? = null // Track last played prayer name

    companion object {
        const val LOCATION_PERMISSION_REQUEST = 1001
        const val NOTIFICATION_PERMISSION_REQUEST = 1002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI components
        initializeUI()

        // Initialize Fused Location and Alarm Scheduler
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        prayerAlarmScheduler = PrayerAlarmScheduler(this)
        prayerNotificationService = PrayerNotificationService() // Initialize the notification service

        // Setup Observers
        setupObservers()

        // Check Permissions
        requestLocationPermissionIfNeeded()
        requestNotificationPermissionIfNeeded()

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_prayer_time -> {
                    // Stay on current activity (Prayer Time activity)
                    return@setOnItemSelectedListener true
                }

                R.id.nav_qibla -> {
                    // Navigate to Qibla activity
                    val intent = Intent(this, CompassActivity::class.java)
                    startActivity(intent)
                    return@setOnItemSelectedListener true
                }

                else -> false
            }
        }
    }

    private fun initializeUI() {
        currentPrayerText = findViewById(R.id.currentPrayerText)
        currentPrayerTime = findViewById(R.id.currentPrayerTime)
        timeUntilNextPrayerText = findViewById(R.id.timeUntilNextPrayer)
        nextPrayerNameTextView = findViewById(R.id.nextPrayerName)
        prayerTimesRecyclerView = findViewById<RecyclerView>(R.id.prayerTimesRecyclerView).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            prayerTimeAdapter = PrayerTimeAdapter(emptyList(), this@MainActivity) // Pass empty list initially
            adapter = prayerTimeAdapter
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupObservers() {
        viewModel.prayerTimes.observe(this) { prayers ->
            updatePrayerTimesUI(prayers)
        }

        viewModel.currentPrayer.observe(this) { prayer ->
            updateCurrentPrayerUI(prayer)
        }

        viewModel.timeUntilNextPrayer.observe(this) { remainingTime ->
            if (remainingTime > 0.toString()) {
                countdownTime = convertTimeToMilliseconds(remainingTime) // Convert to milliseconds
                startCountdown()

                // Get current prayer and check if Adhan has already been played for this prayer
                val currentPrayer = viewModel.currentPrayer.value
                currentPrayer?.let {
                    // Notify for the current prayer
                    prayerNotificationService.showPrayerNotification(
                        this,
                        it.name,
                        it.time
                    )
                }
            } else {
                timeUntilNextPrayerText.text = "00:00:00"
            }
        }

        viewModel.nextPrayerName.observe(this) { nextPrayerName ->
            nextPrayerNameTextView.text = "Next Prayer: $nextPrayerName"
        }
    }

    private fun updatePrayerTimesUI(prayers: List<PrayerTime>) {
        val updatedPrayers = prayers.map { prayer ->
            if (prayer.name == "Maghrib") {
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                val date = sdf.parse(prayer.time) ?: Date() // Use current date if parsing fails
                val calendar = Calendar.getInstance().apply {
                    time = date
                    add(Calendar.MINUTE, 4)
                }
                prayer.copy(time = sdf.format(calendar.time))
            } else {
                prayer
            }
        }
        prayerTimeAdapter.updatePrayers(updatedPrayers)
        updatedPrayers.forEach { prayer ->
            prayerAlarmScheduler.schedulePrayerAlarm(prayer)
        }
    }

    private fun updateCurrentPrayerUI(prayer: PrayerTime?) {
        prayer?.let {
            currentPrayerText.text = it.name
            currentPrayerTime.text = it.time
        } ?: run {
            currentPrayerText.text = getString(R.string.no_prayer_time_available)
            currentPrayerTime.text = getString(R.string.default_time)
        }
    }

    private fun requestLocationPermissionIfNeeded() {
        if (!hasLocationPermission()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
        } else {
            // Permission is already granted, proceed with your logic
            fetchCurrentLocation()
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasNotificationPermission()) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST
                )
            }
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun hasNotificationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun fetchCurrentLocation() {
        if (hasLocationPermission()) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        viewModel.calculatePrayerTimes(it.latitude, it.longitude)
                    } ?: showToast("Unable to get location")
                }
                .addOnFailureListener { e ->
                    showToast("Error getting location: ${e.message}")
                }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, proceed with your logic
                    fetchCurrentLocation()
                } else {
                    // Permission denied, handle the case
                    showToast("Location permission is required for prayer time calculation")
                }
            }

            NOTIFICATION_PERMISSION_REQUEST -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    // Permission denied, handle the case
                    showToast("Notification permission is required for prayer alerts")
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun convertTimeToMilliseconds(time: String): Long {
        val timeParts = time.split(":")
        val hours = timeParts[0].toLongOrNull() ?: 0
        val minutes = timeParts[1].toLongOrNull() ?: 0
        val seconds = timeParts[2].toLongOrNull() ?: 0
        return (hours * 3600 + minutes * 60 + seconds) * 1000
    }

    private fun startCountdown() {
        val countdownTimer = object : CountDownTimer(countdownTime, 1000) {
            @SuppressLint("DefaultLocale")
            override fun onTick(millisUntilFinished: Long) {
                val hours = (millisUntilFinished / 3600000).toInt()
                val minutes = (millisUntilFinished % 3600000 / 60000).toInt()
                val seconds = (millisUntilFinished % 60000 / 1000).toInt()

                timeUntilNextPrayerText.text =
                    String.format("%02d:%02d:%02d", hours, minutes, seconds)
            }

            @SuppressLint("SetTextI18n")
            override fun onFinish() {
                timeUntilNextPrayerText.text = "00:00:00"
            }
        }
        countdownTimer.start()
    }

    // Play the adhan sound for the prayer

    // Helper function to check if the current time is the prayer time
    private fun isTimeForPrayer(prayerTime: String): Boolean {
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        return currentTime == prayerTime
    }
}