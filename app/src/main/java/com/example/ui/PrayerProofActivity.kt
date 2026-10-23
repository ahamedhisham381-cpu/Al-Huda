package com.example.ui

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.data.SettingsManager
import com.example.ui.theme.*
import com.example.util.GeminiPrayerVerifier
import com.example.util.PrayerLockState
import com.example.util.Translations
import com.example.widget.WidgetUpdater
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Captures the "proof" photo via the system camera app (no custom camera code,
 * no extra dependency) and saves it privately inside the app's own files dir.
 * Marks the prayer proven and tells PrayerLockService to drop the overlay.
 */
class PrayerProofActivity : ComponentActivity() {

    companion object {
        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        const val RECITATION_MIN_SECONDS = 20
    }

    private lateinit var settingsManager: SettingsManager
    private var pendingPhotoFile: File? = null
    private var pendingPhotoUri: Uri? = null

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && pendingPhotoFile != null) {
            onPhotoSaved()
        } else {
            onPhotoCancelled()
        }
    }

    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) launchCamera() }

    private var captured by mutableStateOf(false)
    private var cancelledOnce by mutableStateOf(false)
    private var verifying by mutableStateOf(false)
    private var rejectionReason by mutableStateOf<String?>(null)
    private var needsRecitation by mutableStateOf(false)
    private var recitationSecondsLeft by mutableStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsManager = SettingsManager(applicationContext)
        val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: "Salah"

        setContent {
            val lang by settingsManager.lang.collectAsState()
            val accentId by settingsManager.accent.collectAsState()
            ProofCaptureScreen(
                prayerName = prayerName,
                lang = lang,
                accentColor = getAccentColor(accentId),
                captured = captured,
                verifying = verifying,
                cancelledOnce = cancelledOnce,
                rejectionReason = rejectionReason,
                needsRecitation = needsRecitation,
                recitationSecondsLeft = recitationSecondsLeft,
                onCapture = { requestCameraThenLaunch() },
                onConfirmRecitation = { onRecitationConfirmed() },
                onClose = { finish() }
            )
        }
    }

    private fun requestCameraThenLaunch() {
        if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            requestCameraPermission.launch(android.Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera() {
        val dir = File(filesDir, "prayer_proof").apply { mkdirs() }
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(dir, "proof_$stamp.jpg")
        pendingPhotoFile = file
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        pendingPhotoUri = uri
        try {
            takePicture.launch(uri)
        } catch (_: Exception) {
            onPhotoCancelled()
        }
    }

    private fun onPhotoSaved() {
        val file = pendingPhotoFile ?: return
        val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: "Salah"
        rejectionReason = null
        verifying = true
        lifecycleScope.launch {
            when (val result = GeminiPrayerVerifier.verify(file, prayerName)) {
                is GeminiPrayerVerifier.Result.Approved -> onVerified(prayerName, file)
                is GeminiPrayerVerifier.Result.Rejected -> onRejected(file)
                // Can't get a verdict (no key / no network / API hiccup). Don't lock the
                // user out of their own phone over a technical failure, but don't silently
                // auto-approve either: fall back to an offline check — recite Al-Fatiha,
                // then confirm — before unblocking.
                is GeminiPrayerVerifier.Result.TechnicalError -> startRecitationFallback()
            }
        }
    }

    private fun startRecitationFallback() {
        verifying = false
        needsRecitation = true
        recitationSecondsLeft = RECITATION_MIN_SECONDS
        lifecycleScope.launch {
            while (recitationSecondsLeft > 0) {
                delay(1000)
                recitationSecondsLeft -= 1
            }
        }
    }

    private fun onRecitationConfirmed() {
        if (recitationSecondsLeft > 0) return
        val file = pendingPhotoFile ?: return
        val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: "Salah"
        needsRecitation = false
        onVerified(prayerName, file)
    }

    private fun onVerified(prayerName: String, file: File) {
        settingsManager.markPrayerProven(prayerName, file.absolutePath)
        PrayerLockState.release()
        try { WidgetUpdater.updateAll(applicationContext) } catch (_: Exception) {}
        verifying = false
        captured = true
        window.decorView.postDelayed({ finish() }, 1400)
    }

    private fun onRejected(file: File) {
        file.delete()
        pendingPhotoFile = null
        verifying = false
        cancelledOnce = true
        rejectionReason = "no_prayer_evidence"
    }

    private fun onPhotoCancelled() {
        cancelledOnce = true
        rejectionReason = null
        needsRecitation = false
        pendingPhotoFile?.delete()
        pendingPhotoFile = null
    }

}

/** The seven ayahs of Surah Al-Fatiha, bundled offline for the no-network fallback. */
private data class FatihaAyah(val arabic: String, val transliteration: String)

private val AL_FATIHA = listOf(
    FatihaAyah("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", "Bismillahir-Rahmanir-Raheem"),
    FatihaAyah("الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ", "Alhamdu lillahi Rabbil-'alameen"),
    FatihaAyah("الرَّحْمَٰنِ الرَّحِيمِ", "Ar-Rahmanir-Raheem"),
    FatihaAyah("مَالِكِ يَوْمِ الدِّينِ", "Maliki yawmid-deen"),
    FatihaAyah("إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", "Iyyaka na'budu wa iyyaka nasta'een"),
    FatihaAyah("اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ", "Ihdinas-siratal-mustaqeem"),
    FatihaAyah(
        "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ",
        "Siratal-ladhina an'amta 'alayhim ghayril-maghdoobi 'alayhim wa lad-dalleen"
    )
)

@Composable
private fun ProofCaptureScreen(
    prayerName: String,
    lang: String,
    accentColor: Color,
    captured: Boolean,
    verifying: Boolean,
    cancelledOnce: Boolean,
    rejectionReason: String?,
    needsRecitation: Boolean,
    recitationSecondsLeft: Int,
    onCapture: () -> Unit,
    onConfirmRecitation: () -> Unit,
    onClose: () -> Unit
) {
    val t = Translations.get(lang)
    val isAr = lang == "ar"
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
            verticalArrangement = Arrangement.Center
        ) {
            if (needsRecitation) {
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.WifiOff, contentDescription = null, tint = accentColor, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (isAr) "لا يوجد اتصال بالإنترنت" else "No internet connection",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isAr) "اقرأ سورة الفاتحة بصوت مسموع، ثم أكّد ذلك أدناه."
                        else "Recite Surah Al-Fatiha out loud, then confirm below.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = GlassWhite05),
                        border = BorderStroke(1.dp, BorderWhite10)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            AL_FATIHA.forEachIndexed { index, ayah ->
                                Text(
                                    text = ayah.arabic,
                                    color = Color.White,
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.End,
                                    lineHeight = 32.sp,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    text = ayah.transliteration,
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    fontStyle = FontStyle.Italic,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                if (index != AL_FATIHA.lastIndex) Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onConfirmRecitation,
                        enabled = recitationSecondsLeft <= 0,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                    ) {
                        Text(
                            text = if (recitationSecondsLeft > 0) {
                                (if (isAr) "انتظر " else "Wait ") + recitationSecondsLeft + "s"
                            } else {
                                if (isAr) "لقد قرأتها، فتح القفل" else "I've recited it — unlock"
                            },
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            } else if (verifying) {
                CircularProgressIndicator(color = accentColor, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = if (isAr) "جارٍ التحقق من الصورة..." else "Verifying your photo...",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            } else if (captured) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = accentColor, modifier = Modifier.size(72.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (isAr) "تقبل الله صلاتك" else "May your prayer be accepted",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isAr) "تم إلغاء القفل" else "Your phone is unlocked",
                    color = TextMuted,
                    fontSize = 13.sp
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(accentColor.copy(alpha = 0.18f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = accentColor, modifier = Modifier.size(48.dp))
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = (if (isAr) "صورة إثبات صلاة " else "Proof photo for ") + prayerLabel,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isAr) "التقط صورة لسجادة الصلاة على الأرض لتأكيد أنك صليت. تتحقق أداة ذكاء اصطناعي من الصورة بسرعة، وهي تُحفظ على جهازك فقط."
                    else "Take a photo of your prayer mat on the floor to confirm you prayed. An AI check quickly looks it over, and the photo stays only on your device.",
                    color = TextMuted,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 19.sp
                )
                if (cancelledOnce) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (rejectionReason == "no_prayer_evidence") {
                            if (isAr) "لم نتمكن من رؤية أي دليل على الصلاة في هذه الصورة. حاول مرة أخرى."
                            else "We couldn't spot anything prayer-related in that photo. Try again."
                        } else {
                            if (isAr) "لم يتم حفظ صورة. حاول مرة أخرى." else "No photo was saved. Try again."
                        },
                        color = Rose500,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(28.dp))
                Button(
                    onClick = onCapture,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Text(
                        text = if (isAr) "فتح الكاميرا" else "Open Camera",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                TextButton(onClick = onClose) {
                    Text(text = if (isAr) "إلغاء" else "Cancel", color = TextMuted)
                }
            }
        }
    }
}
