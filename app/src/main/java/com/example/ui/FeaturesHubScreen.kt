package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class HubFeatureItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val category: String // "Worship", "Learning", "Community"
)

@Composable
fun FeaturesHubScreen(
    viewModel: AlHudaViewModel,
    modifier: Modifier = Modifier
) {
    val accentId by viewModel.settingsManager.accent.collectAsState()
    val accentColor = getAccentColor(accentId)

    var selectedFilter by remember { mutableStateOf("All") }
    val categories = listOf("All", "Worship", "Learning", "Community")

    val allFeatures = remember {
        listOf(
            HubFeatureItem("qibla", "Qibla Compass", "Direction toward Makkah", Icons.Default.Explore, "Worship"),
            HubFeatureItem("names99", "99 Names of Allah", "Asma ul Husna & meanings", Icons.Default.AutoAwesome, "Worship"),
            HubFeatureItem("duas", "Dua Collection", "Hisn al-Muslim supplications", Icons.Default.VolunteerActivism, "Worship"),
            HubFeatureItem("tracker", "5 Prayers Log", "Daily Salah tracker & streak", Icons.Default.CheckCircle, "Worship"),
            HubFeatureItem("deeds", "Good Deeds Log", "Daily Sunnah habits checklist", Icons.Default.Favorite, "Worship"),
            HubFeatureItem("ramadan", "Ramadan Tracker", "Fasting log & Juz progress", Icons.Default.NightsStay, "Worship"),
            HubFeatureItem("hajj", "Hajj & Umrah", "Step-by-step ritual guide", Icons.Default.Landscape, "Worship"),
            HubFeatureItem("istikharah", "Istikharah Guide", "Method & full Arabic dua", Icons.Default.Lightbulb, "Worship"),
            HubFeatureItem("hadith", "Hadith Library", "Bukhari, Muslim & Nawawi", Icons.Default.FormatQuote, "Learning"),
            HubFeatureItem("quiz", "Islamic Quiz", "Test Quran & Seerah knowledge", Icons.Default.Psychology, "Learning"),
            HubFeatureItem("arabic", "Learn Arabic", "Quranic vocabulary & roots", Icons.Default.Translate, "Learning"),
            HubFeatureItem("seerah", "Seerah Timeline", "Life of Prophet Muhammad ﷺ", Icons.Default.History, "Learning"),
            HubFeatureItem("kids", "Kids Corner", "Stories with morals & duas", Icons.Default.ChildCare, "Learning"),
            HubFeatureItem("fatwa", "Fatwa & Rulings FAQ", "Classical scholarship guidance", Icons.Default.Gavel, "Learning"),
            HubFeatureItem("hifz", "Hifz Memorization", "Track Quran memorized surahs", Icons.Default.Bookmark, "Learning"),
            HubFeatureItem("mosques", "Nearby Mosques", "Local mosque locator & times", Icons.Default.LocationOn, "Community"),
            HubFeatureItem("news", "Islamic News", "Inspiring stories & articles", Icons.Default.Article, "Community"),
            HubFeatureItem("community", "Reflections Board", "Global seekers reflections", Icons.Default.Forum, "Community")
        )
    }

    val filteredList = remember(selectedFilter) {
        if (selectedFilter == "All") allFeatures
        else allFeatures.filter { it.category == selectedFilter }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("features_hub_screen")
    ) {
        Text(
            text = "Islamic Features Hub",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
        )
        Text(
            text = "30 Comprehensive tools for worship, learning & community",
            fontSize = 11.sp,
            color = TextMuted,
            modifier = Modifier.padding(bottom = 14.dp)
        )

        // Filter Pills Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                val isSelected = selectedFilter == cat
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) accentColor else GlassWhite10,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable {
                            selectedFilter = cat
                            viewModel.audioHelper.playTick()
                        }
                ) {
                    Text(
                        text = cat,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else TextMuted,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // Features Grid
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            items(filteredList, key = { it.id }) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .clickable { viewModel.openSubFeature(item.id) }
                        .testTag("hub_feature_${item.id}"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = GlassWhite05),
                    border = BorderStroke(1.dp, BorderWhite10)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(accentColor.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = accentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = item.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.subtitle,
                                fontSize = 10.sp,
                                color = TextMuted,
                                lineHeight = 14.sp,
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        }
    }
}
