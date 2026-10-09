package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.util.AzanAudioHelper

class PrayerNotificationReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_PRAYER_ALERT = "com.example.ACTION_PRAYER_ALERT"
        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        const val EXTRA_CITY_NAME = "extra_city_name"
        const val CHANNEL_ID = "prayer_alerts_channel"
        const val CHANNEL_NAME = "Prayer Times (Salah)"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: "Salah"
        val cityName = intent.getStringExtra(EXTRA_CITY_NAME) ?: "Makkah"

        // 1. Create Notification Channel
        createNotificationChannel(context)

        // 2. Play the Azan recitation audio
        try {
            val azanHelper = AzanAudioHelper(context)
            azanHelper.playAzan(prayerName)
        } catch (_: Exception) {}

        // 3. Build Notification
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            prayerName.hashCode(),
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
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 200, 500, 200, 1000))
            .build()

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(prayerName.hashCode(), notification)
        } catch (_: SecurityException) {
            // Handled if POST_NOTIFICATIONS is not yet granted
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts and Azan notifications for the 5 daily prayer times"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 1000)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
