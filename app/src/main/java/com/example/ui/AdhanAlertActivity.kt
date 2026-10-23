package com.example.ui

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SettingsManager
import com.example.receiver.PrayerNotificationReceiver
import com.example.ui.theme.*
import com.example.util.AzanAudioHelper
import com.example.util.Translations
import java.text.SimpleDateFormat
import java.util.*

/**
 * Full-screen Adhan alert: shows over the lock screen when a prayer time arrives,
 * the way a phone call or alarm clock does, so the alert can't be missed or
 * mistaken for an ordinary notification. Keeps playing the Azan until the
 * person stops it or snoozes for 10 minutes.
 */
class AdhanAlertActivity : ComponentActivity() {

    companion object {
        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        const val EXTRA_CITY_NAME = "extra_city_name"
    }

    private var azanHelper: AzanAudioHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showOverLockScreen()

        val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: "Salah"
        val cityName = intent.getStringExtra(EXTRA_CITY_NAME) ?: ""
        val settingsManager = SettingsManager(applicationContext)

        azanHelper = AzanAudioHelper(applicationContext).also {
            try { it.playAzan(prayerName, settingsManager.alertSound.value) } catch (_: Exception) {}
        }

        setContent {
            val lang by settingsManager.lang.collectAsState()
            val accentId by settingsManager.accent.collectAsState()
            AdhanAlertScreen(
                prayerName = prayerName,
                cityName = cityName,
                lang = lang,
                accentColor = getAccentColor(accentId),
                onStop = { stopAndFinish() },
                onSnooze = { snoozeAndFinish(prayerName, cityName) }
            )
        }

        // Safety auto-dismiss: don't let the alert (and the Azan audio) run forever
        window.decorView.postDelayed({ if (!isFinishing) stopAndFinish() }, 3 * 60 * 1000L)
    }

    private fun showOverLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val km = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            km?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
    }

    private fun stopAndFinish() {
        try { azanHelper?.stop() } catch (_: Exception) {}
        finishAndRemoveTask()
    }

    private fun snoozeAndFinish(prayerName: String, cityName: String) {
        try { azanHelper?.stop() } catch (_: Exception) {}
        val intent = Intent(this, PrayerNotificationReceiver::class.java).apply {
            action = PrayerNotificationReceiver.ACTION_SNOOZE_PRAYER_ALERT
            putExtra(EXTRA_PRAYER_NAME, prayerName)
            putExtra(EXTRA_CITY_NAME, cityName)
        }
        sendBroadcast(intent)
        finishAndRemoveTask()
    }

    override fun onDestroy() {
        super.onDestroy()
        try { azanHelper?.stop() } catch (_: Exception) {}
    }
}

@Composable
private fun AdhanAlertScreen(
    prayerName: String,
    cityName: String,
    lang: String,
    accentColor: Color,
    onStop: () -> Unit,
    onSnooze: () -> Unit
) {
    val t = Translations.get(lang)
    val isAr = lang == "ar"
    val infinite = rememberInfiniteTransition(label = "ripple")
    val rippleScale by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(tween(1800), RepeatMode.Restart),
        label = "rippleScale"
    )
    val rippleAlpha by infinite.animateFloat(
        initialValue = 0.35f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1800), RepeatMode.Restart),
        label = "rippleAlpha"
    )

    val now = remember { SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date()) }
    val prayerDisplayName = translatedPrayerName(prayerName, t)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(BgSlate950, SurfaceSlate900, BgSlate950))
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isAr) "حان وقت الصلاة" else "It's time for prayer",
                    color = TextMuted,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = prayerDisplayName,
                    color = Color.White,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
                if (cityName.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "$cityName • $now", color = TextSubtle, fontSize = 14.sp)
                }
            }

            // Pulsing ripple around a crescent/mosque-style glyph
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(220.dp)) {
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .scale(rippleScale)
                        .background(accentColor.copy(alpha = rippleAlpha), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .background(accentColor.copy(alpha = 0.16f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(118.dp)
                        .background(accentColor.copy(alpha = 0.28f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🕌", fontSize = 48.sp)
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "«حَيَّ عَلَى الصَّلَاة»",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(28.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedButton(
                        onClick = onSnooze,
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.Snooze, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isAr) "تأجيل 10 د" else "Snooze 10m", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onStop,
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                    ) {
                        Icon(Icons.Default.NotificationsOff, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isAr) "إيقاف" else "Stop", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }
        }
    }
}

private fun translatedPrayerName(name: String, t: com.example.util.AppTranslations): String = when (name) {
    "Fajr" -> t.fajr
    "Dhuhr" -> t.dhuhr
    "Asr" -> t.asr
    "Maghrib" -> t.maghrib
    "Isha" -> t.isha
    else -> name
}
