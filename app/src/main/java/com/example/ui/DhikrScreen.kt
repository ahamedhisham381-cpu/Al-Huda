package com.example.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.util.Translations

@Composable
fun DhikrScreen(
    viewModel: AlHudaViewModel,
    modifier: Modifier = Modifier
) {
    val count by viewModel.dhikrCount.collectAsState()
    val phraseIndex by viewModel.dhikrPhraseIndex.collectAsState()
    val currentPhrase = viewModel.spiritualRepo.dhikrPhrases[phraseIndex]
    val lang by viewModel.settingsManager.lang.collectAsState()
    val accentId by viewModel.settingsManager.accent.collectAsState()
    val accentColor = getAccentColor(accentId)
    val t = Translations.get(lang)

    var isTapped by remember { mutableStateOf(false) }
    val circleScale by animateFloatAsState(
        targetValue = if (isTapped) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f),
        label = "tap_scale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("dhikr_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Bar: Title & controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = t.tasbih.uppercase(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp,
                color = accentColor
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Switch phrase
                IconButton(
                    onClick = { viewModel.nextDhikrPhrase() },
                    modifier = Modifier
                        .size(42.dp)
                        .background(GlassWhite10, RoundedCornerShape(14.dp))
                        .testTag("switch_dhikr_phrase_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Switch Phrase",
                        tint = Color.White
                    )
                }

                // Reset counter
                IconButton(
                    onClick = { viewModel.resetDhikr() },
                    modifier = Modifier
                        .size(42.dp)
                        .background(GlassWhite10, RoundedCornerShape(14.dp))
                        .testTag("reset_dhikr_counter_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Counter",
                        tint = Color.White
                    )
                }
            }
        }

        // Dhikr Phrases display & Big Tap Circle
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            // Arabic Text
            Text(
                text = currentPhrase.arabic,
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 60.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // English Transliteration
            Text(
                text = currentPhrase.english,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // English Meaning
            Text(
                text = currentPhrase.translation.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(42.dp))

            // Big Tap-to-count circle
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .scale(circleScale)
                    .background(Color.White.copy(alpha = 0.02f), CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        isTapped = true
                        viewModel.incrementDhikr()
                    }
                    .testTag("dhikr_tap_circle"),
                contentAlignment = Alignment.Center
            ) {
                // Concentric circles
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .background(accentColor.copy(alpha = 0.05f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(accentColor.copy(alpha = 0.10f), CircleShape)
                )

                // Outer border ring
                Surface(
                    shape = CircleShape,
                    color = Color.Transparent,
                    border = BorderStroke(2.dp, accentColor.copy(alpha = 0.35f)),
                    modifier = Modifier.size(210.dp)
                ) {}

                // Count Number
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = count.toString(),
                        fontSize = 80.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = (-2).sp
                    )
                }
            }

            LaunchedEffect(isTapped) {
                if (isTapped) {
                    kotlinx.coroutines.delay(80)
                    isTapped = false
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = t.tapToCount.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp,
                color = TextSubtle
            )
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}
