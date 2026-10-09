package com.example.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
    val hadiths = viewModel.eduRepo.hadiths

    SubFeatureContainer(title = "Hadith Library", onBack = onBack) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            items(hadiths, key = { it.id }) { h ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = GlassWhite05),
                    border = BorderStroke(1.dp, BorderWhite10)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${h.collection} • ${h.number}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = accentColor)
                            Text(h.grade, fontSize = 10.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Narrated by ${h.narrator}", fontSize = 11.sp, color = TextMuted, fontStyle = FontStyle.Italic)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(h.arabicText, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(h.englishText, fontSize = 13.sp, color = Color(0xFFCBD5E1), lineHeight = 20.sp)
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
    val mosques = viewModel.eduRepo.nearbyMosques

    SubFeatureContainer(title = "Nearby Mosques", onBack = onBack) {
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
