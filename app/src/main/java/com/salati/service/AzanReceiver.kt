package com.salati.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.salati.R

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
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                notify(prayerName.hashCode(), notification)
            } else {
                // Show a message to the user
                Toast.makeText(context, "Notification permission is not granted. Please enable it in settings.", Toast.LENGTH_LONG).show()

                // Optionally, open the app's settings page
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
        }

        val mediaPlayer = MediaPlayer.create(context, R.raw.azan_all)
        mediaPlayer.start()
    }
}