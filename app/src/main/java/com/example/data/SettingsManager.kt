package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("al_huda_prefs", Context.MODE_PRIVATE)

    private val _theme = MutableStateFlow(prefs.getString("theme", "emerald") ?: "emerald")
    val theme: StateFlow<String> = _theme.asStateFlow()

    private val _accent = MutableStateFlow(prefs.getString("accent", "emerald") ?: "emerald")
    val accent: StateFlow<String> = _accent.asStateFlow()

    private val _lang = MutableStateFlow(prefs.getString("lang", "en") ?: "en")
    val lang: StateFlow<String> = _lang.asStateFlow()

    private val _soundsEnabled = MutableStateFlow(prefs.getBoolean("sounds", true))
    val soundsEnabled: StateFlow<Boolean> = _soundsEnabled.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(prefs.getBoolean("notifs", true))
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _completedPrayers = MutableStateFlow(loadCompletedPrayers())
    val completedPrayers: StateFlow<Set<String>> = _completedPrayers.asStateFlow()

    private val _city = MutableStateFlow(prefs.getString("city", "Makkah, Saudi Arabia") ?: "Makkah, Saudi Arabia")
    val city: StateFlow<String> = _city.asStateFlow()

    private val _latitude = MutableStateFlow(prefs.getFloat("lat", 21.4225f).toDouble())
    val latitude: StateFlow<Double> = _latitude.asStateFlow()

    private val _longitude = MutableStateFlow(prefs.getFloat("lng", 39.8262f).toDouble())
    val longitude: StateFlow<Double> = _longitude.asStateFlow()

    // 30 Features Settings (Madhab, Azan, Ramadan, Hifz, Good Deeds)
    private val _madhab = MutableStateFlow(prefs.getString("madhab", "shafii") ?: "shafii")
    val madhab: StateFlow<String> = _madhab.asStateFlow()

    private val _goodDeeds = MutableStateFlow(loadTodayGoodDeeds())
    val goodDeeds: StateFlow<Set<String>> = _goodDeeds.asStateFlow()

    private val _ramadanFasts = MutableStateFlow(prefs.getStringSet("ramadan_fasts", emptySet()) ?: emptySet())
    val ramadanFasts: StateFlow<Set<String>> = _ramadanFasts.asStateFlow()

    private val _quizScore = MutableStateFlow(prefs.getInt("quiz_score", 0))
    val quizScore: StateFlow<Int> = _quizScore.asStateFlow()

    private val _hifzMemorized = MutableStateFlow(prefs.getStringSet("hifz_memorized", setOf("1", "112", "113", "114")) ?: setOf("1", "112", "113", "114"))
    val hifzMemorized: StateFlow<Set<String>> = _hifzMemorized.asStateFlow()

    fun setTheme(themeId: String) {
        _theme.value = themeId
        prefs.edit().putString("theme", themeId).apply()
    }

    fun setAccent(accentId: String) {
        _accent.value = accentId
        prefs.edit().putString("accent", accentId).apply()
    }

    fun setLanguage(language: String) {
        _lang.value = language
        prefs.edit().putString("lang", language).apply()
    }

    fun setSounds(enabled: Boolean) {
        _soundsEnabled.value = enabled
        prefs.edit().putBoolean("sounds", enabled).apply()
    }

    fun setNotifications(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        prefs.edit().putBoolean("notifs", enabled).apply()
    }

    fun setMadhab(m: String) {
        _madhab.value = m
        prefs.edit().putString("madhab", m).apply()
    }

    fun setLocation(cityName: String, lat: Double, lng: Double) {
        _city.value = cityName
        _latitude.value = lat
        _longitude.value = lng
        prefs.edit()
            .putString("city", cityName)
            .putFloat("lat", lat.toFloat())
            .putFloat("lng", lng.toFloat())
            .apply()
    }

    fun toggleCompletedPrayer(prayerName: String) {
        val today = getTodayDateKey()
        val currentSet = _completedPrayers.value.toMutableSet()
        if (currentSet.contains(prayerName)) {
            currentSet.remove(prayerName)
        } else {
            currentSet.add(prayerName)
        }
        _completedPrayers.value = currentSet
        saveCompletedPrayers(today, currentSet)
    }

    fun toggleGoodDeed(deedId: String) {
        val today = getTodayDateKey()
        val current = _goodDeeds.value.toMutableSet()
        if (current.contains(deedId)) current.remove(deedId) else current.add(deedId)
        _goodDeeds.value = current
        prefs.edit().putString("deeds_date", today).putStringSet("deeds_set", current).apply()
    }

    fun toggleRamadanFast(dayNum: Int) {
        val key = dayNum.toString()
        val current = _ramadanFasts.value.toMutableSet()
        if (current.contains(key)) current.remove(key) else current.add(key)
        _ramadanFasts.value = current
        prefs.edit().putStringSet("ramadan_fasts", current).apply()
    }

    fun toggleHifz(surahNum: Int) {
        val key = surahNum.toString()
        val current = _hifzMemorized.value.toMutableSet()
        if (current.contains(key)) current.remove(key) else current.add(key)
        _hifzMemorized.value = current
        prefs.edit().putStringSet("hifz_memorized", current).apply()
    }

    fun addQuizPoint() {
        val newScore = _quizScore.value + 10
        _quizScore.value = newScore
        prefs.edit().putInt("quiz_score", newScore).apply()
    }

    private fun loadCompletedPrayers(): Set<String> {
        val today = getTodayDateKey()
        val savedDate = prefs.getString("completed_date", "")
        return if (savedDate == today) {
            prefs.getStringSet("completed_set", emptySet()) ?: emptySet()
        } else {
            emptySet()
        }
    }

    private fun saveCompletedPrayers(dateKey: String, set: Set<String>) {
        prefs.edit()
            .putString("completed_date", dateKey)
            .putStringSet("completed_set", set)
            .apply()
    }

    private fun loadTodayGoodDeeds(): Set<String> {
        val today = getTodayDateKey()
        val savedDate = prefs.getString("deeds_date", "")
        return if (savedDate == today) {
            prefs.getStringSet("deeds_set", emptySet()) ?: emptySet()
        } else {
            emptySet()
        }
    }

    private fun getTodayDateKey(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }
}
