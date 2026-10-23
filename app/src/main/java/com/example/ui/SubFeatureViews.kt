package com.example.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*
import com.example.util.Translations

@Composable
fun SubFeatureContainer(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(42.dp)
                    .background(GlassWhite10, RoundedCornerShape(14.dp))
                    .testTag("sub_feature_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
        content()
    }
}

// ----------------------------------------------------------------------------
// FEATURE 2: Qibla Compass
// ----------------------------------------------------------------------------
@Composable
fun QiblaCompassView(viewModel: AlHudaViewModel, onBack: () -> Unit) {
    val lat by viewModel.settingsManager.latitude.collectAsState()
    val lng by viewModel.settingsManager.longitude.collectAsState()
    val cityName by viewModel.settingsManager.city.collectAsState()
    val azimuth by viewModel.qiblaHelper.azimuth.collectAsState()
    val accentId by viewModel.settingsManager.accent.collectAsState()
    val accentColor = getAccentColor(accentId)

    val qiblaBearing = remember(lat, lng) {
        viewModel.qiblaHelper.calculateQiblaBearing(lat, lng)
    }
    val distanceKm = remember(lat, lng) {
        viewModel.qiblaHelper.calculateDistanceToKaabaKm(lat, lng).toInt()
    }

    val animatedRotation by animateFloatAsState(
        targetValue = -(azimuth - qiblaBearing),
        label = "compass_rot"
    )

    SubFeatureContainer(title = "Qibla Compass", onBack = onBack) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 96.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Location and metrics
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
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
                    Column {
                        Text("Location", fontSize = 11.sp, color = TextMuted)
                        Text(cityName.split(",")[0], fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Qibla Angle", fontSize = 11.sp, color = TextMuted)
                        Text("${qiblaBearing.toInt()}°", fontSize = 18.sp, fontWeight = FontWeight.Black, color = accentColor)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Distance", fontSize = 11.sp, color = TextMuted)
                        Text("$distanceKm km", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            // Interactive Compass Dial
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .clip(CircleShape)
                    .background(GlassWhite05)
                    .testTag("qibla_compass_dial"),
                contentAlignment = Alignment.Center
            ) {
                // Outer Ring with Degree Marks
                Surface(
                    shape = CircleShape,
                    color = Color.Transparent,
                    border = BorderStroke(3.dp, accentColor.copy(alpha = 0.4f)),
                    modifier = Modifier.size(260.dp)
                ) {}

                // Rotating Dial with Kaaba pointer
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .rotate(animatedRotation),
                    contentAlignment = Alignment.Center
                ) {
                    // Kaaba pointer at top (North / Target)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(accentColor, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🕋", fontSize = 18.sp)
                        }
                        Text("KAABA", fontSize = 9.sp, fontWeight = FontWeight.Black, color = accentColor)
                    }

                    // Compass needle
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .height(130.dp)
                            .background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(3.dp))
                    )
                }

                // Center Degree readout
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${((azimuth).toInt() % 360)}°",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text("Heading", fontSize = 10.sp, color = TextMuted)
                }
            }

            // Manual Slider for devices without magnetometer
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Turn device or adjust heading slider:",
                    fontSize = 11.sp,
                    color = TextMuted
                )
                Slider(
                    value = azimuth,
                    onValueChange = { viewModel.qiblaHelper.setManualAzimuth(it) },
                    valueRange = 0f..360f,
                    colors = SliderDefaults.colors(thumbColor = accentColor, activeTrackColor = accentColor),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                )
            }
        }
    }
}

