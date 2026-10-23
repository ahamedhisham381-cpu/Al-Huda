package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.SettingsManager
import com.example.data.PrayerRepository
import com.example.model.PrayerTimes
import com.example.util.Translations

/**
 * Home-screen "Next Prayer" widget. Rendered with classic RemoteViews (no extra
 * dependency needed) and refreshed whenever prayer times are (re)loaded, and at
 * every prayer-time transition via [WidgetUpdater].
 */
class NextPrayerWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { id -> WidgetUpdater.renderInto(context, appWidgetManager, id) }
    }

    override fun onEnabled(context: Context) {
        WidgetUpdater.updateAll(context)
    }
}

object WidgetUpdater {
    private const val PREFS = "widget_prayer_cache"

    /** Called by the app whenever it has fresh prayer times, so the widget stays accurate. */
    fun saveLatest(context: Context, times: PrayerTimes, nextPrayerKey: String, cityName: String, hijri: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString("fajr", times.fajr)
            .putString("dhuhr", times.dhuhr)
            .putString("asr", times.asr)
            .putString("maghrib", times.maghrib)
            .putString("isha", times.isha)
            .putString("next", nextPrayerKey)
            .putString("city", cityName)
            .putString("hijri", hijri)
            .apply()
        updateAll(context)
    }

    fun updateAll(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(
            android.content.ComponentName(context, NextPrayerWidgetProvider::class.java)
        )
        ids.forEach { id -> renderInto(context, manager, id) }
    }

    fun renderInto(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val settingsManager = SettingsManager(context)
        val lang = settingsManager.lang.value
        val t = Translations.get(lang)

        val views = RemoteViews(context.packageName, R.layout.widget_next_prayer)

        val fajr = prefs.getString("fajr", "--:--") ?: "--:--"
        val dhuhr = prefs.getString("dhuhr", "--:--") ?: "--:--"
        val asr = prefs.getString("asr", "--:--") ?: "--:--"
        val maghrib = prefs.getString("maghrib", "--:--") ?: "--:--"
        val isha = prefs.getString("isha", "--:--") ?: "--:--"
        val nextKey = prefs.getString("next", "Fajr") ?: "Fajr"
        val city = prefs.getString("city", "") ?: ""
        val hijri = prefs.getString("hijri", "") ?: ""

        views.setTextViewText(R.id.widget_city, city.ifBlank { context.getString(R.string.app_name) })
        views.setTextViewText(R.id.widget_hijri, hijri)

        val nextLabel = when (nextKey) {
            "Fajr" -> t.fajr
            "Dhuhr" -> t.dhuhr
            "Asr" -> t.asr
            "Maghrib" -> t.maghrib
            "Isha" -> t.isha
            else -> nextKey
        }
        val nextTime = when (nextKey) {
            "Fajr" -> fajr
            "Dhuhr" -> dhuhr
            "Asr" -> asr
            "Maghrib" -> maghrib
            "Isha" -> isha
            else -> "--:--"
        }
        val isAr = lang == "ar"
        views.setTextViewText(R.id.widget_next_prayer_name, (if (isAr) "التالية: " else "Next: ") + nextLabel)
        views.setTextViewText(R.id.widget_next_prayer_time, nextTime)

        views.setTextViewText(R.id.widget_label_fajr, t.fajr)
        views.setTextViewText(R.id.widget_label_dhuhr, t.dhuhr)
        views.setTextViewText(R.id.widget_label_asr, t.asr)
        views.setTextViewText(R.id.widget_label_maghrib, t.maghrib)
        views.setTextViewText(R.id.widget_label_isha, t.isha)
        views.setTextViewText(R.id.widget_time_fajr, fajr)
        views.setTextViewText(R.id.widget_time_dhuhr, dhuhr)
        views.setTextViewText(R.id.widget_time_asr, asr)
        views.setTextViewText(R.id.widget_time_maghrib, maghrib)
        views.setTextViewText(R.id.widget_time_isha, isha)

        // Highlight whichever slot is next
        val slotIds = mapOf(
            "Fajr" to R.id.widget_slot_fajr,
            "Dhuhr" to R.id.widget_slot_dhuhr,
            "Asr" to R.id.widget_slot_asr,
            "Maghrib" to R.id.widget_slot_maghrib,
            "Isha" to R.id.widget_slot_isha
        )
        slotIds.forEach { (key, viewId) ->
            views.setInt(
                viewId,
                "setBackgroundResource",
                if (key == nextKey) R.drawable.widget_prayer_pill else 0
            )
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

        appWidgetManager.updateAppWidget(widgetId, views)
    }

    /** Picks which of the 5 prayers is "next" given the times already computed by PrayerRepository. */
    fun determineNextKey(times: PrayerTimes): String {
        val now = java.util.Calendar.getInstance()
        val nowMinutes = now.get(java.util.Calendar.HOUR_OF_DAY) * 60 + now.get(java.util.Calendar.MINUTE)
        val ordered = listOf(
            "Fajr" to times.fajr, "Dhuhr" to times.dhuhr, "Asr" to times.asr,
            "Maghrib" to times.maghrib, "Isha" to times.isha
        )
        for ((key, value) in ordered) {
            val parts = value.split(":")
            val h = parts.getOrNull(0)?.toIntOrNull() ?: continue
            val m = parts.getOrNull(1)?.toIntOrNull() ?: continue
            if (h * 60 + m > nowMinutes) return key
        }
        return "Fajr" // past Isha: next prayer is tomorrow's Fajr
    }
}
