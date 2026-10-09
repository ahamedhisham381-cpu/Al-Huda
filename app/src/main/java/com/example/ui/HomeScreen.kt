package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PrayerTimes
import com.example.ui.theme.*
import com.example.util.Translations

data class PrayerDisplayItem(
    val id: String,
    val name: String,
    val rawTime: String,
    val isNext: Boolean,
    val isPassed: Boolean,
    val isCompleted: Boolean
)

@Composable
fun HomeScreen(
    viewModel: AlHudaViewModel,
    modifier: Modifier = Modifier
) {
    val prayerTimes by viewModel.prayerTimes.collectAsState()
    val hijriDate by viewModel.hijriDate.collectAsState()
    val nextPrayer by viewModel.nextPrayer.collectAsState()
    val completedPrayers by viewModel.settingsManager.completedPrayers.collectAsState()
    val cityName by viewModel.settingsManager.city.collectAsState()
    val lat by viewModel.settingsManager.latitude.collectAsState()
    val lng by viewModel.settingsManager.longitude.collectAsState()
    val lang by viewModel.settingsManager.lang.collectAsState()
    val accentId by viewModel.settingsManager.accent.collectAsState()
    val accentColor = getAccentColor(accentId)
    val t = Translations.get(lang)

    var searchQuery by remember { mutableStateOf("") }
    val isFriday = remember { viewModel.isTodayFriday() }

    val prayerList = remember(prayerTimes, nextPrayer, completedPrayers, isFriday, lang) {
        val pt = prayerTimes ?: PrayerTimes("05:15", "06:35", "12:30", "15:45", "18:20", "19:40")
        listOf(
            PrayerDisplayItem("Fajr", t.fajr, pt.fajr, nextPrayer == "Fajr", viewModel.isPrayerPassed(pt.fajr), completedPrayers.contains("Fajr")),
            PrayerDisplayItem("Sunrise", t.sunrise, pt.sunrise, nextPrayer == "Sunrise", viewModel.isPrayerPassed(pt.sunrise), completedPrayers.contains("Sunrise")),
            PrayerDisplayItem(
                "Dhuhr",
                if (isFriday) t.fridaySalah else t.dhuhr,
                pt.dhuhr,
                nextPrayer == "Dhuhr",
                viewModel.isPrayerPassed(pt.dhuhr),
                completedPrayers.contains("Dhuhr")
            ),
            PrayerDisplayItem("Asr", t.asr, pt.asr, nextPrayer == "Asr", viewModel.isPrayerPassed(pt.asr), completedPrayers.contains("Asr")),
            PrayerDisplayItem("Maghrib", t.maghrib, pt.maghrib, nextPrayer == "Maghrib", viewModel.isPrayerPassed(pt.maghrib), completedPrayers.contains("Maghrib")),
            PrayerDisplayItem("Isha", t.isha, pt.isha, nextPrayer == "Isha", viewModel.isPrayerPassed(pt.isha), completedPrayers.contains("Isha"))
        )
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("home_screen_grid"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp)
    ) {
        // City search bar
        item(span = { GridItemSpan(maxLineSpan) }) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.onCitySearch(it)
                },
                placeholder = { Text(t.searchCity, color = TextMuted, fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                },
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = GlassWhite05,
                    unfocusedContainerColor = GlassWhite05,
                    focusedBorderColor = accentColor.copy(alpha = 0.5f),
                    unfocusedBorderColor = BorderWhite10,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("city_search_input")
            )
        }

        // Section Title, Hijri date and export actions
        item(span = { GridItemSpan(maxLineSpan) }) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = t.salah,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )

                    hijriDate?.let { hd ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(accentColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            val hijriText = if (lang == "ar") {
                                "${hd.day} ${hd.monthAr} ${hd.year} هـ"
                            } else {
                                "${hd.day} ${hd.monthEn} ${hd.year} AH"
                            }
                            Text(
                                text = hijriText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextMuted
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = cityName.split(",")[0],
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )

                        // Export Schedule Icon Button
                        IconButton(
                            onClick = { viewModel.exportAnnualSchedule() },
                            modifier = Modifier
                                .size(32.dp)
                                .background(GlassWhite10, RoundedCornerShape(10.dp))
                                .testTag("export_schedule_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = t.downloadSchedule,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Text(
                        text = String.format(java.util.Locale.US, "%.2f° N, %.2f° E", lat, lng),
                        fontSize = 11.sp,
                        color = TextSubtle
                    )
                }
            }
        }

        // Prayer Cards
        items(prayerList, key = { it.id }) { item ->
            PrayerCardItem(
                item = item,
                accentColor = accentColor,
                format12 = { viewModel.prayerRepo.formatTo12Hr(it) },
                onToggle = { viewModel.togglePrayerCompleted(item.id) }
            )
        }

        // Athan Audio & Notification Trigger Controls
        item(span = { GridItemSpan(maxLineSpan) }) {
            val isAzanPlaying by viewModel.azanAudio.isPlaying.collectAsState()
            val azanPrayer by viewModel.azanAudio.currentPrayerName.collectAsState()

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("athan_control_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isAzanPlaying) Color(0xFF065F46).copy(alpha = 0.4f) else GlassWhite05
                ),
                border = BorderStroke(1.dp, if (isAzanPlaying) Color(0xFF10B981) else BorderWhite10)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(accentColor.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isAzanPlaying) Icons.Default.VolumeUp else Icons.Default.NotificationsActive,
                                contentDescription = "Athan Alert",
                                tint = accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column {
                            Text(
                                text = if (isAzanPlaying) "Athan Recitation (${azanPrayer ?: "Salah"})" else "Azan & Prayer Alerts",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (isAzanPlaying) "Playing live Athan audio" else "Scheduled for 5 daily prayer times",
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (isAzanPlaying) {
                            Button(
                                onClick = { viewModel.stopAzan() },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("Stop Athan", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        } else {
                            Button(
                                onClick = { viewModel.playAzan(nextPrayer ?: "Fajr") },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp).testTag("play_athan_button")
                            ) {
                                Text("Play Athan", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        // Test Notification Trigger
                        IconButton(
                            onClick = { viewModel.triggerTestPrayerNotification() },
                            modifier = Modifier
                                .size(34.dp)
                                .background(GlassWhite10, RoundedCornerShape(12.dp))
                                .testTag("test_prayer_notification_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Test Notification",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Quick Access Islamic Tools Row
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                Text(
                    text = "QUICK ISLAMIC TOOLS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionChip("🕋 Qibla", accentColor) { viewModel.openSubFeature("qibla") }
                    QuickActionChip("✨ 99 Names", accentColor) { viewModel.openSubFeature("names99") }
                    QuickActionChip("🤲 Duas", accentColor) { viewModel.openSubFeature("duas") }
                    QuickActionChip("🌙 Ramadan", accentColor) { viewModel.openSubFeature("ramadan") }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionChip("🧠 Quiz", accentColor) { viewModel.openSubFeature("quiz") }
                    QuickActionChip("💚 Sunnah", accentColor) { viewModel.openSubFeature("deeds") }
                    QuickActionChip("📖 Arabic", accentColor) { viewModel.openSubFeature("arabic") }
                    QuickActionChip("📜 Seerah", accentColor) { viewModel.openSubFeature("seerah") }
                }
            }
        }

        // Daily Wisdom Card
        item(span = { GridItemSpan(maxLineSpan) }) {
            DailyWisdomCard(
                wisdom = viewModel.dailyWisdom,
                accentColor = accentColor,
                t = t
            )
        }

        // Encouragement Banner
        item(span = { GridItemSpan(maxLineSpan) }) {
            TrustTimingCard(
                accentColor = accentColor,
                title = t.trustTiming,
                verse = t.trustTimingVerse
            )
        }
    }
}

@Composable
fun PrayerCardItem(
    item: PrayerDisplayItem,
    accentColor: Color,
    format12: (String) -> String,
    onToggle: () -> Unit
) {
    val isCompleted = item.isCompleted
    val isNext = item.isNext
    val isPassed = item.isPassed && !isNext

    val bgModifier = when {
        isCompleted -> Modifier.background(
            color = Color(0xFF064E3B).copy(alpha = 0.35f),
            shape = RoundedCornerShape(26.dp)
        )
        isNext -> Modifier.background(
            brush = Brush.linearGradient(
                listOf(accentColor, accentColor.copy(alpha = 0.85f))
            ),
            shape = RoundedCornerShape(26.dp)
        )
        isPassed -> Modifier.background(
            color = GlassWhite05,
            shape = RoundedCornerShape(26.dp)
        )
        else -> Modifier.background(
            color = GlassWhite10,
            shape = RoundedCornerShape(26.dp)
        )
    }

    val borderStroke = when {
        isCompleted -> BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f))
        isNext -> null
        else -> BorderStroke(1.dp, BorderWhite10)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp)
            .clip(RoundedCornerShape(26.dp))
            .then(bgModifier)
            .clickable { onToggle() }
            .testTag("prayer_card_${item.id}"),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = borderStroke
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            // "Next" badge on top right
            if (isNext && !isCompleted) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "NEXT",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                }
            }

            // Completed checkmark on top right
            if (isCompleted) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(22.dp)
                        .background(Color(0xFF10B981).copy(alpha = 0.25f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = Color(0xFF34D399),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.align(Alignment.BottomStart)
            ) {
                Text(
                    text = item.name.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isNext) Color.White.copy(alpha = 0.85f) else TextMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = format12(item.rawTime),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = when {
                        isCompleted -> Color(0xFFA7F3D0)
                        isNext -> Color.White
                        isPassed -> TextMuted.copy(alpha = 0.6f)
                        else -> Color.White
                    }
                )
            }
        }
    }
}

