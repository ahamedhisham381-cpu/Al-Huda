package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LibraryItem
import com.example.ui.theme.*
import com.example.util.Translations

@Composable
fun LibraryScreen(
    viewModel: AlHudaViewModel,
    modifier: Modifier = Modifier
) {
    val lang by viewModel.settingsManager.lang.collectAsState()
    val accentId by viewModel.settingsManager.accent.collectAsState()
    val accentColor = getAccentColor(accentId)
    val library = viewModel.libraryContent
    val t = Translations.get(lang)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("library_screen")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = t.library,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Spacer(modifier = Modifier.width(16.dp))
            Divider(
                color = accentColor.copy(alpha = 0.3f),
                modifier = Modifier.weight(1f)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            item {
                LibraryCardItem(
                    label = "The Noble Quran",
                    item = library.quranVerse,
                    icon = Icons.Default.MenuBook,
                    accentColor = accentColor
                )
            }
            item {
                LibraryCardItem(
                    label = "Authentic Hadith",
                    item = library.hadith,
                    icon = Icons.Default.FormatQuote,
                    accentColor = accentColor
                )
            }
            item {
                LibraryCardItem(
                    label = "Inspirational Story",
                    item = library.story,
                    icon = Icons.Default.AutoStories,
                    accentColor = accentColor
                )
            }
        }
    }
}

@Composable
fun LibraryCardItem(
    label: String,
    item: LibraryItem,
    icon: ImageVector,
    accentColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("library_card_${item.title}"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = GlassWhite05),
        border = BorderStroke(1.dp, BorderWhite10)
    ) {
        Column(
            modifier = Modifier.padding(22.dp)
        ) {
            // Label & Icon badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = label.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = accentColor
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Title
            Text(
                text = item.title,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Arabic text
            if (item.arabicContent.isNotBlank()) {
                Text(
                    text = item.arabicContent,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.End,
                    lineHeight = 38.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // English text
            Text(
                text = item.englishContent,
                fontSize = 14.sp,
                fontStyle = FontStyle.Italic,
                color = Color(0xFFE2E8F0),
                lineHeight = 22.sp
            )

            // Lesson box
            if (item.explanation.isNotBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = BorderWhite10)
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "LESSON",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = accentColor.copy(alpha = 0.85f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.explanation,
                    fontSize = 13.sp,
                    color = TextMuted,
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Source tag
            Box(
                modifier = Modifier
                    .align(Alignment.End)
                    .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = item.source,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMuted
                )
            }
        }
    }
}
