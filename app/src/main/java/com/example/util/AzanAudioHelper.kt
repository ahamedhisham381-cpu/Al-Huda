package com.example.util

import android.content.Context
import android.media.MediaPlayer
import com.example.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AzanAudioHelper(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPrayerName = MutableStateFlow<String?>(null)
    val currentPrayerName: StateFlow<String?> = _currentPrayerName.asStateFlow()

    /**
     * Plays the bundled Athan clip so it works without internet access. [soundId] lets the
     * person choose "azan_short" (the full Athan recitation) or "prayer_alert" (a brief tone)
     * from Settings → Customization.
     */
    fun playAzan(prayerName: String = "Salah", soundId: String = "azan_short") {
        stop()
        _currentPrayerName.value = prayerName

        val resId = if (soundId == "prayer_alert") R.raw.prayer_alert else R.raw.azan_short
        try {
            mediaPlayer = MediaPlayer.create(context, resId)?.apply {
                setOnCompletionListener { stop() }
                setOnErrorListener { _, _, _ ->
                    stop()
                    true
                }
                _isPlaying.value = true
                start()
            }
            if (mediaPlayer == null) stop()
        } catch (_: Exception) {
            stop()
        }
    }

    fun stop() {
        try {
            mediaPlayer?.stop()
        } catch (_: Exception) {
        }
        try {
            mediaPlayer?.release()
        } catch (_: Exception) {
        }
        mediaPlayer = null
        _isPlaying.value = false
        _currentPrayerName.value = null
    }
}