@Composable
fun DailyWisdomCard(
    wisdom: com.example.model.DailyWisdom,
    accentColor: Color,
    t: com.example.util.AppTranslations
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("daily_wisdom_card"),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = GlassWhite05),
        border = BorderStroke(1.dp, BorderWhite10)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(accentColor, CircleShape)
                )
                Text(
                    text = t.wisdom.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Arabic text
            Text(
                text = wisdom.verse,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 44.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // English translation
            Text(
                text = "“${wisdom.translation}”",
                fontSize = 14.sp,
                fontStyle = FontStyle.Italic,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Reflection & Action item in glass container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Column {
                        Text(
                            text = t.reflection.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = accentColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = wisdom.reflection,
                            fontSize = 13.sp,
                            color = Color(0xFFCBD5E1),
                            lineHeight = 20.sp
                        )
                    }

                    Divider(color = BorderWhite10)

                    Column {
                        Text(
                            text = t.todaysStep.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = accentColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(accentColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = wisdom.actionItem,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrustTimingCard(
    accentColor: Color,
    title: String,
    verse: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("trust_timing_banner"),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.25f)),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = verse,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                fontStyle = FontStyle.Italic
            )
        }
    }
}

@Composable
fun RowScope.QuickActionChip(
    label: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = GlassWhite10,
        border = BorderStroke(1.dp, BorderWhite10),
        modifier = Modifier
            .weight(1f)
            .height(44.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1
            )
        }
    }
}
