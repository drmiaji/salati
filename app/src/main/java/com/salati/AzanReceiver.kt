package com.salati

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class AzanReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra("prayer_name") ?: return

        val notification = NotificationCompat.Builder(context, "azan_channel")
            .setSmallIcon(R.drawable.ic_azan)
            .setContentTitle("Prayer Time")
            .setContentText("It's time for $prayerName prayer")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        with(NotificationManagerCompat.from(context)) {
            notify(prayerName.hashCode(), notification)
        }

        val mediaPlayer = MediaPlayer.create(context, R.raw.azan_all)
        mediaPlayer.start()
    }
}