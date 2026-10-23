package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*
import com.example.util.Translations

@Composable
fun SettingsDialog(
    viewModel: AlHudaViewModel,
    onDismiss: () -> Unit
) {
    val currentTheme by viewModel.settingsManager.theme.collectAsState()
    val currentAccent by viewModel.settingsManager.accent.collectAsState()
    val currentLang by viewModel.settingsManager.lang.collectAsState()
    val soundsEnabled by viewModel.settingsManager.soundsEnabled.collectAsState()
    val notifsEnabled by viewModel.settingsManager.notificationsEnabled.collectAsState()
    val strictLockEnabled by viewModel.settingsManager.strictLockEnabled.collectAsState()
    var showCreditsScreen by remember { mutableStateOf(false) }

    val t = Translations.get(currentLang)
    val accentColor = getAccentColor(currentAccent)
    val context = LocalContext.current
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // Granted -> uses GPS/fused location. Denied -> falls back to IP-based location.
        viewModel.autoLocate()
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> /* permission result is only needed again at capture time */ }
    val overlaySettingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        if (android.provider.Settings.canDrawOverlays(context)) {
            viewModel.settingsManager.setStrictLockEnabled(true)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("settings_dialog"),
            color = MaterialTheme.colorScheme.background
        ) {
            if (showCreditsScreen) {
                CreditsScreen(currentLang = currentLang, accentColor = accentColor, onBack = { showCreditsScreen = false })
            } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = t.settings,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(38.dp)
                            .background(GlassWhite10, CircleShape)
                            .testTag("settings_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Scrollable settings body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(26.dp)
                ) {
                    // Location Section
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "PRAYER LOCATION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = TextMuted
                        )
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = GlassWhite05),
                            border = BorderStroke(1.dp, BorderWhite10)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = viewModel.settingsManager.city.collectAsState().value,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = if (currentLang == "ar") "لمواقيت الصلاة الدقيقة حسب موقعك" else "Used for accurate daily prayer times",
                                        fontSize = 12.sp,
                                        color = TextMuted
                                    )
                                }
                                Button(
                                    onClick = {
                                        val hasLocation = ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.ACCESS_FINE_LOCATION
                                        ) == PackageManager.PERMISSION_GRANTED
                                        if (hasLocation) viewModel.autoLocate()
                                        else locationPermissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                                Manifest.permission.ACCESS_FINE_LOCATION
                                            )
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                                    shape = RoundedCornerShape(14.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                    modifier = Modifier.testTag("auto_locate_button")
                                ) {
                                    Text(t.autoLocate, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Language Section
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = t.language.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = TextMuted
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // English
                            val isEn = currentLang == "en"
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(22.dp))
                                    .clickable {
                                        viewModel.settingsManager.setLanguage("en")
                                        viewModel.audioHelper.playTick()
                                    }
                                    .testTag("language_en_button"),
                                shape = RoundedCornerShape(22.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isEn) GlassWhite15 else GlassWhite05
                                ),
                                border = BorderStroke(2.dp, if (isEn) Color.White else Color.Transparent)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("English", fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("Global", fontSize = 11.sp, color = TextMuted)
                                }
                            }

                            // Arabic
                            val isAr = currentLang == "ar"
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(22.dp))
                                    .clickable {
                                        viewModel.settingsManager.setLanguage("ar")
                                        viewModel.audioHelper.playTick()
                                    }
                                    .testTag("language_ar_button"),
                                shape = RoundedCornerShape(22.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isAr) GlassWhite15 else GlassWhite05
                                ),
                                border = BorderStroke(2.dp, if (isAr) Color.White else Color.Transparent)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("العربية", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("اللغة الرسمية", fontSize = 10.sp, color = TextMuted)
                                }
                            }
                        }

                        // Additional Languages Row (Urdu, French, Indonesian)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                Triple("ur", "اردو", "Urdu"),
                                Triple("fr", "Français", "French"),
                                Triple("id", "Indonesia", "Bahasa")
                            ).forEach { (code, nativeName, _) ->
                                val isSelected = currentLang == code
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isSelected) GlassWhite15 else GlassWhite05,
                                    border = BorderStroke(1.5.dp, if (isSelected) Color.White else BorderWhite10),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable {
                                            viewModel.settingsManager.setLanguage(code)
                                            viewModel.audioHelper.playTick()
                                        }
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(nativeName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }

                    // Madhab & Calculation Settings (Feature 30)
                    val currentMadhab by viewModel.settingsManager.madhab.collectAsState()
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = t.madhabTitle.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = TextMuted
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val isShafii = currentMadhab == "shafii"
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable {
                                        viewModel.settingsManager.setMadhab("shafii")
                                        viewModel.loadPrayerTimes()
                                        viewModel.audioHelper.playTick()
                                    },
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = if (isShafii) GlassWhite15 else GlassWhite05),
                                border = BorderStroke(2.dp, if (isShafii) accentColor else Color.Transparent)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Standard / Shafi'i", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("Standard Asr shadow ratio", fontSize = 10.sp, color = TextMuted)
                                }
                            }

                            val isHanafi = currentMadhab == "hanafi"
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable {
                                        viewModel.settingsManager.setMadhab("hanafi")
                                        viewModel.loadPrayerTimes()
                                        viewModel.audioHelper.playTick()
                                    },
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = if (isHanafi) GlassWhite15 else GlassWhite05),
                                border = BorderStroke(2.dp, if (isHanafi) accentColor else Color.Transparent)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Hanafi", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("Double shadow Asr ratio", fontSize = 10.sp, color = TextMuted)
                                }
                            }
                        }
                    }

                    // Alerts & Media Section
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "ALERTS & MEDIA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = TextMuted
                        )

                        // Sounds Toggle
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = GlassWhite05),
                            border = BorderStroke(1.dp, BorderWhite10)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(t.sounds, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(t.soundsDesc, fontSize = 12.sp, color = TextMuted)
                                }
                                Switch(
                                    checked = soundsEnabled,
                                    onCheckedChange = {
                                        viewModel.settingsManager.setSounds(it)
                                        viewModel.audioHelper.playTick()
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = accentColor,
                                        uncheckedThumbColor = Color.White,
                                        uncheckedTrackColor = SurfaceSlate800
                                    ),
                                    modifier = Modifier.testTag("sounds_switch")
                                )
                            }
                        }

                        // Notifications Toggle
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = GlassWhite05),
                            border = BorderStroke(1.dp, BorderWhite10)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(t.notifications, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(t.notificationsDesc, fontSize = 12.sp, color = TextMuted)
                                }
                                Switch(
                                    checked = notifsEnabled,
                                    onCheckedChange = {
                                        viewModel.settingsManager.setNotifications(it)
                                        viewModel.audioHelper.playTick()
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = accentColor,
                                        uncheckedThumbColor = Color.White,
                                        uncheckedTrackColor = SurfaceSlate800
                                    ),
                                    modifier = Modifier.testTag("notifications_switch")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Prayer Accountability Lock Toggle
                        Card(
                            modifier = Modifier.fillMaxWidth().testTag("strict_lock_card"),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = GlassWhite05),
                            border = BorderStroke(1.dp, BorderWhite10)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (currentLang == "ar") "🔒 قفل مساءلة الصلاة" else "🔒 Prayer Accountability Lock",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = if (currentLang == "ar")
                                                "يقفل الشاشة عند دخول وقت الصلاة حتى تلتقط صورة تثبت أنك صليت"
                                            else
                                                "Blocks your screen when a prayer's time starts, until you take a photo proving you prayed",
                                            fontSize = 12.sp,
                                            color = TextMuted
                                        )
                                    }
                                    Switch(
                                        checked = strictLockEnabled,
                                        onCheckedChange = { enable ->
                                            viewModel.audioHelper.playTick()
                                            if (enable) {
                                                if (android.provider.Settings.canDrawOverlays(context)) {
                                                    viewModel.settingsManager.setStrictLockEnabled(true)
                                                } else {
                                                    val intent = android.content.Intent(
                                                        android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                        android.net.Uri.parse("package:${context.packageName}")
                                                    )
                                                    overlaySettingsLauncher.launch(intent)
                                                }
                                                if (androidx.core.content.ContextCompat.checkSelfPermission(
                                                        context, Manifest.permission.CAMERA
                                                    ) != PackageManager.PERMISSION_GRANTED
                                                ) {
                                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                                }
                                            } else {
                                                viewModel.settingsManager.setStrictLockEnabled(false)
                                            }
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = Rose500,
                                            uncheckedThumbColor = Color.White,
                                            uncheckedTrackColor = SurfaceSlate800
                                        ),
                                        modifier = Modifier.testTag("strict_lock_switch")
                                    )
                                }
                                if (strictLockEnabled) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = if (currentLang == "ar")
                                            "نظام شرف: الصورة تُحفظ على جهازك فقط ولا يتم التحقق منها آلياً. يُفتح القفل تلقائياً خلال 45 دقيقة، ويبقى الاتصال الطارئ متاحاً دائماً."
                                        else
                                            "Honor system: the photo stays on your device only and isn't AI-verified. Always auto-unlocks within 45 minutes, and emergency calling stays available.",
                                        fontSize = 11.sp,
                                        color = TextSubtle,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = if (currentLang == "ar") "🎨 التخصيص" else "🎨 Customization",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = TextMuted,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        CustomizationSection(viewModel = viewModel, currentLang = currentLang, accentColor = accentColor)

                        // Test Athan & Notification Button
                        OutlinedButton(
                            onClick = { viewModel.triggerTestPrayerNotification() },
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth().testTag("test_azan_button")
                        ) {
                            Text("🔊 Test Athan & Prayer Notification", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = accentColor)
                        }
                    }

                    // Accent Color Section
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = t.accentColor.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = TextMuted
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(26.dp),
                            colors = CardDefaults.cardColors(containerColor = GlassWhite05),
                            border = BorderStroke(1.dp, BorderWhite10)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    AvailableAccents.take(4).forEach { option ->
                                        AccentSwatchItem(
                                            option = option,
                                            isSelected = currentAccent == option.id,
                                            onSelect = {
                                                viewModel.settingsManager.setAccent(option.id)
                                                viewModel.audioHelper.playTick()
                                            }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    AvailableAccents.drop(4).take(4).forEach { option ->
                                        AccentSwatchItem(
                                            option = option,
                                            isSelected = currentAccent == option.id,
                                            onSelect = {
                                                viewModel.settingsManager.setAccent(option.id)
                                                viewModel.audioHelper.playTick()
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Theme Selection Section
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = t.theme.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = TextMuted
                        )

                        AvailableThemes.forEach { theme ->
                            val isSelected = currentTheme == theme.id
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(22.dp))
                                    .clickable {
                                        viewModel.settingsManager.setTheme(theme.id)
                                        viewModel.audioHelper.playTick()
                                    }
                                    .testTag("theme_option_${theme.id}"),
                                shape = RoundedCornerShape(22.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) GlassWhite15 else GlassWhite05
                                ),
                                border = BorderStroke(2.dp, if (isSelected) Color.White else Color.Transparent)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .background(theme.defaultAccent, RoundedCornerShape(12.dp))
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(theme.name, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .background(Color.White, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = Color.Black,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // About Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = GlassWhite05),
                        border = BorderStroke(1.dp, BorderWhite10)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text("About Al-Huda", fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Al-Huda is your offline-first Islamic companion for prayer times, spiritual wisdom, Holy Quran reading, and daily Dhikr. Made with reverence and precision.",
                                fontSize = 12.sp,
                                color = TextMuted,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Version 1.0.0 (Native Android Edition)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = accentColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Credits Card (empty for now — reserved for contributor/attribution credits)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCreditsScreen = true }
                            .testTag("credits_row"),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = GlassWhite05),
                        border = BorderStroke(1.dp, BorderWhite10)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (currentLang == "ar") "الشكر والتقدير" else "Credits",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                // Save & Exit button
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("save_and_exit_button"),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = t.saveExit,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
            }
        }
    }
}

