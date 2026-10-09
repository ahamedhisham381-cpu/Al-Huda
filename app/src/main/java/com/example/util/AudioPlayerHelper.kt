package com.example.util

import android.media.AudioAttributes
import android.media.MediaPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AudioPlayerHelper {
    private var mediaPlayer: MediaPlayer? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentTrackTitle = MutableStateFlow<String?>(null)
    val currentTrackTitle: StateFlow<String?> = _currentTrackTitle.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun playAudio(url: String, title: String) {
        stop()
        _isLoading.value = true
        _currentTrackTitle.value = title

        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(url)
                setOnPreparedListener {
                    _isLoading.value = false
                    _isPlaying.value = true
                    it.start()
                }
                setOnCompletionListener {
                    _isPlaying.value = false
                    _currentTrackTitle.value = null
                }
                setOnErrorListener { _, _, _ ->
                    _isLoading.value = false
                    _isPlaying.value = false
                    _currentTrackTitle.value = null
                    true
                }
                prepareAsync()
            }
        } catch (_: Exception) {
            _isLoading.value = false
            _isPlaying.value = false
        }
    }

    fun pause() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                _isPlaying.value = false
            }
        } catch (_: Exception) {}
    }

    fun resume() {
        try {
            if (mediaPlayer != null && !_isPlaying.value) {
                mediaPlayer?.start()
                _isPlaying.value = true
            }
        } catch (_: Exception) {}
    }

    fun stop() {
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer?.isPlaying == true) {
                    mediaPlayer?.stop()
                }
                mediaPlayer?.release()
                mediaPlayer = null
            }
        } catch (_: Exception) {}
        _isPlaying.value = false
        _isLoading.value = false
        _currentTrackTitle.value = null
    }
}
