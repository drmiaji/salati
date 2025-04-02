package com.salati.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.salati.service.PrayerNotificationService

class PrayerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra("PRAYER_NAME") ?: return
        val prayerTime = intent.getStringExtra("PRAYER_TIME") ?: return

        // Start the notification service
        val serviceIntent = Intent(context, PrayerNotificationService::class.java)
        context.startService(serviceIntent)

        // Show notification
        val notificationService = PrayerNotificationService()
        // Assuming you have access to the context
        notificationService.showPrayerNotification(context, prayerName, prayerTime)

    }
}