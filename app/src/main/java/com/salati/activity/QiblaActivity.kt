package com.salati.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.salati.activity.MainActivity
import com.salati.R
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class QiblaActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var qiblaDirectionText: TextView
    private lateinit var qiblaCompass: ImageView

    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0

    private val qiblaLatitude = 21.4225  // Latitude of the Kaaba (Qibla)
    private val qiblaLongitude = 39.8262 // Longitude of the Kaaba (Qibla)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qibla)

        // Initialize the BottomNavigationView
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNav)

        // Set Qibla as the selected item by default when the activity is created
        bottomNavigationView.selectedItemId = R.id.nav_qibla

        // Set a listener for item selection
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_qibla -> {
                    val intent = Intent(this, CompassActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }

                R.id.nav_prayer_time -> {
                    // Switch to MainActivity when the Prayer Time item is clicked
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish() // Close current activity if desired
                    true
                }

                else -> false
            }
        }

        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        qiblaDirectionText = findViewById(R.id.qiblaDirectionText)
        qiblaCompass = findViewById(R.id.qiblaCompass)

        // Request location permission if not granted
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        } else {
            getCurrentLocation()
        }
    }

    private fun getCurrentLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    currentLatitude = it.latitude
                    currentLongitude = it.longitude
                    calculateQiblaDirection()
                }
            }
    }

    private fun calculateQiblaDirection() {
        // Calculate the bearing from the current location to the Qibla
        val lat1 = Math.toRadians(currentLatitude)
        val lon1 = Math.toRadians(currentLongitude)
        val lat2 = Math.toRadians(qiblaLatitude)
        val lon2 = Math.toRadians(qiblaLongitude)

        val deltaLon = lon2 - lon1

        val y = sin(deltaLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(deltaLon)
        val bearing = Math.toDegrees(atan2(y, x))

        val normalizedBearing = (bearing + 360) % 360

        // Update the UI with the bearing
        qiblaDirectionText.text = getString(R.string.qibla_direction, normalizedBearing)

        // Rotate the compass
        val animation = RotateAnimation(
            qiblaCompass.rotation, -normalizedBearing.toFloat(),
            RotateAnimation.RELATIVE_TO_SELF, 0.5f,
            RotateAnimation.RELATIVE_TO_SELF, 0.5f
        )
        animation.duration = 500
        animation.fillAfter = true
        qiblaCompass.startAnimation(animation)
    }
}