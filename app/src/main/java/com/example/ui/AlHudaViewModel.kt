package com.example.ui

import android.app.Application
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.model.*
import com.example.util.*
import kotlinx.coroutines.Dispatchers
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

    val prayerRepo = PrayerRepository()
    val quranRepo = QuranRepository()
    val spiritualRepo = SpiritualRepository()
    val eduRepo = EducationAndWorshipRepository()
    val settingsManager = SettingsManager(application)
    val audioHelper = AudioHelper(application)
    val qiblaHelper = QiblaHelper(application)
    val audioPlayer = AudioPlayerHelper()
    val azanAudio = AzanAudioHelper(application)

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

        val (calcTimes, calcHijri) = prayerRepo.calculateOfflinePrayerTimes(lat, lng)
        _prayerTimes.value = calcTimes
        _hijriDate.value = calcHijri
        determineNextPrayer(calcTimes)

        // Schedule exact alarms
        PrayerNotificationScheduler.scheduleAllPrayerAlerts(
            getApplication(),
            calcTimes,
            settingsManager.city.value,
            settingsManager.notificationsEnabled.value
        )

        viewModelScope.launch {
            val onlineResult = prayerRepo.getPrayerTimesOnline(lat, lng)
            if (onlineResult != null) {
                _prayerTimes.value = onlineResult.first
                _hijriDate.value = onlineResult.second
                determineNextPrayer(onlineResult.first)
                _isOfflineMode.value = false

                // Reschedule with refined online timings
                PrayerNotificationScheduler.scheduleAllPrayerAlerts(
                    getApplication(),
                    onlineResult.first,
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
        val found = prayerRepo.searchCity(query)
        if (found != null) {
            settingsManager.setLocation(found.name, found.latitude, found.longitude)
            loadPrayerTimes()
            _citySearchQuery.value = ""
            audioHelper.playTick()
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
        _isLoadingAyahs.value = true
        viewModelScope.launch {
            val list = quranRepo.getAyahsForSurah(surah.number)
            _ayahs.value = list
            _isLoadingAyahs.value = false
        }
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
