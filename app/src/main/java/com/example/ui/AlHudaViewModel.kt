package com.example.ui

import android.app.Application
import android.content.Context
import android.os.Build
import com.example.widget.WidgetUpdater
import android.content.Intent
import android.location.Location
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.model.*
import com.example.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Calendar
import java.util.Locale

class AlHudaViewModel(application: Application) : AndroidViewModel(application) {

    val prayerRepo = PrayerRepository(application.getSharedPreferences("prayer_cache", Context.MODE_PRIVATE))
    val geoRepo = GeoRepository(application)
    val quranRepo = QuranRepository()
    val spiritualRepo = SpiritualRepository()
    val eduRepo = EducationAndWorshipRepository()
    val hadithRepo = com.example.data.HadithRepository(application)
    val mosqueRepo = com.example.data.MosqueRepository()
    val settingsManager = SettingsManager(application)
    val audioHelper = AudioHelper(application)
    val qiblaHelper = QiblaHelper(application)
    val audioPlayer = AudioPlayerHelper()
    val azanAudio = AzanAudioHelper(application)
    private val locationHelper = LocationHelper(application)

    // Splash screen state
    private val _showSplash = MutableStateFlow(true)
    val showSplash: StateFlow<Boolean> = _showSplash.asStateFlow()

    // Navigation state: Main Tabs ("home", "quran", "dhikr", "features", "library")
    private val _activeTab = MutableStateFlow("home")
    val activeTab: StateFlow<String> = _activeTab.asStateFlow()

    // Sub-feature drilldown screen
    private val _activeSubFeature = MutableStateFlow<String?>(null)
    val activeSubFeature: StateFlow<String?> = _activeSubFeature.asStateFlow()

    // Settings dialog state
    private val _showSettings = MutableStateFlow(false)
    val showSettings: StateFlow<Boolean> = _showSettings.asStateFlow()

    // Prayer times state
    private val _prayerTimes = MutableStateFlow<PrayerTimes?>(null)
    val prayerTimes: StateFlow<PrayerTimes?> = _prayerTimes.asStateFlow()

    private val _hijriDate = MutableStateFlow<HijriDate?>(null)
    val hijriDate: StateFlow<HijriDate?> = _hijriDate.asStateFlow()

    private val _nextPrayer = MutableStateFlow<String?>("Fajr")
    val nextPrayer: StateFlow<String?> = _nextPrayer.asStateFlow()

    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()

    private val _citySearchQuery = MutableStateFlow("")
    val citySearchQuery: StateFlow<String> = _citySearchQuery.asStateFlow()

    private val _citySuggestions = MutableStateFlow<List<GeoPlace>>(emptyList())
    val citySuggestions: StateFlow<List<GeoPlace>> = _citySuggestions.asStateFlow()

    private val _isLocating = MutableStateFlow(false)
    val isLocating: StateFlow<Boolean> = _isLocating.asStateFlow()

    private var citySearchJob: Job? = null
    private var prayerJob: Job? = null
    private var lastLoadedDay = -1

    // Quran State
    private val _selectedSurah = MutableStateFlow<Surah?>(null)
    val selectedSurah: StateFlow<Surah?> = _selectedSurah.asStateFlow()

    private val _ayahs = MutableStateFlow<List<Ayah>>(emptyList())
    val ayahs: StateFlow<List<Ayah>> = _ayahs.asStateFlow()

    private val _isLoadingAyahs = MutableStateFlow(false)
    val isLoadingAyahs: StateFlow<Boolean> = _isLoadingAyahs.asStateFlow()

    private val _surahSearchQuery = MutableStateFlow("")
    val surahSearchQuery: StateFlow<String> = _surahSearchQuery.asStateFlow()

    private val _selectedReciterId = MutableStateFlow("mishary")
    val selectedReciterId: StateFlow<String> = _selectedReciterId.asStateFlow()

    // Dhikr State
    private val _dhikrCount = MutableStateFlow(0)
    val dhikrCount: StateFlow<Int> = _dhikrCount.asStateFlow()

    private val _dhikrPhraseIndex = MutableStateFlow(0)
    val dhikrPhraseIndex: StateFlow<Int> = _dhikrPhraseIndex.asStateFlow()

    // Quiz State
    private val _quizQuestionIndex = MutableStateFlow(0)
    val quizQuestionIndex: StateFlow<Int> = _quizQuestionIndex.asStateFlow()

    private val _quizSelectedOption = MutableStateFlow<Int?>(null)
    val quizSelectedOption: StateFlow<Int?> = _quizSelectedOption.asStateFlow()

