package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.R
import com.example.data.SettingsManager
import com.example.ui.PrayerLockOverlayScreen
import com.example.util.PrayerLockState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Draws a full-screen blocking overlay above every other app until the current
 * prayer is marked "proven" (photo captured) or the lock's safety deadline passes.
 *
 * Honest limitation: a regular app cannot truly disable a phone (that needs
 * Device Owner / MDM provisioning). This overlay blocks touches to whatever is
 * underneath while "Display over other apps" permission is granted; the person
 * can always escape by revoking that permission in Android Settings, which is
 * the standard, expected override for this category of self-discipline app.
 */
class PrayerLockService : Service(), LifecycleOwner, SavedStateRegistryOwner, ViewModelStoreOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry
    private val savedStateController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry get() = savedStateController.savedStateRegistry
    override val viewModelStore = ViewModelStore()

    private var windowManager: WindowManager? = null
    private var overlayView: ComposeView? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main.immediate + Job())

    companion object {
        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        const val CHANNEL_ID = "prayer_lock_channel"
        private const val LOCK_WINDOW_MS = 45 * 60 * 1000L // auto-release safety cap: 45 minutes
        private const val NOTIF_ID = 9901

        fun start(context: Context, prayerName: String) {
            val intent = Intent(context, PrayerLockService::class.java).apply {
                putExtra(EXTRA_PRAYER_NAME, prayerName)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        savedStateController.performAttach()
        savedStateController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val prayerName = intent?.getStringExtra(EXTRA_PRAYER_NAME) ?: return START_NOT_STICKY
        val settingsManager = SettingsManager(applicationContext)

        // Already proven (e.g. captured right after the full-screen Adhan alert)? Nothing to do.
        if (!settingsManager.strictLockEnabled.value || settingsManager.isPrayerProven(prayerName)) {
            stopSelf()
            return START_NOT_STICKY
        }

        startForeground(NOTIF_ID, buildOngoingNotification(prayerName))

        val deadline = System.currentTimeMillis() + LOCK_WINDOW_MS
        PrayerLockState.engage(prayerName, deadline)
        showOverlay(prayerName, deadline, settingsManager)

        serviceScope.launch {
            PrayerLockState.isLocked.collectLatest { locked ->
                if (!locked) {
                    removeOverlay()
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
        }

        // Safety auto-release so the phone is never blocked indefinitely
        serviceScope.launch {
            kotlinx.coroutines.delay(LOCK_WINDOW_MS)
            if (PrayerLockState.isLocked.value) PrayerLockState.release()
        }

        return START_STICKY
    }

    private fun showOverlay(prayerName: String, deadlineMillis: Long, settingsManager: SettingsManager) {
        if (overlayView != null) return
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            type,
            WindowManager.LayoutParams.FLAG_FULLSCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.CENTER }

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@PrayerLockService)
            setViewTreeSavedStateRegistryOwner(this@PrayerLockService)
            setViewTreeViewModelStoreOwner(this@PrayerLockService)
            setContent {
                PrayerLockOverlayScreen(
                    prayerName = prayerName,
                    deadlineMillis = deadlineMillis,
                    settingsManager = settingsManager,
                    onOpenProofCapture = { openProofCapture(prayerName) },
                    onEmergencyCall = { openDialer() }
                )
            }
        }
        overlayView = composeView
        try {
            windowManager?.addView(composeView, params)
        } catch (_: Exception) {
            // SYSTEM_ALERT_WINDOW not granted: can't block, so don't hold the phone hostage silently
            stopSelf()
        }
    }

    private fun removeOverlay() {
        try { overlayView?.let { windowManager?.removeView(it) } } catch (_: Exception) {}
        overlayView = null
    }

    private fun openProofCapture(prayerName: String) {
        val intent = Intent(this, com.example.ui.PrayerProofActivity::class.java).apply {
            putExtra(com.example.ui.PrayerProofActivity.EXTRA_PRAYER_NAME, prayerName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }

    private fun openDialer() {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        } catch (_: Exception) {}
    }

    private fun buildOngoingNotification(prayerName: String): android.app.Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Prayer Lock", NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Shown while the prayer accountability lock is active" }
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }
        val openApp = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Prayer Lock active · $prayerName")
            .setContentText("Capture a photo once you've prayed to unlock your phone.")
            .setOngoing(true)
            .setContentIntent(openApp)
            .build()
    }

    override fun onDestroy() {
        removeOverlay()
        try { serviceScope.coroutineContext[Job]?.cancel() } catch (_: Exception) {}
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
