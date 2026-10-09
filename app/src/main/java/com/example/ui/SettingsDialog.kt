package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
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

    val t = Translations.get(currentLang)
    val accentColor = getAccentColor(currentAccent)

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