    // Ramadan state
    private val _ramadanJuzProgress = MutableStateFlow(1)
    val ramadanJuzProgress: StateFlow<Int> = _ramadanJuzProgress.asStateFlow()

    // Last prayer alerted to avoid duplicate Athan triggers
    private var lastAlertedPrayerKey: String? = null

    // Festivals, Library, Daily Wisdom
    val festivals: List<HolyFestival> = spiritualRepo.getHolyFestivals()
    val libraryContent: LibraryContent = spiritualRepo.getLibraryContent()
    val dailyWisdom: DailyWisdom = spiritualRepo.getDailyWisdom()

    init {
        // Sync audio feedback settings
        viewModelScope.launch {
            settingsManager.soundsEnabled.collect {
                audioHelper.setSoundEnabled(it)
            }
        }

        // Sync notifications alarm scheduler
        viewModelScope.launch {
            settingsManager.notificationsEnabled.collect { enabled ->
                PrayerNotificationScheduler.scheduleAllPrayerAlerts(
                    getApplication(),
                    _prayerTimes.value,
                    settingsManager.city.value,
                    enabled
                )
            }
        }

        loadPrayerTimes()

        // If the saved location came from GPS, quietly refresh it (user may have travelled)
        if (settingsManager.autoLocation.value && locationHelper.hasPermission()) {
            autoLocate(silent = true)
        }

        // Splash screen timeout
        viewModelScope.launch {
            delay(2200)
            _showSplash.value = false
        }

        // Live prayer time checker for real-time in-app Athan sound
        viewModelScope.launch {
            while (true) {
                delay(20000)
                checkCurrentPrayerTimeAlert()
                // New day: fetch fresh prayer times
                if (Calendar.getInstance().get(Calendar.DAY_OF_YEAR) != lastLoadedDay) loadPrayerTimes()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        qiblaHelper.stopListening()
        audioPlayer.stop()
        azanAudio.stop()
    }

    fun loadPrayerTimes() {
        val lat = settingsManager.latitude.value
        val lng = settingsManager.longitude.value
        val country = settingsManager.countryCode.value
        val hanafi = settingsManager.madhab.value == "hanafi"
        val methodOverride = settingsManager.calculationMethodOverride.value
        val offsets = settingsManager.prayerOffsets.value
        lastLoadedDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)

        // Instant local estimate so the screen is never empty
        val (rawCalcTimes, calcHijri) = prayerRepo.calculateOfflinePrayerTimes(lat, lng, hanafi = hanafi)
        val calcTimes = prayerRepo.applyOffsets(rawCalcTimes, offsets)
        _prayerTimes.value = calcTimes
        _hijriDate.value = calcHijri
        determineNextPrayer(calcTimes)

        // Keep the home-screen widget in sync
        try {
            WidgetUpdater.saveLatest(
                getApplication(),
                calcTimes,
                WidgetUpdater.determineNextKey(calcTimes),
                settingsManager.city.value,
                "${calcHijri.day} ${if (settingsManager.lang.value == "ar") calcHijri.monthAr else calcHijri.monthEn}"
            )
        } catch (_: Exception) {}
        PrayerNotificationScheduler.scheduleAllPrayerAlerts(
            getApplication(),
            calcTimes,
            settingsManager.city.value,
            settingsManager.notificationsEnabled.value
        )

        // Accurate times from the Aladhan API (cached monthly for offline use)
        prayerJob?.cancel()
        prayerJob = viewModelScope.launch {
            val onlineResult = prayerRepo.getPrayerTimesOnline(lat, lng, country, hanafi, methodOverride)
            if (onlineResult != null) {
                val adjustedTimes = prayerRepo.applyOffsets(onlineResult.first, offsets)
                _prayerTimes.value = adjustedTimes
                _hijriDate.value = onlineResult.second
                determineNextPrayer(adjustedTimes)
                _isOfflineMode.value = false

                try {
                    WidgetUpdater.saveLatest(
                        getApplication(),
                        adjustedTimes,
                        WidgetUpdater.determineNextKey(adjustedTimes),
                        settingsManager.city.value,
                        "${onlineResult.second.day} ${if (settingsManager.lang.value == "ar") onlineResult.second.monthAr else onlineResult.second.monthEn}"
                    )
                } catch (_: Exception) {}

                PrayerNotificationScheduler.scheduleAllPrayerAlerts(
                    getApplication(),
                    adjustedTimes,
                    settingsManager.city.value,
                    settingsManager.notificationsEnabled.value
                )
            } else {
                _isOfflineMode.value = true
            }
        }
    }

    private fun checkCurrentPrayerTimeAlert() {
        if (!settingsManager.notificationsEnabled.value) return
        val times = _prayerTimes.value ?: return

        val cal = Calendar.getInstance()
        val currentH = cal.get(Calendar.HOUR_OF_DAY)
        val currentM = cal.get(Calendar.MINUTE)
        val currentTimeFormatted = String.format(Locale.US, "%02d:%02d", currentH, currentM)

        val prayerMap = mapOf(
            "Fajr" to times.fajr,
            "Dhuhr" to times.dhuhr,
            "Asr" to times.asr,
            "Maghrib" to times.maghrib,
            "Isha" to times.isha
        )

        prayerMap.forEach { (name, timeStr) ->
            if (timeStr == currentTimeFormatted && lastAlertedPrayerKey != "$name-$currentTimeFormatted") {
                lastAlertedPrayerKey = "$name-$currentTimeFormatted"
                // Trigger Athan recitation sound!
                azanAudio.playAzan(name)
            }
        }
    }

    fun onCitySearch(query: String) {
        _citySearchQuery.value = query
        citySearchJob?.cancel()
        val q = query.trim()
        if (q.length < 2) {
            _citySuggestions.value = emptyList()
            return
        }
        citySearchJob = viewModelScope.launch {
            delay(350) // debounce typing
            var results = geoRepo.searchCities(q, settingsManager.lang.value)
            if (results.isEmpty()) {
                // Offline / no result: fall back to built-in cities
                val lower = q.lowercase(Locale.ROOT)
                results = prayerRepo.presetCities
                    .filter { it.name.lowercase(Locale.ROOT).contains(lower) }
                    .map { GeoPlace(it.name, it.latitude, it.longitude, it.countryCode) }
            }
            _citySuggestions.value = results
        }
    }

    fun selectCity(place: GeoPlace) {
        settingsManager.setLocation(place.name, place.latitude, place.longitude, place.countryCode, auto = false)
        citySearchJob?.cancel()
        _citySearchQuery.value = ""
        _citySuggestions.value = emptyList()
        loadPrayerTimes()
        audioHelper.playTick()
    }

    /**
     * Detects the user's location: device location first (Google fused / GPS),
     * then an IP-based lookup if the device can't provide one. Needs no setup from the user.
     */
    fun autoLocate(silent: Boolean = false) {
        if (_isLocating.value) return
        viewModelScope.launch {
            _isLocating.value = true
            try {
                val lang = settingsManager.lang.value
                val isAr = lang == "ar"
                fun say(ar: String, en: String, long: Boolean = false) {
                    if (silent) return
                    Toast.makeText(
                        getApplication(),
                        if (isAr) ar else en,
                        if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
                    ).show()
                }

                say("جاري تحديد موقعك…", "Finding your location…")

                val gps = locationHelper.getCurrentLocation()
                var lat: Double
                var lng: Double
                var ipPlace: GeoPlace? = null

                if (gps != null) {
                    lat = gps.latitude
                    lng = gps.longitude
                } else {
                    // Silent refreshes never replace a precise saved location with an approximate one
                    if (silent) return@launch
                    ipPlace = geoRepo.ipLocate()
                    if (ipPlace == null) {
                        say(
                            "تعذر تحديد الموقع. تأكد من الإنترنت وتشغيل خدمة الموقع، أو ابحث عن مدينتك.",
                            "Couldn't detect your location. Check your internet and Location setting, or search your city.",
                            long = true
                        )
                        return@launch
                    }
                    lat = ipPlace.latitude
                    lng = ipPlace.longitude
                }

                // Silent refresh: ignore small movements (< 3 km)
                if (silent) {
                    val out = FloatArray(1)
                    Location.distanceBetween(
                        settingsManager.latitude.value, settingsManager.longitude.value, lat, lng, out
                    )
                    if (out[0] < 3000f) return@launch
                }

                val resolved = geoRepo.reverse(lat, lng, lang)
                val name = resolved?.name?.ifBlank { null }
                    ?: ipPlace?.name?.ifBlank { null }
                    ?: (if (isAr) "موقعي الحالي" else "Current location")
                val country = resolved?.countryCode?.ifBlank { null }
                    ?: ipPlace?.countryCode?.ifBlank { null }
                    ?: settingsManager.countryCode.value

                settingsManager.setLocation(name, lat, lng, country, auto = gps != null)
                loadPrayerTimes()

                if (gps != null) {
                    say("تم تحديث مواقيت الصلاة: $name", "Prayer times updated for $name")
                } else {
                    say(
                        "تم استخدام موقع تقريبي: $name. فعّل GPS لموقع أدق.",
                        "Using approximate location: $name. Turn on GPS and allow location for exact times.",
                        long = true
                    )
                }
            } finally {
                _isLocating.value = false
            }
        }
    }

    private fun determineNextPrayer(times: PrayerTimes) {
        val cal = Calendar.getInstance()
        val currentMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)

        fun parseMin(timeStr: String): Int {
            val parts = timeStr.split(":")
            if (parts.size < 2) return 0
            val h = parts[0].toIntOrNull() ?: 0
            val m = parts[1].toIntOrNull() ?: 0
            return h * 60 + m
        }

        val prayerList = listOf(
            "Fajr" to parseMin(times.fajr),
            "Sunrise" to parseMin(times.sunrise),
            "Dhuhr" to parseMin(times.dhuhr),
            "Asr" to parseMin(times.asr),
            "Maghrib" to parseMin(times.maghrib),
            "Isha" to parseMin(times.isha)
        )

        val next = prayerList.firstOrNull { it.second > currentMinutes } ?: prayerList[0]
        _nextPrayer.value = next.first
    }