// ----------------------------------------------------------------------------
// FEATURE 7: 99 Names of Allah
// ----------------------------------------------------------------------------
@Composable
fun NamesOfAllahView(viewModel: AlHudaViewModel, onBack: () -> Unit) {
    val accentId by viewModel.settingsManager.accent.collectAsState()
    val accentColor = getAccentColor(accentId)
    var search by remember { mutableStateOf("") }

    val filtered = remember(search) {
        if (search.isBlank()) viewModel.eduRepo.namesOfAllah
        else {
            val q = search.trim().lowercase()
            viewModel.eduRepo.namesOfAllah.filter {
                it.transliteration.lowercase().contains(q) ||
                        it.meaning.lowercase().contains(q) ||
                        it.arabic.contains(q)
            }
        }
    }

    SubFeatureContainer(title = "99 Names of Allah", onBack = onBack) {
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            placeholder = { Text("Search name or meaning...", color = TextMuted, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = TextMuted) },
            shape = RoundedCornerShape(22.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = GlassWhite05,
                unfocusedContainerColor = GlassWhite05,
                focusedBorderColor = accentColor,
                unfocusedBorderColor = BorderWhite10,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            items(filtered, key = { it.number }) { name ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { viewModel.audioHelper.playTick() },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = GlassWhite05),
                    border = BorderStroke(1.dp, BorderWhite10)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("#${name.number}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(name.arabic, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = accentColor)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(name.transliteration, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(name.meaning, fontSize = 11.sp, color = TextMuted, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// FEATURE 6: Hadith Library
// ----------------------------------------------------------------------------
@Composable
fun HadithLibraryView(viewModel: AlHudaViewModel, onBack: () -> Unit) {
    val accentId by viewModel.settingsManager.accent.collectAsState()
    val accentColor = getAccentColor(accentId)
    val selectedCollection by viewModel.hadithSelectedCollection.collectAsState()
    val hadithList by viewModel.hadithList.collectAsState()
    val isLoading by viewModel.isLoadingHadiths.collectAsState()
    val loadFailed by viewModel.hadithLoadFailed.collectAsState()
    val searchQuery by viewModel.hadithSearchQuery.collectAsState()
    val hadithOfDay by viewModel.hadithOfTheDay.collectAsState()
    val arabicById by viewModel.arabicByHadithId.collectAsState()
    var expandedId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) { viewModel.loadHadithOfTheDay() }

    SubFeatureContainer(title = "Hadith Library", onBack = { if (selectedCollection != null) viewModel.clearHadithCollection() else onBack() }) {
        if (selectedCollection == null) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                item {
                    Text(
                        text = "Sourced from sunnah.com via the open hadith-api",
                        fontSize = 10.sp,
                        color = TextSubtle
                    )
                }
                // Hadith of the Day — live from the API, cached once per day
                item {
                    if (hadithOfDay != null) {
                        val h = hadithOfDay!!
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(26.dp),
                            colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.12f)),
                            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text("✨ HADITH OF THE DAY", fontSize = 11.sp, fontWeight = FontWeight.Black, color = accentColor, letterSpacing = 1.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                if (h.arabicText.isNotBlank()) {
                                    Text(h.arabicText, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                                Text(h.englishText, fontSize = 13.sp, color = Color(0xFFCBD5E1), lineHeight = 20.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("${h.collection} ${h.number}", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(color = accentColor, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Fetching Hadith of the Day…", fontSize = 12.sp, color = TextMuted)
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("BROWSE A COLLECTION", fontSize = 11.sp, fontWeight = FontWeight.Black, color = TextMuted, letterSpacing = 1.sp)
                }

                items(viewModel.hadithRepo.collections, key = { it.slug }) { c ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { viewModel.loadHadithCollection(c.slug) },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = GlassWhite05),
                        border = BorderStroke(1.dp, BorderWhite10)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(c.nameEn, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(c.nameAr, fontSize = 12.sp, color = TextMuted)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = accentColor, modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onHadithSearchChanged(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search this collection or #number", color = TextMuted, fontSize = 13.sp) },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = accentColor, unfocusedBorderColor = BorderWhite10
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                when {
                    isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = accentColor)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Downloading this collection…", fontSize = 12.sp, color = TextMuted)
                        }
                    }
                    loadFailed -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.WifiOff, contentDescription = null, tint = TextMuted, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Couldn't load this collection. Check your connection.", fontSize = 12.sp, color = TextMuted, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(10.dp))
                            TextButton(onClick = { viewModel.loadHadithCollection(selectedCollection!!) }) {
                                Text("Retry", color = accentColor, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    else -> {
                        val filtered = viewModel.filteredHadiths
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(bottom = 96.dp)
                        ) {
                            items(filtered, key = { it.id }) { h ->
                                val isExpanded = expandedId == h.id
                                Card(
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        expandedId = if (isExpanded) null else h.id
                                        if (!isExpanded) viewModel.fetchArabicForHadith(h.id)
                                    },
                                    shape = RoundedCornerShape(22.dp),
                                    colors = CardDefaults.cardColors(containerColor = GlassWhite05),
                                    border = BorderStroke(1.dp, BorderWhite10)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("${h.collection} • ${h.number}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = accentColor)
                                            if (h.grade.isNotBlank()) {
                                                Text(h.grade, fontSize = 10.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        if (h.chapter.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(h.chapter, fontSize = 11.sp, color = TextMuted, fontStyle = FontStyle.Italic)
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = h.englishText,
                                            fontSize = 13.sp,
                                            color = Color(0xFFCBD5E1),
                                            lineHeight = 20.sp,
                                            maxLines = if (isExpanded) Int.MAX_VALUE else 3
                                        )
                                        if (isExpanded) {
                                            val arabic = arabicById[h.id]
                                            Spacer(modifier = Modifier.height(10.dp))
                                            if (arabic != null) {
                                                Text(arabic, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
                                            } else {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    CircularProgressIndicator(color = accentColor, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text("Loading Arabic text…", fontSize = 11.sp, color = TextMuted)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (filtered.isEmpty()) {
                                item {
                                    Text("No hadiths match \"$searchQuery\".", color = TextMuted, fontSize = 13.sp, modifier = Modifier.padding(top = 20.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// FEATURE 17: Dua Collection
// ----------------------------------------------------------------------------
@Composable
fun DuaCollectionView(viewModel: AlHudaViewModel, onBack: () -> Unit) {
    val accentId by viewModel.settingsManager.accent.collectAsState()
    val accentColor = getAccentColor(accentId)
    val duas = viewModel.eduRepo.duas

    SubFeatureContainer(title = "Dua & Supplications", onBack = onBack) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            items(duas, key = { it.id }) { d ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = GlassWhite05),
                    border = BorderStroke(1.dp, BorderWhite10)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(d.category.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Black, color = accentColor)
                            Text(d.reference, fontSize = 10.sp, color = TextMuted)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(d.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(d.arabic, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(d.transliteration, fontSize = 12.sp, color = accentColor.copy(alpha = 0.9f))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(d.translation, fontSize = 13.sp, fontStyle = FontStyle.Italic, color = Color(0xFFCBD5E1))
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// FEATURE 11: Islamic Quiz
// ----------------------------------------------------------------------------
@Composable
fun IslamicQuizView(viewModel: AlHudaViewModel, onBack: () -> Unit) {
    val accentId by viewModel.settingsManager.accent.collectAsState()
    val accentColor = getAccentColor(accentId)
    val score by viewModel.settingsManager.quizScore.collectAsState()
    val qIndex by viewModel.quizQuestionIndex.collectAsState()
    val selectedOpt by viewModel.quizSelectedOption.collectAsState()
    val question = viewModel.eduRepo.quizQuestions[qIndex]

    SubFeatureContainer(title = "Islamic Quiz", onBack = onBack) {
        Column(
            modifier = Modifier.fillMaxSize().padding(bottom = 96.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header stats
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Question ${qIndex + 1} of ${viewModel.eduRepo.quizQuestions.size}", fontSize = 13.sp, color = TextMuted)
                Text("Score: $score pts", fontSize = 14.sp, fontWeight = FontWeight.Black, color = accentColor)
            }

            // Question Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = GlassWhite05),
                border = BorderStroke(1.dp, BorderWhite10)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(question.category.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Black, color = accentColor)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(question.question, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            // 4 Options
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                question.options.forEachIndexed { i, opt ->
                    val isChosen = selectedOpt == i
                    val isCorrect = i == question.correctIndex
                    val btnBg = when {
                        selectedOpt == null -> GlassWhite05
                        isCorrect -> Color(0xFF065F46) // Green
                        isChosen -> Color(0xFF991B1B) // Red
                        else -> GlassWhite05
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .clickable(enabled = selectedOpt == null) { viewModel.answerQuiz(i) },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = btnBg),
                        border = BorderStroke(1.dp, BorderWhite10)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("${('A' + i)}.", fontWeight = FontWeight.Black, color = accentColor)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(opt, fontWeight = FontWeight.SemiBold, color = Color.White)
                        }
                    }
                }
            }

            // Next button & Explanation
            if (selectedOpt != null) {
                Text(question.explanation, fontSize = 12.sp, color = TextMuted, textAlign = TextAlign.Center)
                Button(
                    onClick = { viewModel.nextQuizQuestion() },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Text("Next Question", fontWeight = FontWeight.Bold, color = Color.White)
                }
            } else {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

// ----------------------------------------------------------------------------
// FEATURE 18: Ramadan Tracker
// ----------------------------------------------------------------------------
@Composable
fun RamadanTrackerView(viewModel: AlHudaViewModel, onBack: () -> Unit) {
    val accentId by viewModel.settingsManager.accent.collectAsState()
    val accentColor = getAccentColor(accentId)
    val fasts by viewModel.settingsManager.ramadanFasts.collectAsState()
    val juzProgress by viewModel.ramadanJuzProgress.collectAsState()

    SubFeatureContainer(title = "Ramadan Tracker", onBack = onBack) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Fasting Progress Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.2f)),
                    border = BorderStroke(1.dp, accentColor)
                ) {
                    Column(modifier = Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("FASTING LOG: ${fasts.size} OF 30 DAYS", fontWeight = FontWeight.Black, color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { fasts.size / 30f },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = accentColor,
                            trackColor = Color.White.copy(alpha = 0.2f)
                        )
                    }
                }
            }

            // 30 Days Grid
            item {
                Text("Tap day to mark fast completed:", fontSize = 12.sp, color = TextMuted)
                Spacer(modifier = Modifier.height(8.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    modifier = Modifier.height(260.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(30) { dayIdx ->
                        val dayNum = dayIdx + 1
                        val isDone = fasts.contains(dayNum.toString())
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDone) accentColor else GlassWhite10)
                                .clickable { viewModel.settingsManager.toggleRamadanFast(dayNum) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(dayNum.toString(), fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            // Quran Juz Reading Progress
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = GlassWhite05),
                    border = BorderStroke(1.dp, BorderWhite10)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("QURAN JUZ (1-30)", fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Juz $juzProgress", fontWeight = FontWeight.Black, color = accentColor)
                        }
                        Slider(
                            value = juzProgress.toFloat(),
                            onValueChange = { viewModel.setRamadanJuz(it.toInt()) },
                            valueRange = 1f..30f,
                            steps = 28,
                            colors = SliderDefaults.colors(thumbColor = accentColor, activeTrackColor = accentColor)
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// FEATURE 19: Hajj & Umrah Guide
// ----------------------------------------------------------------------------
@Composable
fun HajjUmrahView(viewModel: AlHudaViewModel, onBack: () -> Unit) {
    val accentId by viewModel.settingsManager.accent.collectAsState()
    val accentColor = getAccentColor(accentId)
    val steps = viewModel.eduRepo.hajjSteps

    SubFeatureContainer(title = "Hajj & Umrah Guide", onBack = onBack) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            items(steps, key = { "${it.isUmrah}_${it.stepNumber}" }) { step ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = GlassWhite05),
                    border = BorderStroke(1.dp, BorderWhite10)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(if (step.isUmrah) "UMRAH STEP ${step.stepNumber}" else "HAJJ STEP ${step.stepNumber}", fontSize = 10.sp, fontWeight = FontWeight.Black, color = accentColor)
                            Text(step.arabicTitle, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(step.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(step.description, fontSize = 13.sp, color = TextMuted)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(step.duaText, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = accentColor)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("💡 Tip: ${step.tips}", fontSize = 11.sp, fontStyle = FontStyle.Italic, color = TextSubtle)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// FEATURE 22: Good Deeds Sunnah Log
// ----------------------------------------------------------------------------
@Composable
fun GoodDeedsView(viewModel: AlHudaViewModel, onBack: () -> Unit) {
    val accentId by viewModel.settingsManager.accent.collectAsState()
    val accentColor = getAccentColor(accentId)
    val deedsDone by viewModel.settingsManager.goodDeeds.collectAsState()
    val deeds = viewModel.eduRepo.defaultGoodDeeds

    SubFeatureContainer(title = "Good Deeds Sunnah Log", onBack = onBack) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            item {
                Text("Daily Sunnah habits completed: ${deedsDone.size} of ${deeds.size}", fontSize = 12.sp, color = TextMuted)
            }
            items(deeds, key = { it.id }) { d ->
                val isDone = deedsDone.contains(d.id)
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { viewModel.settingsManager.toggleGoodDeed(d.id) },
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isDone) Color(0xFF065F46).copy(alpha = 0.35f) else GlassWhite05),
                    border = BorderStroke(1.dp, if (isDone) Color(0xFF10B981) else BorderWhite10)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(d.title, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(d.virtue, fontSize = 11.sp, color = TextMuted)
                        }
                        Checkbox(
                            checked = isDone,
                            onCheckedChange = { viewModel.settingsManager.toggleGoodDeed(d.id) },
                            colors = CheckboxDefaults.colors(checkedColor = accentColor)
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// FEATURE 23: Istikharah Guide
// ----------------------------------------------------------------------------
@Composable
fun IstikharahView(viewModel: AlHudaViewModel, onBack: () -> Unit) {
    val accentId by viewModel.settingsManager.accent.collectAsState()
    val accentColor = getAccentColor(accentId)

    SubFeatureContainer(title = "Salat al-Istikharah Guide", onBack = onBack) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = GlassWhite05),
                    border = BorderStroke(1.dp, BorderWhite10)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("HOW TO PRAY ISTIKHARAH", fontSize = 12.sp, fontWeight = FontWeight.Black, color = accentColor)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "1. Perform wudu and pray 2 voluntary rak'ahs of prayer.\n" +
                                    "2. After the tasleem, praise Allah and send Salawat on the Prophet ﷺ.\n" +
                                    "3. Recite the authentic dua below, mentioning your decision.",
                            fontSize = 13.sp,
                            color = Color(0xFFE2E8F0),
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = GlassWhite05),
                    border = BorderStroke(1.dp, BorderWhite10)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("DUA AL-ISTIKHARAH (ARABIC)", fontSize = 11.sp, fontWeight = FontWeight.Black, color = accentColor)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(viewModel.eduRepo.istikharahDua, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.End, lineHeight = 36.sp, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(14.dp))
                        Divider(color = BorderWhite10)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(viewModel.eduRepo.istikharahTranslation, fontSize = 13.sp, fontStyle = FontStyle.Italic, color = Color(0xFFCBD5E1), lineHeight = 20.sp)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// FEATURE 12: Learn Quranic Arabic
// ----------------------------------------------------------------------------
@Composable
fun LearnArabicView(viewModel: AlHudaViewModel, onBack: () -> Unit) {
    val accentId by viewModel.settingsManager.accent.collectAsState()
    val accentColor = getAccentColor(accentId)
    val lessons = viewModel.eduRepo.arabicLessons

    SubFeatureContainer(title = "Learn Quranic Arabic", onBack = onBack) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            items(lessons, key = { it.id }) { l ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { viewModel.audioHelper.playTick() },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = GlassWhite05),
                    border = BorderStroke(1.dp, BorderWhite10)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(l.category.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Black, color = accentColor)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(l.letterOrWord, fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(l.transliteration, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(l.meaning, fontSize = 11.sp, color = TextMuted, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// FEATURE 13: Seerah Timeline
// ----------------------------------------------------------------------------
@Composable
fun SeerahTimelineView(viewModel: AlHudaViewModel, onBack: () -> Unit) {
    val accentId by viewModel.settingsManager.accent.collectAsState()
    val accentColor = getAccentColor(accentId)
    val events = viewModel.eduRepo.seerahEvents

    SubFeatureContainer(title = "Seerah Timeline of Prophet ﷺ", onBack = onBack) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            items(events, key = { it.yearHijriOrAD }) { ev ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = GlassWhite05),
                    border = BorderStroke(1.dp, BorderWhite10)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(ev.yearHijriOrAD, fontSize = 11.sp, fontWeight = FontWeight.Black, color = accentColor)
                            Text(ev.location, fontSize = 11.sp, color = TextMuted)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(ev.title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(ev.description, fontSize = 13.sp, color = TextMuted, lineHeight = 19.sp)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// FEATURE 14: Kids Corner
// ----------------------------------------------------------------------------
@Composable
fun KidsCornerView(viewModel: AlHudaViewModel, onBack: () -> Unit) {
    val accentId by viewModel.settingsManager.accent.collectAsState()
    val accentColor = getAccentColor(accentId)
    val stories = viewModel.eduRepo.kidsStories

    SubFeatureContainer(title = "Kids Corner 🌟", onBack = onBack) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            items(stories, key = { it.id }) { s ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = GlassWhite05),
                    border = BorderStroke(1.dp, BorderWhite10)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(s.arabicTitle, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = accentColor, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(s.title, fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(s.storyText, fontSize = 14.sp, color = Color(0xFFE2E8F0), lineHeight = 22.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("✨ Lesson: ${s.moral}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = accentColor)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// FEATURE 20: Prayer Tracker (5 Daily Prayers)
// ----------------------------------------------------------------------------
@Composable
fun PrayerTrackerView(viewModel: AlHudaViewModel, onBack: () -> Unit) {
    val accentId by viewModel.settingsManager.accent.collectAsState()
    val accentColor = getAccentColor(accentId)
    val completed by viewModel.settingsManager.completedPrayers.collectAsState()
    val prayers = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")

    SubFeatureContainer(title = "5 Daily Prayers Tracker", onBack = onBack) {
        Column(
            modifier = Modifier.fillMaxSize().padding(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.2f)),
                border = BorderStroke(1.dp, accentColor)
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("TODAY'S PRAYER FULFILLMENT", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.White)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${completed.size} / 5 Prayers Done", fontSize = 22.sp, fontWeight = FontWeight.Black, color = accentColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { completed.size / 5f },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = accentColor,
                        trackColor = Color.White.copy(alpha = 0.2f)
                    )
                }
            }

            // Weekly streak — last 7 days at a glance
            val weekHistory = remember(completed) { viewModel.settingsManager.getWeekHistory() }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = GlassWhite05),
                border = BorderStroke(1.dp, BorderWhite10)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("THIS WEEK", fontSize = 11.sp, fontWeight = FontWeight.Black, color = TextMuted, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        weekHistory.forEach { (dayLabel, isToday, count) ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(
                                            when {
                                                count == 5 -> accentColor
                                                count > 0 -> accentColor.copy(alpha = 0.3f)
                                                else -> GlassWhite10
                                            },
                                            CircleShape
                                        )
                                        .then(if (isToday) Modifier.border(1.5.dp, Color.White, CircleShape) else Modifier),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (count == 5) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                    } else if (count > 0) {
                                        Text(count.toString(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(dayLabel, fontSize = 10.sp, color = TextMuted)
                            }
                        }
                    }
                }
            }

            prayers.forEach { p ->
                val isDone = completed.contains(p)
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { viewModel.togglePrayerCompleted(p) },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isDone) Color(0xFF065F46).copy(alpha = 0.35f) else GlassWhite05),
                    border = BorderStroke(1.dp, if (isDone) Color(0xFF10B981) else BorderWhite10)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(p, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Checkbox(
                            checked = isDone,
                            onCheckedChange = { viewModel.togglePrayerCompleted(p) },
                            colors = CheckboxDefaults.colors(checkedColor = accentColor)
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// FEATURE 21: Hifz Memorization Tracker
// ----------------------------------------------------------------------------
@Composable
fun HifzTrackerView(viewModel: AlHudaViewModel, onBack: () -> Unit) {
    val accentId by viewModel.settingsManager.accent.collectAsState()
    val accentColor = getAccentColor(accentId)
    val memorized by viewModel.settingsManager.hifzMemorized.collectAsState()
    val surahs = viewModel.quranRepo.surahs

    SubFeatureContainer(title = "Hifz Quran Memorization", onBack = onBack) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            item {
                Text("Total Surahs memorized: ${memorized.size} of 114", fontSize = 12.sp, color = TextMuted)
            }
            items(surahs, key = { it.number }) { s ->
                val isDone = memorized.contains(s.number.toString())
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { viewModel.settingsManager.toggleHifz(s.number) },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isDone) Color(0xFF065F46).copy(alpha = 0.35f) else GlassWhite05),
                    border = BorderStroke(1.dp, if (isDone) Color(0xFF10B981) else BorderWhite10)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(s.number.toString().padStart(2, '0'), fontSize = 11.sp, color = TextMuted)
                            Text(s.nameEn, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Checkbox(
                            checked = isDone,
                            onCheckedChange = { viewModel.settingsManager.toggleHifz(s.number) },
                            colors = CheckboxDefaults.colors(checkedColor = accentColor)
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// FEATURE 25: Nearby Mosques Finder
// ----------------------------------------------------------------------------
@Composable
fun NearbyMosquesView(viewModel: AlHudaViewModel, onBack: () -> Unit) {
    val accentId by viewModel.settingsManager.accent.collectAsState()
    val accentColor = getAccentColor(accentId)
    val mosques by viewModel.nearbyMosques.collectAsState()
    val isLoading by viewModel.isLoadingMosques.collectAsState()
    val loadFailed by viewModel.mosqueLoadFailed.collectAsState()
    val cityName by viewModel.settingsManager.city.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadNearbyMosques() }

    SubFeatureContainer(title = "Nearby Mosques", onBack = onBack) {
        Text(
            text = "Real mosques mapped on OpenStreetMap around $cityName",
            fontSize = 12.sp,
            color = TextMuted,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        when {
            isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = accentColor)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Searching nearby mosques…", fontSize = 12.sp, color = TextMuted)
                }
            }
            loadFailed -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.WifiOff, contentDescription = null, tint = TextMuted, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "No mosques found nearby — either none are mapped on OpenStreetMap yet, or there's no connection.",
                        fontSize = 12.sp, color = TextMuted, textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    TextButton(onClick = { viewModel.loadNearbyMosques(force = true) }) {
                        Text("Retry", color = accentColor, fontWeight = FontWeight.Bold)
                    }
                }
            }
            else -> MosqueResultsList(mosques, accentColor)
        }
    }
}

@Composable
private fun MosqueResultsList(mosques: List<com.example.model.MosqueItem>, accentColor: Color) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            items(mosques, key = { it.id }) { m ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = GlassWhite05),
                    border = BorderStroke(1.dp, BorderWhite10)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(m.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(m.address, fontSize = 12.sp, color = TextMuted)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(m.contact, fontSize = 11.sp, color = accentColor)
                        }
                        Box(
                            modifier = Modifier.background(accentColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp)).padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${m.distanceKm} km", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
}

// ----------------------------------------------------------------------------
// FEATURE 24: Islamic News & Uplifting Stories
// ----------------------------------------------------------------------------
@Composable
fun IslamicNewsView(viewModel: AlHudaViewModel, onBack: () -> Unit) {
    val accentId by viewModel.settingsManager.accent.collectAsState()
    val accentColor = getAccentColor(accentId)
    val news = viewModel.eduRepo.newsArticles

    SubFeatureContainer(title = "Islamic News & Articles", onBack = onBack) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            items(news, key = { it.id }) { n ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = GlassWhite05),
                    border = BorderStroke(1.dp, BorderWhite10)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(n.category.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Black, color = accentColor)
                            Text(n.date, fontSize = 10.sp, color = TextMuted)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(n.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(n.summary, fontSize = 13.sp, color = TextMuted)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// FEATURE 27: Community Reflections
// ----------------------------------------------------------------------------
@Composable
fun CommunityReflectionsView(viewModel: AlHudaViewModel, onBack: () -> Unit) {
    val accentId by viewModel.settingsManager.accent.collectAsState()
    val accentColor = getAccentColor(accentId)
    val posts = viewModel.eduRepo.communityPosts

    SubFeatureContainer(title = "Community Reflections", onBack = onBack) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            items(posts, key = { it.id }) { post ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = GlassWhite05),
                    border = BorderStroke(1.dp, BorderWhite10)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(post.authorName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(post.country, fontSize = 11.sp, color = TextMuted)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(post.text, fontSize = 13.sp, color = Color(0xFFCBD5E1), lineHeight = 20.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Favorite, contentDescription = "Likes", tint = accentColor, modifier = Modifier.size(16.dp))
                            Text("${post.likesCount} blessings", fontSize = 11.sp, color = TextMuted)
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// FEATURE 16: Fatwa & Classical Rulings FAQ
// ----------------------------------------------------------------------------
@Composable
fun FatwaFaqView(viewModel: AlHudaViewModel, onBack: () -> Unit) {
    val accentId by viewModel.settingsManager.accent.collectAsState()
    val accentColor = getAccentColor(accentId)
    val fatwas = viewModel.eduRepo.fatwas

    SubFeatureContainer(title = "Fatwa & Rulings FAQ", onBack = onBack) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            items(fatwas, key = { it.id }) { f ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = GlassWhite05),
                    border = BorderStroke(1.dp, BorderWhite10)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(f.category.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Black, color = accentColor)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(f.question, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(f.answerSummary, fontSize = 13.sp, color = Color(0xFFCBD5E1), lineHeight = 20.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Source: ${f.reference}", fontSize = 10.sp, color = TextMuted)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// FEATURE: Daily Azkar (Morning & Evening Remembrance)
// ----------------------------------------------------------------------------
@Composable
fun AzkarView(viewModel: AlHudaViewModel, onBack: () -> Unit) {
    val accentId by viewModel.settingsManager.accent.collectAsState()
    val accentColor = getAccentColor(accentId)
    val completed by viewModel.settingsManager.azkarCompleted.collectAsState()
    val allAzkar = viewModel.eduRepo.azkar

    var section by remember { mutableStateOf("Morning") }
    val sectionAzkar = remember(section) { allAzkar.filter { it.section == section } }
    val sectionDone = sectionAzkar.count { completed.contains(it.id) }

    SubFeatureContainer(title = "Daily Azkar", onBack = onBack) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf("Morning" to "🌅 Morning", "Evening" to "🌙 Evening").forEach { (id, label) ->
                val selected = section == id
                FilterChip(
                    selected = selected,
                    onClick = { section = id },
                    label = { Text(label, fontSize = 13.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = accentColor,
                        selectedLabelColor = Color.Black,
                        containerColor = GlassWhite05,
                        labelColor = Color.White
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selected,
                        borderColor = BorderWhite10,
                        selectedBorderColor = accentColor
                    )
                )
            }
        }

        Text(
            text = "$sectionDone of ${sectionAzkar.size} completed today",
            fontSize = 12.sp,
            color = TextMuted,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            items(sectionAzkar, key = { it.id }) { item ->
                val isDone = completed.contains(item.id)
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { viewModel.settingsManager.toggleAzkar(item.id) },
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isDone) Color(0xFF065F46).copy(alpha = 0.35f) else GlassWhite05),
                    border = BorderStroke(1.dp, if (isDone) Color(0xFF10B981) else BorderWhite10)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(accentColor.copy(alpha = 0.18f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (item.repeat > 1) "×${item.repeat}" else "once",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = accentColor
                                )
                            }
                            Checkbox(
                                checked = isDone,
                                onCheckedChange = { viewModel.settingsManager.toggleAzkar(item.id) },
                                colors = CheckboxDefaults.colors(checkedColor = accentColor)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            item.arabic,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.End,
                            lineHeight = 30.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(item.transliteration, fontSize = 11.sp, fontStyle = FontStyle.Italic, color = TextMuted)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(item.translation, fontSize = 13.sp, color = Color(0xFFCBD5E1), lineHeight = 19.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Source: ${item.reference}", fontSize = 10.sp, color = TextMuted)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// FEATURE: Zakat Calculator
// ----------------------------------------------------------------------------
@Composable
fun ZakatCalculatorView(viewModel: AlHudaViewModel, onBack: () -> Unit) {
    val accentId by viewModel.settingsManager.accent.collectAsState()
    val accentColor = getAccentColor(accentId)

    var cash by remember { mutableStateOf("") }
    var goldSilver by remember { mutableStateOf("") }
    var investments by remember { mutableStateOf("") }
    var businessAssets by remember { mutableStateOf("") }
    var receivables by remember { mutableStateOf("") }
    var debts by remember { mutableStateOf("") }
    var nisab by remember { mutableStateOf("") }

    fun num(s: String) = s.toDoubleOrNull() ?: 0.0

    val totalWealth = num(cash) + num(goldSilver) + num(investments) + num(businessAssets) + num(receivables)
    val netWealth = (totalWealth - num(debts)).coerceAtLeast(0.0)
    val nisabValue = num(nisab)
    val meetsNisab = nisabValue > 0 && netWealth >= nisabValue
    val zakatDue = if (meetsNisab) netWealth * 0.025 else 0.0

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = GlassWhite05,
        unfocusedContainerColor = GlassWhite05,
        focusedBorderColor = accentColor.copy(alpha = 0.5f),
        unfocusedBorderColor = BorderWhite10,
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White
    )

    @Composable
    fun moneyField(label: String, value: String, onChange: (String) -> Unit) {
        OutlinedTextField(
            value = value,
            onValueChange = { v -> onChange(v.filter { it.isDigit() || it == '.' }) },
            label = { Text(label, color = TextMuted, fontSize = 13.sp) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = fieldColors,
            modifier = Modifier.fillMaxWidth()
        )
    }

    SubFeatureContainer(title = "Zakat Calculator", onBack = onBack) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = GlassWhite05),
                    border = BorderStroke(1.dp, BorderWhite10)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("ABOUT ZAKAT", fontSize = 11.sp, fontWeight = FontWeight.Black, color = accentColor)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Zakat is 2.5% of your zakatable wealth held for one lunar year, once it reaches the Nisab threshold (≈ the value of 87.48g gold or 612.36g silver). Enter today's Nisab value in your currency — check the current gold/silver price to work it out.",
                            fontSize = 12.sp,
                            color = Color(0xFFCBD5E1),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
            item { moneyField("Nisab threshold (your currency)", nisab) { nisab = it } }
            item { Text("ZAKATABLE ASSETS", fontSize = 11.sp, fontWeight = FontWeight.Black, color = accentColor) }
            item { moneyField("Cash & bank savings", cash) { cash = it } }
            item { moneyField("Gold & silver value", goldSilver) { goldSilver = it } }
            item { moneyField("Investments & stocks", investments) { investments = it } }
            item { moneyField("Business inventory", businessAssets) { businessAssets = it } }
            item { moneyField("Money owed to you", receivables) { receivables = it } }
            item { Text("LIABILITIES", fontSize = 11.sp, fontWeight = FontWeight.Black, color = accentColor) }
            item { moneyField("Debts you currently owe", debts) { debts = it } }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.14f)),
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("NET ZAKATABLE WEALTH", fontSize = 11.sp, fontWeight = FontWeight.Black, color = TextMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(String.format("%,.2f", netWealth), fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White)
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("ZAKAT DUE (2.5%)", fontSize = 11.sp, fontWeight = FontWeight.Black, color = TextMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(String.format("%,.2f", zakatDue), fontSize = 32.sp, fontWeight = FontWeight.Black, color = accentColor)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = when {
                                nisabValue <= 0 -> "Enter the Nisab threshold above to calculate."
                                meetsNisab -> "Your wealth meets the Nisab — Zakat is due."
                                else -> "Your wealth is below the Nisab — no Zakat is due this year."
                            },
                            fontSize = 11.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
