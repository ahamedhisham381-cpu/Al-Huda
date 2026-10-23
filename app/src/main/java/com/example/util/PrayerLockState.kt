package com.example.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Shared in-process signal between PrayerLockService (which draws the blocking
 * overlay) and PrayerProofActivity (which captures the photo). Kept as a plain
 * singleton rather than a Binder service since both run in the same process.
 */
object PrayerLockState {
    private val _isLocked = MutableStateFlow(false)
    val isLocked = _isLocked.asStateFlow()

    private val _lockedPrayerName = MutableStateFlow<String?>(null)
    val lockedPrayerName = _lockedPrayerName.asStateFlow()

    private val _lockDeadlineMillis = MutableStateFlow(0L)
    val lockDeadlineMillis = _lockDeadlineMillis.asStateFlow()

    fun engage(prayerName: String, deadlineMillis: Long) {
        _lockedPrayerName.value = prayerName
        _lockDeadlineMillis.value = deadlineMillis
        _isLocked.value = true
    }

    fun release() {
        _isLocked.value = false
        _lockedPrayerName.value = null
        _lockDeadlineMillis.value = 0L
    }
}
