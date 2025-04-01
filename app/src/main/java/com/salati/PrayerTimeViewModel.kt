package com.salati

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.batoulapps.adhan.*
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.data.DateComponents
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class PrayerTimeViewModel : ViewModel() {

    private val _prayerTimes = MutableLiveData<List<PrayerTime>>()
    val prayerTimes: LiveData<List<PrayerTime>> = _prayerTimes

    private val _currentPrayer = MutableLiveData<PrayerTime?>()
    val currentPrayer: LiveData<PrayerTime?> = _currentPrayer

    private val _timeUntilNextPrayer = MutableLiveData<String>()
    val timeUntilNextPrayer: LiveData<String> = _timeUntilNextPrayer

    private val _nextPrayerName = MutableLiveData<String>()
    val nextPrayerName: LiveData<String> = _nextPrayerName

    private var countdownRunnable: Runnable? = null
    private val handler = Handler(Looper.getMainLooper())

    fun calculatePrayerTimes(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            val coordinates = Coordinates(latitude, longitude)
            val dateComponents = DateComponents.from(Date())
            val params = CalculationMethod.MUSLIM_WORLD_LEAGUE.parameters

            val prayerTimes = PrayerTimes(
                coordinates,
                dateComponents,
                params
            )

            val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            formatter.timeZone = TimeZone.getDefault()

            val prayers = listOf(
                PrayerTime("Fajr", formatter.format(prayerTimes.fajr), prayerTimes.fajr.time),
                PrayerTime(
                    "Sunrise",
                    formatter.format(prayerTimes.sunrise),
                    prayerTimes.sunrise.time
                ),
                PrayerTime("Dhuhr", formatter.format(prayerTimes.dhuhr), prayerTimes.dhuhr.time),
                PrayerTime("Asr", formatter.format(prayerTimes.asr), prayerTimes.asr.time),
                PrayerTime(
                    "Maghrib",
                    formatter.format(prayerTimes.maghrib),
                    prayerTimes.maghrib.time
                ),
                PrayerTime("Isha", formatter.format(prayerTimes.isha), prayerTimes.isha.time)
            )

            _prayerTimes.postValue(prayers)
            updateCurrentPrayer(prayers)
        }
    }

    private fun updateCurrentPrayer(prayers: List<PrayerTime>) {
        val currentTime = System.currentTimeMillis()
        val currentPrayer = prayers.findLast { it.timestamp <= currentTime }
        _currentPrayer.postValue(currentPrayer)

        val nextPrayer = prayers.find { it.timestamp > currentTime } ?: prayers.firstOrNull()

        nextPrayer?.let {
            val nextPrayerTimestamp = if (it.name == "Fajr" && currentPrayer?.name == "Isha") {
                // For Fajr after Isha, add a day to the timestamp
                it.timestamp + (24 * 60 * 60 * 1000)
            } else {
                it.timestamp
            }

            _nextPrayerName.postValue(it.name)
            startCountdown(nextPrayerTimestamp)
        }
    }

    private fun startCountdown(nextPrayerTimestamp: Long) {
        countdownRunnable?.let { handler.removeCallbacks(it) }

        countdownRunnable = object : Runnable {
            override fun run() {
                val currentTime = System.currentTimeMillis()
                val remainingTime = nextPrayerTimestamp - currentTime

                if (remainingTime > 0) {
                    val hours = (remainingTime / (1000 * 60 * 60)) % 24
                    val minutes = (remainingTime / (1000 * 60)) % 60
                    val seconds = (remainingTime / 1000) % 60

                    _timeUntilNextPrayer.postValue(
                        String.format(
                            "%02d:%02d:%02d",
                            hours,
                            minutes,
                            seconds
                        )
                    )
                    handler.postDelayed(this, 1000)
                } else {
                    _timeUntilNextPrayer.postValue("00:00:00")
                    handler.removeCallbacks(this)  // Stop the countdown
                    updateCurrentPrayer(_prayerTimes.value ?: listOf()) // Trigger the next prayer
                }
            }
        }

        handler.post(countdownRunnable!!)
    }
}
