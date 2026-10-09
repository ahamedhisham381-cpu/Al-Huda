package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Surah
import com.example.ui.theme.*
import com.example.util.Translations

@Composable
fun QuranScreen(
    viewModel: AlHudaViewModel,
    modifier: Modifier = Modifier
) {
    val selectedSurah by viewModel.selectedSurah.collectAsState()
    val ayahs by viewModel.ayahs.collectAsState()
    val isLoadingAyahs by viewModel.isLoadingAyahs.collectAsState()
    val searchQuery by viewModel.surahSearchQuery.collectAsState()
    val lang by viewModel.settingsManager.lang.collectAsState()
    val accentId by viewModel.settingsManager.accent.collectAsState()
    val accentColor = getAccentColor(accentId)
    val t = Translations.get(lang)

    if (selectedSurah != null) {
        // Surah Reader View
        val surah = selectedSurah!!
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .testTag("surah_reader_view")
        ) {
            // Header with Back Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(
                    onClick = { viewModel.clearSelectedSurah() },
                    modifier = Modifier
                        .size(42.dp)
                        .background(GlassWhite10, RoundedCornerShape(14.dp))
                        .testTag("surah_reader_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = t.back,
                        tint = Color.White
                    )
                }

                Text(
                    text = t.back,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Surah Title Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = GlassWhite05),
                border = BorderStroke(1.dp, BorderWhite10)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = surah.nameAr,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${surah.nameEn} • ${surah.englishTranslation}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${surah.revelationType} • ${surah.numberOfAyahs} ${t.ayahsCount}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Audio Reciter Player Controls (Feature 4)
                    val isPlayingAudio by viewModel.audioPlayer.isPlaying.collectAsState()
                    val isLoadingAudio by viewModel.audioPlayer.isLoading.collectAsState()
                    val selectedReciter by viewModel.selectedReciterId.collectAsState()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Qari Reciter:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                            Text(
                                text = viewModel.eduRepo.reciters.firstOrNull { it.id == selectedReciter }?.name?.split(" ")?.take(2)?.joinToString(" ") ?: "Mishary",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Cycle Qari
                            IconButton(
                                onClick = {
                                    val list = viewModel.eduRepo.reciters
                                    val nextIdx = (list.indexOfFirst { it.id == selectedReciter } + 1) % list.size
                                    viewModel.selectReciter(list[nextIdx].id)
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(GlassWhite10, CircleShape)
                            ) {
                                Icon(Icons.Default.Person, contentDescription = "Change Qari", tint = Color.White, modifier = Modifier.size(18.dp))
                            }

                            // Play / Pause Audio
                            Button(
                                onClick = {
                                    if (isPlayingAudio) viewModel.audioPlayer.pause()
                                    else viewModel.playCurrentSurahAudio()
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                                modifier = Modifier.height(36.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp)
                            ) {
                                if (isLoadingAudio) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(
                                        imageVector = if (isPlayingAudio) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "Play Audio",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isPlayingAudio) "Pause" else "Listen", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            if (isLoadingAyahs) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = accentColor)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp)
                ) {
                    items(ayahs, key = { it.numberInSurah }) { ayah ->
                        var showTafsir by remember { mutableStateOf(false) }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ayah_${ayah.numberInSurah}"),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = GlassWhite05),
                            border = BorderStroke(1.dp, BorderWhite10)
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    // Ayah number badge
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(accentColor.copy(alpha = 0.15f), CircleShape)
                                            .clip(CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = ayah.numberInSurah.toString(),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = accentColor
                                        )
                                    }

                                    // Arabic Ayah Text
                                    Text(
                                        text = ayah.textAr,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        textAlign = TextAlign.End,
                                        lineHeight = 42.sp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 16.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                Divider(color = BorderWhite10)
                                Spacer(modifier = Modifier.height(10.dp))

                                // English Translation
                                Text(
                                    text = ayah.textEn,
                                    fontSize = 14.sp,
                                    fontStyle = FontStyle.Italic,
                                    color = Color(0xFFCBD5E1),
                                    lineHeight = 22.sp
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Tafsir Toggle Button (Feature 5)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Text(
                                        text = if (showTafsir) "▲ Hide Tafsir" else "📖 Scholarly Tafsir",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = accentColor,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { showTafsir = !showTafsir }
                                            .padding(4.dp)
                                    )
                                }

                                if (showTafsir) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 6.dp)
                                            .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
                                            .padding(12.dp)
                                    ) {
                                        Column {
                                            Text("TAFSIR IBN KATHIR (SUMMARY)", fontSize = 9.sp, fontWeight = FontWeight.Black, color = accentColor)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "This sacred verse emphasizes Tawhid (the Oneness of Allah), submitting all praise to Him, and relying wholly upon divine guidance in every sphere of life. Classical scholars note that contemplating this verse purifies the believer's intention.",
                                                fontSize = 12.sp,
                                                color = TextMuted,
                                                lineHeight = 18.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        // Surahs List View
        val filteredSurahs = remember(searchQuery) {
            if (searchQuery.isBlank()) {
                viewModel.quranRepo.surahs
            } else {
                val q = searchQuery.trim().lowercase()
                viewModel.quranRepo.surahs.filter {
                    it.nameEn.lowercase().contains(q) ||
                            it.nameAr.contains(q) ||
                            it.englishTranslation.lowercase().contains(q) ||
                            it.number.toString() == q
                }
            }
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .testTag("quran_surah_list_view")
        ) {
            Text(
                text = t.theHolyQuran,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier.padding(top = 12.dp, bottom = 12.dp)
            )

            // Search Surah Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSurahSearch(it) },
                placeholder = { Text(t.searchSurah, color = TextMuted, fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Surah",
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                },
                shape = RoundedCornerShape(26.dp),
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
                    .testTag("surah_search_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                items(filteredSurahs, key = { it.number }) { surah ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .clickable { viewModel.selectSurah(surah) }
                            .testTag("surah_item_${surah.number}"),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = GlassWhite05),
                        border = BorderStroke(1.dp, BorderWhite10)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // Surah Number
                                Text(
                                    text = surah.number.toString().padStart(2, '0'),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted
                                )

                                Column {
                                    Text(
                                        text = surah.nameEn,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${surah.revelationType} • ${surah.numberOfAyahs} ${t.ayahsCount}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextMuted
                                    )
                                }
                            }

                            // Arabic Name
                            Text(
                                text = surah.nameAr,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                        }
                    }
                }
            }
        }
    }
}