    fun isPrayerPassed(timeStr: String): Boolean {
        val cal = Calendar.getInstance()
        val currentMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        val parts = timeStr.split(":")
        if (parts.size < 2) return false
        val h = parts[0].toIntOrNull() ?: 0
        val m = parts[1].toIntOrNull() ?: 0
        return currentMinutes > (h * 60 + m)
    }

    fun isTodayFriday(): Boolean {
        return Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY
    }

    fun togglePrayerCompleted(name: String) {
        audioHelper.playTick()
        settingsManager.toggleCompletedPrayer(name)
    }

    fun setActiveTab(tab: String) {
        if (_activeTab.value != tab) {
            _activeTab.value = tab
            _activeSubFeature.value = null
            audioHelper.playTabChange()
            if (tab == "home") qiblaHelper.stopListening()
        }
    }

    fun openSubFeature(featureId: String) {
        audioHelper.playTick()
        _activeSubFeature.value = featureId
        if (featureId == "qibla") {
            qiblaHelper.startListening()
        } else {
            qiblaHelper.stopListening()
        }
    }

    fun closeSubFeature() {
        audioHelper.playTick()
        _activeSubFeature.value = null
        qiblaHelper.stopListening()
    }

    fun setShowSettings(show: Boolean) {
        _showSettings.value = show
        audioHelper.playTick()
    }

