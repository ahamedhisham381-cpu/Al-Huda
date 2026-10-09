package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.model.PrayerTimes
import com.example.receiver.PrayerNotificationReceiver
import java.util.Calendar

object PrayerNotificationScheduler {

    private val prayerKeys = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")

    /**
     * Schedules exact alarms for all 5 daily prayers using AlarmManager.
     */
    fun scheduleAllPrayerAlerts(
        context: Context,
        prayerTimes: PrayerTimes?,
        cityName: String,
        enabled: Boolean
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        // If disabled, cancel all pending prayer alarms
        if (!enabled || prayerTimes == null) {
            cancelAllAlarms(context, alarmManager)
            return
        }

        val prayerMap = mapOf(
            "Fajr" to prayerTimes.fajr,
            "Dhuhr" to prayerTimes.dhuhr,
            "Asr" to prayerTimes.asr,
            "Maghrib" to prayerTimes.maghrib,
            "Isha" to prayerTimes.isha
        )

        val now = Calendar.getInstance()

        prayerMap.forEach { (name, timeStr) ->
            val parts = timeStr.split(":")
            if (parts.size >= 2) {
                val hour = parts[0].toIntOrNull() ?: return@forEach
                val minute = parts[1].toIntOrNull() ?: return@forEach

                val alarmCal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                // If this prayer time has already passed today, schedule for tomorrow
                if (alarmCal.before(now)) {
                    alarmCal.add(Calendar.DAY_OF_YEAR, 1)
                }

                val intent = Intent(context, PrayerNotificationReceiver::class.java).apply {
                    action = PrayerNotificationReceiver.ACTION_PRAYER_ALERT
                    putExtra(PrayerNotificationReceiver.EXTRA_PRAYER_NAME, name)
                    putExtra(PrayerNotificationReceiver.EXTRA_CITY_NAME, cityName)
                }

                val requestCode = name.hashCode()
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            alarmCal.timeInMillis,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            alarmCal.timeInMillis,
                            pendingIntent
                        )
                    }
                } catch (_: SecurityException) {
                    // Fallback to inexact alarm if exact alarm permission restricted
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        alarmCal.timeInMillis,
                        pendingIntent
                    )
                }
            }
        }
    }

    fun cancelAllAlarms(context: Context, alarmManager: AlarmManager) {
        prayerKeys.forEach { name ->
            val intent = Intent(context, PrayerNotificationReceiver::class.java).apply {
                action = PrayerNotificationReceiver.ACTION_PRAYER_ALERT
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                name.hashCode(),
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }

    /**
     * Triggers an immediate test prayer alert notification with Athan recitation.
     */
    fun triggerImmediateTestAlert(context: Context, prayerName: String, cityName: String) {
        val intent = Intent(context, PrayerNotificationReceiver::class.java).apply {
            action = PrayerNotificationReceiver.ACTION_PRAYER_ALERT
            putExtra(PrayerNotificationReceiver.EXTRA_PRAYER_NAME, prayerName)
            putExtra(PrayerNotificationReceiver.EXTRA_CITY_NAME, cityName)
        }
        context.sendBroadcast(intent)
    }
}
