package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.ui.AdhanAlertActivity
import com.example.util.AzanAudioHelper
import com.example.util.PrayerNotificationScheduler
import com.example.widget.WidgetUpdater

class PrayerNotificationReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_PRAYER_ALERT = "com.example.ACTION_PRAYER_ALERT"
        const val ACTION_SNOOZE_PRAYER_ALERT = "com.example.ACTION_SNOOZE_PRAYER_ALERT"
        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        const val EXTRA_CITY_NAME = "extra_city_name"
        const val CHANNEL_ID = "prayer_alerts_channel_v2"
        const val CHANNEL_NAME = "Prayer Times (Salah)"
        const val SNOOZE_MINUTES = 10
    }

    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: "Salah"
        val cityName = intent.getStringExtra(EXTRA_CITY_NAME) ?: "Makkah"

        if (intent.action == ACTION_SNOOZE_PRAYER_ALERT) {
            PrayerNotificationScheduler.scheduleSnooze(context, prayerName, cityName, SNOOZE_MINUTES)
            return
        }

        // 1. Create Notification Channel
        createNotificationChannel(context)

        // 2. Play the Azan recitation audio (also started inside the full-screen activity;
        //    this covers the brief window before that activity's window attaches)
        try {
            val azanHelper = AzanAudioHelper(context)
            val soundPref = com.example.data.SettingsManager(context).alertSound.value
            azanHelper.playAzan(prayerName, soundPref)
        } catch (_: Exception) {}

        // 3. Launch the full-screen Adhan alert, the way an incoming call or alarm
        //    clock takes over the screen, so the prayer time can't be missed.
        val fullScreenIntent = Intent(context, AdhanAlertActivity::class.java).apply {
            putExtra(AdhanAlertActivity.EXTRA_PRAYER_NAME, prayerName)
            putExtra(AdhanAlertActivity.EXTRA_CITY_NAME, cityName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            prayerName.hashCode(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 4. Heads-up notification as a fallback/history entry (also used to launch the
        //    full-screen alert on API levels or battery states that suppress it automatically)
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            prayerName.hashCode() + 1,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Al-Huda • Time for $prayerName Prayer (حان وقت الصلاة)")
            .setContentText("It is now time for the $prayerName prayer in $cityName.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        "It is now time for the $prayerName prayer in $cityName.\n" +
                                "«إِنَّ الصَّلَاةَ كَانَتْ عَلَى الْمُؤْمِنِينَ كِتَابًا مَّوْقُوتًا»\n" +
                                "\"Indeed, prayer has been decreed upon the believers a decree of specified times.\" — Quran 4:103"
                    )
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setOngoing(false)
            .setContentIntent(contentPendingIntent)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setSound(Uri.parse("android.resource://${context.packageName}/${R.raw.prayer_alert}"))
            .setVibrate(longArrayOf(0, 500, 200, 500, 200, 1000))
            .build()

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(prayerName.hashCode(), notification)
        } catch (_: SecurityException) {
            // Handled if POST_NOTIFICATIONS is not yet granted
        }

        // Also try to launch the full-screen alert directly: on many OEM skins and when the
        // screen is already on, the system does not auto-launch fullScreenIntent.
        try {
            context.startActivity(fullScreenIntent)
        } catch (_: Exception) {}

        // 5. Refresh the "Next Prayer" home-screen widget to reflect the new current prayer
        try { WidgetUpdater.updateAll(context) } catch (_: Exception) {}

        // 6. Prayer Accountability Lock: if enabled, block the screen until proof is captured
        try {
            val settingsManager = com.example.data.SettingsManager(context)
            if (settingsManager.strictLockEnabled.value && !settingsManager.isPrayerProven(prayerName)) {
                com.example.service.PrayerLockService.start(context, prayerName)
            }
        } catch (_: Exception) {}
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts and Azan notifications for the 5 daily prayer times"
                setSound(
                    Uri.parse("android.resource://${context.packageName}/${R.raw.prayer_alert}"),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 1000)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
