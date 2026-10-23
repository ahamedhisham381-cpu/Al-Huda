package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SettingsManager
import com.example.ui.theme.*
import com.example.util.Translations
import kotlinx.coroutines.delay

/**
 * The full-screen content drawn above every other app by PrayerLockService.
 * Honest about what it is: an honor-system accountability screen, not AI
 * verification that a prayer mat is actually on the floor.
 */
@Composable
fun PrayerLockOverlayScreen(
    prayerName: String,
    deadlineMillis: Long,
    settingsManager: SettingsManager,
    onOpenProofCapture: () -> Unit,
    onEmergencyCall: () -> Unit
) {
    val lang by settingsManager.lang.collectAsState()
    val accentId by settingsManager.accent.collectAsState()
    val accentColor = getAccentColor(accentId)
    val t = Translations.get(lang)
    val isAr = lang == "ar"

    var remainingMs by remember { mutableStateOf((deadlineMillis - System.currentTimeMillis()).coerceAtLeast(0L)) }
    LaunchedEffect(deadlineMillis) {
        while (remainingMs > 0) {
            delay(1000)
            remainingMs = (deadlineMillis - System.currentTimeMillis()).coerceAtLeast(0)
        }
    }
    val minutes = remainingMs / 60000
    val seconds = (remainingMs / 1000) % 60

    val prayerLabel = when (prayerName) {
        "Fajr" -> t.fajr
        "Dhuhr" -> t.dhuhr
        "Asr" -> t.asr
        "Maghrib" -> t.maghrib
        "Isha" -> t.isha
        else -> prayerName
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgSlate950, SurfaceSlate900, BgSlate950))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🔒", fontSize = 40.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (isAr) "القفل قيد التفعيل" else "Prayer Lock is on",
                    color = TextMuted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isAr) "صلّ $prayerLabel ثم التقط صورة لإثبات ذلك"
                    else "Pray $prayerLabel, then take a photo to prove it",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    lineHeight = 30.sp
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isAr) "هذا نظام شرف — صورة توثّق صلاتك، وليس تحققاً آلياً."
                    else "This is an honor system — the photo is a personal log, not AI-verified proof.",
                    color = TextSubtle,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (isAr) "يُفتح تلقائياً خلال ${minutes}:${seconds.toString().padStart(2, '0')}"
                    else "Auto-unlocks in ${minutes}:${seconds.toString().padStart(2, '0')}",
                    color = TextSubtle,
                    fontSize = 12.sp
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onOpenProofCapture,
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isAr) "التقط صورة الآن" else "I've prayed — take photo",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                TextButton(onClick = onEmergencyCall) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isAr) "اتصال طارئ" else "Emergency call",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
