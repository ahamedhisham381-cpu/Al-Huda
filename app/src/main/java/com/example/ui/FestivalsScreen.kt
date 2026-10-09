package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HolyFestival
import com.example.ui.theme.*
import com.example.util.Translations

@Composable
fun FestivalsScreen(
    viewModel: AlHudaViewModel,
    modifier: Modifier = Modifier
) {
    val lang by viewModel.settingsManager.lang.collectAsState()
    val accentId by viewModel.settingsManager.accent.collectAsState()
    val accentColor = getAccentColor(accentId)
    val hijriDate by viewModel.hijriDate.collectAsState()
    val festivals = viewModel.festivals
    val t = Translations.get(lang)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("festivals_screen")
    ) {
        Column(modifier = Modifier.padding(top = 12.dp, bottom = 16.dp)) {
            Text(
                text = t.upcomingEvents,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${t.festivalsDesc} • ${hijriDate?.year ?: "1446"} AH",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = TextMuted
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            items(festivals, key = { it.nameEn }) { fest ->
                FestivalCardItem(
                    festival = fest,
                    accentColor = accentColor,
                    isArabic = lang == "ar"
                )
            }
        }
    }
}

@Composable
fun FestivalCardItem(
    festival: HolyFestival,
    accentColor: Color,
    isArabic: Boolean
) {
    val categoryIcon = when (festival.category.lowercase()) {
        "celebration" -> Icons.Default.Star
        "holy month" -> Icons.Default.NightsStay
        else -> Icons.Default.Schedule
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("festival_card_${festival.nameEn}"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = GlassWhite05),
        border = BorderStroke(1.dp, BorderWhite10)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Category Icon & Dual Date badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category icon with soft badge
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = festival.category,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Dual Dates
                Column(horizontalAlignment = Alignment.End) {
                    Box(
                        modifier = Modifier
                            .background(GlassWhite10, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = festival.hijriDate.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = accentColor,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = festival.gregorianDate,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Names (Primary and Secondary based on language)
            val mainName = if (isArabic) festival.nameAr else festival.nameEn
            val subName = if (isArabic) festival.nameEn else festival.nameAr

            Text(
                text = mainName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subName,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = accentColor.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Category tag
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(accentColor, CircleShape)
                )
                Text(
                    text = festival.category.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = TextSubtle
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Significance
            val significanceText = if (isArabic) festival.significanceAr else festival.significanceEn
            Text(
                text = significanceText,
                fontSize = 13.sp,
                fontStyle = FontStyle.Italic,
                color = Color(0xFFCBD5E1),
                lineHeight = 20.sp
            )
        }
    }
}
