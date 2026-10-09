package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.media.ToneGenerator
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class AudioHelper(private val context: Context) {
    private var isSoundEnabled: Boolean = true
    private var vibrator: Vibrator? = null
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (_: Exception) {}

        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 60)
        } catch (_: Exception) {}
    }

    fun setSoundEnabled(enabled: Boolean) {
        isSoundEnabled = enabled
    }

    fun getSoundEnabled(): Boolean = isSoundEnabled

    fun playTick() {
        vibrate(25L)
        if (!isSoundEnabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 30)
        } catch (_: Exception) {}
    }

    /**
     * Dedicated, crisp wooden bead sound for EVERY dhikr count tap.
     */
    fun playBeadClick() {
        vibrate(20L)
        if (!isSoundEnabled) return
        try {
            // Short high-fidelity pleasant click tone
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 35)
        } catch (_: Exception) {}
    }

    fun playChime() {
        vibrate(longArrayOf(0, 40, 60, 40))
        if (!isSoundEnabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 120)
        } catch (_: Exception) {}
    }

    fun playTabChange() {
        vibrate(15L)
        if (!isSoundEnabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 25)
        } catch (_: Exception) {}
    }

    private fun vibrate(durationMs: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    private fun vibrate(pattern: LongArray) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, -1)
            }
        } catch (_: Exception) {}
    }
}