    // Sound for EVERY single Dhikr count tap!
    fun incrementDhikr() {
        val newCount = _dhikrCount.value + 1
        _dhikrCount.value = newCount

        // Distinct tactile bead click sound on EVERY single count
        audioHelper.playBeadClick()

        // Milestone celebratory chime on 33, 99, 100
        if (newCount == 33 || newCount == 99 || newCount == 100) {
            audioHelper.playChime()
        }

        // Reaching the person's chosen goal: chime + a short buzz so it's felt, not just heard
        if (newCount == settingsManager.tasbihGoal.value) {
            audioHelper.playChime()
            vibrateGoalReached()
        }
    }

    private fun vibrateGoalReached() {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = getApplication<Application>().getSystemService(android.os.VibratorManager::class.java)
                manager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getApplication<Application>().getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(android.os.VibrationEffect.createWaveform(longArrayOf(0, 80, 60, 80, 60, 150), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 80, 60, 80, 60, 150), -1)
            }
        } catch (_: Exception) {}
    }

    fun resetDhikr() {
        _dhikrCount.value = 0
        audioHelper.playTick()
    }

    fun nextDhikrPhrase() {
        _dhikrPhraseIndex.value = (_dhikrPhraseIndex.value + 1) % spiritualRepo.dhikrPhrases.size
        _dhikrCount.value = 0
        audioHelper.playTabChange()
    }

    // Athan / Azan Controls
    fun playAzan(prayerName: String = "Salah") {
        audioHelper.playTick()
        azanAudio.playAzan(prayerName)
    }

    fun stopAzan() {
        audioHelper.playTick()
        azanAudio.stop()
    }

    fun triggerTestPrayerNotification() {
        audioHelper.playTick()
        val nextP = _nextPrayer.value ?: "Fajr"
        val city = settingsManager.city.value
        PrayerNotificationScheduler.triggerImmediateTestAlert(getApplication(), nextP, city)
    }

    // Quran & Audio
    fun selectSurah(surah: Surah) {
        audioHelper.playTick()
        _selectedSurah.value = surah
        settingsManager.setLastRead(surah.number, surah.nameEn)
        _isLoadingAyahs.value = true
        viewModelScope.launch {
            val list = quranRepo.getAyahsForSurah(surah.number)
            _ayahs.value = list
            _isLoadingAyahs.value = false
        }
    }

    // "surah" or "juz" browsing mode on the Quran home screen
    private val _quranBrowseMode = MutableStateFlow("surah")
    val quranBrowseMode: StateFlow<String> = _quranBrowseMode.asStateFlow()
    fun setQuranBrowseMode(mode: String) {
        _quranBrowseMode.value = mode
        audioHelper.playTick()
    }

    private val _showBookmarks = MutableStateFlow(false)
    val showBookmarks: StateFlow<Boolean> = _showBookmarks.asStateFlow()
    fun toggleShowBookmarks(show: Boolean) {
        _showBookmarks.value = show
        audioHelper.playTick()
    }

    fun toggleAyahBookmark(ayahNumber: Int) {
        val surahNumber = _selectedSurah.value?.number ?: return
        settingsManager.toggleBookmark(surahNumber, ayahNumber)
        audioHelper.playTick()
    }

    fun clearSelectedSurah() {
        audioHelper.playTick()
        _selectedSurah.value = null
        _ayahs.value = emptyList()
        audioPlayer.stop()
    }

    fun onSurahSearch(query: String) {
        _surahSearchQuery.value = query
    }

    fun selectReciter(reciterId: String) {
        _selectedReciterId.value = reciterId
        audioHelper.playTick()
    }

    fun playCurrentSurahAudio() {
        val surah = _selectedSurah.value ?: return
        val reciter = _selectedReciterId.value
        val url = eduRepo.getAudioUrlForSurah(reciter, surah.number)
        audioPlayer.playAudio(url, "${surah.nameEn} • ${eduRepo.reciters.firstOrNull { it.id == reciter }?.name}")
    }

    // Quiz
    fun answerQuiz(optionIndex: Int) {
        if (_quizSelectedOption.value != null) return
        _quizSelectedOption.value = optionIndex
        val q = eduRepo.quizQuestions[_quizQuestionIndex.value]
        if (optionIndex == q.correctIndex) {
            audioHelper.playChime()
            settingsManager.addQuizPoint()
        } else {
            audioHelper.playTick()
        }
    }

    fun nextQuizQuestion() {
        audioHelper.playTick()
        _quizSelectedOption.value = null
        _quizQuestionIndex.value = (_quizQuestionIndex.value + 1) % eduRepo.quizQuestions.size
    }

    // Hadith Library (live data from the fawazahmed0 Hadith API)
    private val _hadithSelectedCollection = MutableStateFlow<String?>(null)
    val hadithSelectedCollection: StateFlow<String?> = _hadithSelectedCollection.asStateFlow()

    private val _hadithList = MutableStateFlow<List<com.example.model.HadithItem>>(emptyList())
    val hadithList: StateFlow<List<com.example.model.HadithItem>> = _hadithList.asStateFlow()

    private val _isLoadingHadiths = MutableStateFlow(false)
    val isLoadingHadiths: StateFlow<Boolean> = _isLoadingHadiths.asStateFlow()

    private val _hadithLoadFailed = MutableStateFlow(false)
    val hadithLoadFailed: StateFlow<Boolean> = _hadithLoadFailed.asStateFlow()

    private val _hadithSearchQuery = MutableStateFlow("")
    val hadithSearchQuery: StateFlow<String> = _hadithSearchQuery.asStateFlow()

    private val _hadithOfTheDay = MutableStateFlow<com.example.model.HadithItem?>(null)
    val hadithOfTheDay: StateFlow<com.example.model.HadithItem?> = _hadithOfTheDay.asStateFlow()

    private val _arabicByHadithId = MutableStateFlow<Map<Int, String>>(emptyMap())
    val arabicByHadithId: StateFlow<Map<Int, String>> = _arabicByHadithId.asStateFlow()

    private var hadithLoadJob: Job? = null

    fun loadHadithCollection(slug: String) {
        if (_hadithSelectedCollection.value == slug && _hadithList.value.isNotEmpty()) return
        audioHelper.playTick()
        _hadithSelectedCollection.value = slug
        _hadithSearchQuery.value = ""
        _hadithList.value = emptyList()
        _hadithLoadFailed.value = false
        hadithLoadJob?.cancel()
        hadithLoadJob = viewModelScope.launch {
            _isLoadingHadiths.value = true
            val items = hadithRepo.getCollection(slug)
            _hadithList.value = items
            _hadithLoadFailed.value = items.isEmpty()
            _isLoadingHadiths.value = false
        }
    }

    fun clearHadithCollection() {
        _hadithSelectedCollection.value = null
        _hadithList.value = emptyList()
        _hadithSearchQuery.value = ""
    }

    fun onHadithSearchChanged(query: String) {
        _hadithSearchQuery.value = query
    }

    val filteredHadiths: List<com.example.model.HadithItem>
        get() = hadithRepo.search(_hadithList.value, _hadithSearchQuery.value)

    fun fetchArabicForHadith(hadithId: Int) {
        val slug = _hadithSelectedCollection.value ?: return
        if (_arabicByHadithId.value.containsKey(hadithId)) return
        viewModelScope.launch {
            val arabic = hadithRepo.getArabicText(slug, hadithId) ?: return@launch
            _arabicByHadithId.value = _arabicByHadithId.value + (hadithId to arabic)
        }
    }

    fun loadHadithOfTheDay(force: Boolean = false) {
        if (_hadithOfTheDay.value != null && !force) return
        viewModelScope.launch {
            val cached = settingsManager.getCachedHadithOfDay()
            if (cached != null && !force) {
                _hadithOfTheDay.value = cached
                return@launch
            }
            val fresh = hadithRepo.getRandomHadith() ?: return@launch
            _hadithOfTheDay.value = fresh
            settingsManager.setCachedHadithOfDay(fresh)
        }
    }

    // Nearby Mosques (real OpenStreetMap/Overpass lookup around the saved location)
    private val _nearbyMosques = MutableStateFlow<List<com.example.model.MosqueItem>>(emptyList())
    val nearbyMosques: StateFlow<List<com.example.model.MosqueItem>> = _nearbyMosques.asStateFlow()

    private val _isLoadingMosques = MutableStateFlow(false)
    val isLoadingMosques: StateFlow<Boolean> = _isLoadingMosques.asStateFlow()

    private val _mosqueLoadFailed = MutableStateFlow(false)
    val mosqueLoadFailed: StateFlow<Boolean> = _mosqueLoadFailed.asStateFlow()

    fun loadNearbyMosques(force: Boolean = false) {
        if (_isLoadingMosques.value) return
        if (_nearbyMosques.value.isNotEmpty() && !force) return
        viewModelScope.launch {
            _isLoadingMosques.value = true
            _mosqueLoadFailed.value = false
            val lat = settingsManager.latitude.value
            val lng = settingsManager.longitude.value
            val results = mosqueRepo.findNearby(lat, lng)
            _nearbyMosques.value = results
            _mosqueLoadFailed.value = results.isEmpty()
            _isLoadingMosques.value = false
        }
    }

    // Customization: changing any of these should immediately recompute prayer times
    fun setCalculationMethod(methodId: Int?) {
        settingsManager.setCalculationMethodOverride(methodId)
        audioHelper.playTick()
        loadPrayerTimes()
    }

    fun setPrayerOffset(prayerName: String, minutes: Int) {
        settingsManager.setPrayerOffset(prayerName, minutes)
        loadPrayerTimes()
    }

    fun setRamadanJuz(juz: Int) {
        _ramadanJuzProgress.value = juz.coerceIn(1, 30)
        audioHelper.playTick()
    }

    // Export Schedule
    fun exportAnnualSchedule() {
        audioHelper.playChime()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val year = Calendar.getInstance().get(Calendar.YEAR)
                val lat = settingsManager.latitude.value
                val lng = settingsManager.longitude.value
                val cityName = settingsManager.city.value
                val textContent = prayerRepo.generateAnnualScheduleText(cityName, lat, lng, year)

                val file = File(getApplication<Application>().cacheDir, "AlHuda_Prayer_Schedule_$year.txt")
                file.writeText(textContent)

                val uri = FileProvider.getUriForFile(
                    getApplication(),
                    "${getApplication<Application>().packageName}.fileprovider",
                    file
                )

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Al-Huda Annual Prayer Schedule - $year")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                getApplication<Application>().startActivity(
                    Intent.createChooser(shareIntent, "Save or Share Prayer Schedule").apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                )
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(getApplication(), "Export: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
