package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
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

    // ---- WeMuslim-style customization ----

    /** "default" | "sepia" | "night" | "green" */
    private val _quranReadingTheme = MutableStateFlow(prefs.getString("quran_theme", "default") ?: "default")
    val quranReadingTheme: StateFlow<String> = _quranReadingTheme.asStateFlow()
    fun setQuranReadingTheme(id: String) {
        _quranReadingTheme.value = id
        prefs.edit().putString("quran_theme", id).apply()
    }

    /** Scales Arabic ayah text size. 1.0 = default 24sp. */
    private val _quranFontScale = MutableStateFlow(prefs.getFloat("quran_font_scale", 1.0f))
    val quranFontScale: StateFlow<Float> = _quranFontScale.asStateFlow()
    fun setQuranFontScale(scale: Float) {
        val clamped = scale.coerceIn(0.75f, 1.75f)
        _quranFontScale.value = clamped
        prefs.edit().putFloat("quran_font_scale", clamped).apply()
    }

    /** Aladhan calculation method id, or null to auto-pick by country (the previous behavior). */
    private val _calculationMethodOverride = MutableStateFlow(
        prefs.getInt("calc_method_override", -1).let { if (it == -1) null else it }
    )
    val calculationMethodOverride: StateFlow<Int?> = _calculationMethodOverride.asStateFlow()
    fun setCalculationMethodOverride(methodId: Int?) {
        _calculationMethodOverride.value = methodId
        prefs.edit().putInt("calc_method_override", methodId ?: -1).apply()
    }

    /** Per-prayer manual fine-tuning in minutes, e.g. because of a tall horizon or local masjid timing. */
    private val _prayerOffsets = MutableStateFlow(
        mapOf(
            "Fajr" to prefs.getInt("offset_fajr", 0),
            "Dhuhr" to prefs.getInt("offset_dhuhr", 0),
            "Asr" to prefs.getInt("offset_asr", 0),
            "Maghrib" to prefs.getInt("offset_maghrib", 0),
            "Isha" to prefs.getInt("offset_isha", 0)
        )
    )
    val prayerOffsets: StateFlow<Map<String, Int>> = _prayerOffsets.asStateFlow()
    fun setPrayerOffset(prayerName: String, minutes: Int) {
        val clamped = minutes.coerceIn(-30, 30)
        _prayerOffsets.value = _prayerOffsets.value.toMutableMap().apply { put(prayerName, clamped) }
        prefs.edit().putInt("offset_${prayerName.lowercase(Locale.ROOT)}", clamped).apply()
    }

    // Tasbih goal (WeMuslim-style target count with a completion buzz)
    private val _tasbihGoal = MutableStateFlow(prefs.getInt("tasbih_goal", 33))
    val tasbihGoal: StateFlow<Int> = _tasbihGoal.asStateFlow()
    fun setTasbihGoal(goal: Int) {
        val clamped = goal.coerceIn(1, 10000)
        _tasbihGoal.value = clamped
        prefs.edit().putInt("tasbih_goal", clamped).apply()
    }

    // Quran Bookmarks + Continue Reading (WeMuslim-style "favorites" / "last read")
    private val _bookmarkedAyahs = MutableStateFlow(prefs.getStringSet("quran_bookmarks", emptySet()) ?: emptySet())
    val bookmarkedAyahs: StateFlow<Set<String>> = _bookmarkedAyahs.asStateFlow()

    fun toggleBookmark(surahNumber: Int, ayahNumber: Int) {
        val key = "$surahNumber:$ayahNumber"
        val current = _bookmarkedAyahs.value.toMutableSet()
        if (!current.remove(key)) current.add(key)
        _bookmarkedAyahs.value = current
        prefs.edit().putStringSet("quran_bookmarks", current).apply()
    }

    private val _lastReadSurahNumber = MutableStateFlow(
        prefs.getInt("last_read_surah", -1).let { if (it == -1) null else it }
    )
    val lastReadSurahNumber: StateFlow<Int?> = _lastReadSurahNumber.asStateFlow()

    private val _lastReadSurahName = MutableStateFlow(prefs.getString("last_read_surah_name", "") ?: "")
    val lastReadSurahName: StateFlow<String> = _lastReadSurahName.asStateFlow()

    fun setLastRead(surahNumber: Int, surahName: String) {
        _lastReadSurahNumber.value = surahNumber
        _lastReadSurahName.value = surahName
        prefs.edit()
            .putInt("last_read_surah", surahNumber)
            .putString("last_read_surah_name", surahName)
            .apply()
    }

    /** Which bundled sound plays for the full-screen prayer alert: "azan_short" or "prayer_alert". */
    private val _alertSound = MutableStateFlow(prefs.getString("alert_sound", "azan_short") ?: "azan_short")
    val alertSound: StateFlow<String> = _alertSound.asStateFlow()
    fun setAlertSound(id: String) {
        _alertSound.value = id
        prefs.edit().putString("alert_sound", id).apply()
    }

    // Prayer Accountability Lock: blocks the screen after each prayer's time starts,
    // until a photo is captured as proof (honor-system log, not verified by AI).
    private val _strictLockEnabled = MutableStateFlow(prefs.getBoolean("strict_lock", false))
    val strictLockEnabled: StateFlow<Boolean> = _strictLockEnabled.asStateFlow()

    private val _provenPrayers = MutableStateFlow(loadProvenPrayers())
    val provenPrayers: StateFlow<Set<String>> = _provenPrayers.asStateFlow()

    fun setStrictLockEnabled(enabled: Boolean) {
        _strictLockEnabled.value = enabled
        prefs.edit().putBoolean("strict_lock", enabled).apply()
    }

    /** Records a prayer as "proven" for today with a photo path, and also checks it off. */
    fun markPrayerProven(prayerName: String, photoPath: String) {
        val today = getTodayDateKey()
        val current = _provenPrayers.value.toMutableSet()
        current.add(prayerName)
        _provenPrayers.value = current
        prefs.edit()
            .putString("proven_date", today)
            .putStringSet("proven_set", current)
            .putString("proof_photo_$today" + "_$prayerName", photoPath)
            .apply()
        toggleCompletedPrayerIfNotSet(prayerName)
    }

    fun isPrayerProven(prayerName: String): Boolean = _provenPrayers.value.contains(prayerName)

    fun getProofPhotoPath(prayerName: String): String? {
        val today = getTodayDateKey()
        return prefs.getString("proof_photo_$today" + "_$prayerName", null)
    }

    private fun toggleCompletedPrayerIfNotSet(prayerName: String) {
        if (!_completedPrayers.value.contains(prayerName)) toggleCompletedPrayer(prayerName)
    }

    private fun loadProvenPrayers(): Set<String> {
        val today = getTodayDateKey()
        val savedDate = prefs.getString("proven_date", "")
        return if (savedDate == today) prefs.getStringSet("proven_set", emptySet()) ?: emptySet() else emptySet()
    }

    private val _completedPrayers = MutableStateFlow(loadCompletedPrayers())
    val completedPrayers: StateFlow<Set<String>> = _completedPrayers.asStateFlow()

    private val _city = MutableStateFlow(prefs.getString("city", "Makkah, Saudi Arabia") ?: "Makkah, Saudi Arabia")
    val city: StateFlow<String> = _city.asStateFlow()

    private val _latitude = MutableStateFlow(prefs.getFloat("lat", 21.4225f).toDouble())
    val latitude: StateFlow<Double> = _latitude.asStateFlow()

    private val _longitude = MutableStateFlow(prefs.getFloat("lng", 39.8262f).toDouble())
    val longitude: StateFlow<Double> = _longitude.asStateFlow()

    private val _countryCode = MutableStateFlow(prefs.getString("country", "SA") ?: "SA")
    val countryCode: StateFlow<String> = _countryCode.asStateFlow()

    // True when the saved location came from the device GPS (so we can refresh it on app start)
    private val _autoLocation = MutableStateFlow(prefs.getBoolean("auto_location", false))
    val autoLocation: StateFlow<Boolean> = _autoLocation.asStateFlow()

    /** True once the app has tried to auto-detect location on first launch, so it only does this once. */
    val hasRequestedInitialLocation: Boolean
        get() = prefs.getBoolean("requested_initial_location", false)
    fun setHasRequestedInitialLocation() {
        prefs.edit().putBoolean("requested_initial_location", true).apply()
    }

    // 30 Features Settings (Madhab, Azan, Ramadan, Hifz, Good Deeds)
    private val _madhab = MutableStateFlow(prefs.getString("madhab", "shafii") ?: "shafii")
    val madhab: StateFlow<String> = _madhab.asStateFlow()

    private val _goodDeeds = MutableStateFlow(loadTodayGoodDeeds())
    val goodDeeds: StateFlow<Set<String>> = _goodDeeds.asStateFlow()

    private val _azkarCompleted = MutableStateFlow(loadTodayAzkar())
    val azkarCompleted: StateFlow<Set<String>> = _azkarCompleted.asStateFlow()

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

    fun setLocation(
        cityName: String,
        lat: Double,
        lng: Double,
        countryCode: String = _countryCode.value,
        auto: Boolean = false
    ) {
        _city.value = cityName
        _latitude.value = lat
        _longitude.value = lng
        _countryCode.value = countryCode
        _autoLocation.value = auto
        prefs.edit()
            .putString("city", cityName)
            .putFloat("lat", lat.toFloat())
            .putFloat("lng", lng.toFloat())
            .putString("country", countryCode)
            .putBoolean("auto_location", auto)
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
        // Archived separately (unlike the "today" working set) so a weekly history can be shown.
        prefs.edit().putStringSet("completed_history_$today", currentSet).apply()
    }

    /** Last 7 days (oldest first) as (dayLabel, isToday, completedCount) for a weekly streak view. */
    fun getWeekHistory(): List<Triple<String, Boolean, Int>> {
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val keyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val calendar = Calendar.getInstance()
        val today = getTodayDateKey()
        val result = mutableListOf<Triple<String, Boolean, Int>>()
        calendar.add(Calendar.DAY_OF_YEAR, -6)
        repeat(7) {
            val dateKey = keyFormat.format(calendar.time)
            val count = prefs.getStringSet("completed_history_$dateKey", null)?.size ?: 0
            result.add(Triple(dayFormat.format(calendar.time), dateKey == today, count))
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return result
    }

    fun toggleGoodDeed(deedId: String) {
        val today = getTodayDateKey()
        val current = _goodDeeds.value.toMutableSet()
        if (current.contains(deedId)) current.remove(deedId) else current.add(deedId)
        _goodDeeds.value = current
        prefs.edit().putString("deeds_date", today).putStringSet("deeds_set", current).apply()
    }

    fun toggleAzkar(azkarId: String) {
        val today = getTodayDateKey()
        val current = _azkarCompleted.value.toMutableSet()
        if (current.contains(azkarId)) current.remove(azkarId) else current.add(azkarId)
        _azkarCompleted.value = current
        prefs.edit().putString("azkar_date", today).putStringSet("azkar_set", current).apply()
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

    private fun loadTodayAzkar(): Set<String> {
        val today = getTodayDateKey()
        val savedDate = prefs.getString("azkar_date", "")
        return if (savedDate == today) {
            prefs.getStringSet("azkar_set", emptySet()) ?: emptySet()
        } else {
            emptySet()
        }
    }

    // Hadith of the Day: cached once per calendar day so it doesn't refetch on every visit.
    fun getCachedHadithOfDay(): com.example.model.HadithItem? {
        val today = getTodayDateKey()
        if (prefs.getString("hadith_of_day_date", "") != today) return null
        val collection = prefs.getString("hadith_of_day_collection", null) ?: return null
        return com.example.model.HadithItem(
            id = prefs.getInt("hadith_of_day_id", 0),
            collection = collection,
            number = prefs.getString("hadith_of_day_number", "") ?: "",
            narrator = "",
            arabicText = prefs.getString("hadith_of_day_ar", "") ?: "",
            englishText = prefs.getString("hadith_of_day_en", "") ?: "",
            chapter = "",
            grade = prefs.getString("hadith_of_day_grade", "") ?: "Reported"
        )
    }

    fun setCachedHadithOfDay(item: com.example.model.HadithItem) {
        prefs.edit()
            .putString("hadith_of_day_date", getTodayDateKey())
            .putString("hadith_of_day_collection", item.collection)
            .putInt("hadith_of_day_id", item.id)
            .putString("hadith_of_day_number", item.number)
            .putString("hadith_of_day_ar", item.arabicText)
            .putString("hadith_of_day_en", item.englishText)
            .putString("hadith_of_day_grade", item.grade)
            .apply()
    }

    private fun getTodayDateKey(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }
}
