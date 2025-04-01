package com.salati

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import androidx.core.app.NotificationCompat

class PrayerNotificationService {

    private var isNotificationShown = false
    private var mediaPlayer: MediaPlayer? = null

    fun showPrayerNotification(context: Context, prayerName: String, prayerTime: String) {
        // Skip showing notification or playing Adhan if the time has passed or Adhan has been played
        if (isNotificationShown || !isTimeForPrayer(prayerTime)) {
            return
        }

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "prayer_channel",
                "Prayer Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Cancel any existing notifications with the same ID before showing a new one
        notificationManager.cancel(1)

        // Create the notification
        val notification = NotificationCompat.Builder(context, "prayer_channel")
            .setContentTitle("Time for $prayerName")
            .setContentText("The time for $prayerName is now: $prayerTime")
            .setSmallIcon(R.drawable.notification) // Ensure this icon exists
            .setAutoCancel(true) // Notification will be removed when clicked
            .build()

        // Show the notification
        notificationManager.notify(1, notification)

        // Set the flag to true after showing the notification
        isNotificationShown = true

        // Play the Adhan (Azaan) based on the prayer
        playAdhan(context, prayerName)
    }

    private fun playAdhan(context: Context, prayerName: String) {
        // Skip playing Adhan for Sunrise (Shuruq) as there is no Adhan for it
        if (prayerName.equals("Sunrise", true)) {
            return
        }

        // Stop any previous audio if it's still playing
        mediaPlayer?.stop()
        mediaPlayer?.reset()

        // Choose the appropriate Adhan based on the prayer
        val adhanResource = if (prayerName.equals("Fajr", true)) {
            R.raw.azan_fajr // Fajr
        } else {
            R.raw.azan_all // Other prayers
        }

        // Initialize MediaPlayer and start playing the selected Adhan
        mediaPlayer = MediaPlayer.create(context, adhanResource)
        mediaPlayer?.start()
    }

    // Helper function to check if it's time for the prayer
    private fun isTimeForPrayer(prayerTime: String): Boolean {
        val currentTime = getCurrentTime()
        return prayerTime == currentTime
    }

    // Get current time formatted as HH:mm (in 24-hour format)
    private fun getCurrentTime(): String {
        val currentTime = System.currentTimeMillis()
        val dateFormat = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        return dateFormat.format(currentTime)
    }

    // Call this method when you want to reset the notification flag (e.g., when prayer time changes)
    fun resetNotificationFlag() {
        isNotificationShown = false
    }
}