@Composable
fun AccentSwatchItem(
    option: AccentOption,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(option.color)
            .clickable { onSelect() }
            .then(
                if (isSelected) BorderStroke(3.dp, Color.White).let {
                    Modifier.background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                } else Modifier
            )
            .testTag("accent_${option.id}"),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * WeMuslim-style customization: Quran reading theme + font size, prayer-time
 * fine-tuning per prayer, a calculation-method override, and the alert sound.
 */
@Composable
private fun CustomizationSection(viewModel: AlHudaViewModel, currentLang: String, accentColor: Color) {
    val isAr = currentLang == "ar"
    val quranTheme by viewModel.settingsManager.quranReadingTheme.collectAsState()
    val fontScale by viewModel.settingsManager.quranFontScale.collectAsState()
    val offsets by viewModel.settingsManager.prayerOffsets.collectAsState()
    val methodOverride by viewModel.settingsManager.calculationMethodOverride.collectAsState()
    val alertSound by viewModel.settingsManager.alertSound.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

        // Quran Reading Theme
        CustomizationCard(title = if (isAr) "ثيم قراءة القرآن" else "Quran Reading Theme") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuranReadingThemes.forEach { themeOption ->
                    val selected = themeOption.id == quranTheme
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(themeOption.pageBackground)
                            .border(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) accentColor else BorderWhite10,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { viewModel.settingsManager.setQuranReadingTheme(themeOption.id) }
                            .testTag("quran_theme_${themeOption.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ا",
                            color = themeOption.arabicText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = QuranReadingThemes.firstOrNull { it.id == quranTheme }?.label ?: "Default",
                fontSize = 11.sp,
                color = TextMuted
            )
        }

        // Quran Font Size
        CustomizationCard(title = if (isAr) "حجم خط القرآن" else "Quran Font Size") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("A", fontSize = 12.sp, color = TextMuted)
                Slider(
                    value = fontScale,
                    onValueChange = { viewModel.settingsManager.setQuranFontScale(it) },
                    valueRange = 0.75f..1.75f,
                    modifier = Modifier.weight(1f).padding(horizontal = 10.dp).testTag("quran_font_slider"),
                    colors = SliderDefaults.colors(thumbColor = accentColor, activeTrackColor = accentColor)
                )
                Text("A", fontSize = 20.sp, color = TextMuted, fontWeight = FontWeight.Bold)
            }
        }

        // Prayer Time Fine-Tuning
        CustomizationCard(title = if (isAr) "ضبط أوقات الصلاة (دقائق)" else "Prayer Time Fine-Tuning (minutes)") {
            val prayerLabels = listOf("Fajr" to "الفجر", "Dhuhr" to "الظهر", "Asr" to "العصر", "Maghrib" to "المغرب", "Isha" to "العشاء")
            prayerLabels.forEach { (key, arLabel) ->
                val value = offsets[key] ?: 0
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (isAr) arLabel else key, fontSize = 13.sp, color = Color.White, modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = { viewModel.setPrayerOffset(key, value - 1) },
                        modifier = Modifier.size(32.dp).background(GlassWhite10, CircleShape)
                    ) { Text("−", color = Color.White, fontWeight = FontWeight.Bold) }
                    Text(
                        text = if (value > 0) "+$value" else "$value",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (value == 0) TextMuted else accentColor,
                        modifier = Modifier.width(40.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    IconButton(
                        onClick = { viewModel.setPrayerOffset(key, value + 1) },
                        modifier = Modifier.size(32.dp).background(GlassWhite10, CircleShape)
                    ) { Text("+", color = Color.White, fontWeight = FontWeight.Bold) }
                }
            }
        }

        // Calculation Method Override
        CustomizationCard(title = if (isAr) "طريقة حساب أوقات الصلاة" else "Calculation Method") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (methodOverride == null) accentColor.copy(alpha = 0.18f) else Color.Transparent)
                        .clickable { viewModel.setCalculationMethod(null) }
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = methodOverride == null,
                        onClick = { viewModel.setCalculationMethod(null) },
                        colors = RadioButtonDefaults.colors(selectedColor = accentColor)
                    )
                    Text(
                        if (isAr) "تلقائي (حسب بلدك)" else "Automatic (by your country)",
                        fontSize = 13.sp, color = Color.White
                    )
                }
                viewModel.prayerRepo.manualMethods.forEach { (id, name) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (methodOverride == id) accentColor.copy(alpha = 0.18f) else Color.Transparent)
                            .clickable { viewModel.setCalculationMethod(id) }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = methodOverride == id,
                            onClick = { viewModel.setCalculationMethod(id) },
                            colors = RadioButtonDefaults.colors(selectedColor = accentColor)
                        )
                        Text(name, fontSize = 13.sp, color = Color.White)
                    }
                }
            }
        }

        // Alert Sound
        CustomizationCard(title = if (isAr) "صوت التنبيه" else "Alert Sound") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("azan_short" to (if (isAr) "أذان كامل" else "Full Azan"), "prayer_alert" to (if (isAr) "نغمة قصيرة" else "Short Tone")).forEach { (id, label) ->
                    val selected = alertSound == id
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (selected) accentColor.copy(alpha = 0.2f) else GlassWhite05)
                            .border(1.dp, if (selected) accentColor else BorderWhite10, RoundedCornerShape(14.dp))
                            .clickable { viewModel.settingsManager.setAlertSound(id); viewModel.audioHelper.playTick() }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomizationCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = GlassWhite05),
        border = BorderStroke(1.dp, BorderWhite10)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

/** Reserved for future contributor/attribution credits — intentionally empty for now. */
@Composable
private fun CreditsScreen(currentLang: String, accentColor: Color, onBack: () -> Unit) {
    val isAr = currentLang == "ar"
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("credits_screen")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isAr) "الشكر والتقدير" else "Credits",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = TextMuted, modifier = Modifier.size(36.dp))
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (isAr) "سيتم إضافة الشكر والتقدير هنا قريباً" else "Credits will appear here soon",
                    fontSize = 13.sp,
                    color = TextMuted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}
