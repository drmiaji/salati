package com.salati

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.*

class PrayerAlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedulePrayerAlarm(prayerTime: PrayerTime) {
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            putExtra("PRAYER_NAME", prayerTime.name)
            putExtra("PRAYER_TIME", prayerTime.time)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            prayerTime.name.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Create a Calendar instance for the prayer time
        val calendar = Calendar.getInstance()
        val timeComponents = prayerTime.time.split(":")
        calendar.set(Calendar.HOUR_OF_DAY, timeComponents[0].toInt())
        calendar.set(Calendar.MINUTE, timeComponents[1].toInt())
        calendar.set(Calendar.SECOND, 0)

        // If the prayer time has already passed for today, set it for the next day
        if (calendar.timeInMillis < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        // Scheduling alarm based on API level
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // For Android 6.0 (API 23) and above, use setExactAndAllowWhileIdle
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // For Android 4.4 (API 19) and above, use setExact
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            // For devices below Android 4.4 (API 19), use set
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }
}
