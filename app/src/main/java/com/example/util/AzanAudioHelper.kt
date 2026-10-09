package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.ToneGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AzanAudioHelper(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var toneGenerator: ToneGenerator? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPrayerName = MutableStateFlow<String?>(null)
    val currentPrayerName: StateFlow<String?> = _currentPrayerName.asStateFlow()

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 80)
        } catch (_: Exception) {}
    }

    /**
     * Plays the authentic Azan recitation.
     * Uses primary high-quality Azan audio with offline fallback melody.
     */
    fun playAzan(prayerName: String = "Salah") {
        stop()
        _currentPrayerName.value = prayerName
        _isPlaying.value = true

        val azanUrl = "https://www.islamcan.com/audio/adhan/azan1.mp3"

        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
                setDataSource(azanUrl)
                setOnPreparedListener {
                    it.start()
                }
                setOnCompletionListener {
                    stop()
                }
                setOnErrorListener { _, _, _ ->
                    // Fallback to offline melodic chimes if network is unavailable
                    playOfflineAzanTones()
                    true
                }
                prepareAsync()
            }
        } catch (_: Exception) {
            playOfflineAzanTones()
        }
    }

    private fun playOfflineAzanTones() {
        try {
            Thread {
                try {
                    // Play majestic sequence representing Takbeer: Allahu Akbar (repeated twice)
                    for (i in 1..4) {
                        toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 500)
                        Thread.sleep(600)
                        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 800)
                        Thread.sleep(1000)
                    }
                } catch (_: Exception) {} finally {
                    _isPlaying.value = false
                    _currentPrayerName.value = null
                }
            }.start()
        } catch (_: Exception) {
            _isPlaying.value = false
            _currentPrayerName.value = null
        }
    }

    fun stop() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (_: Exception) {}
        _isPlaying.value = false
        _currentPrayerName.value = null
    }
}
