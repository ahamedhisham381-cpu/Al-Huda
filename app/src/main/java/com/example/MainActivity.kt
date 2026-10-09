package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.*
import com.example.ui.theme.*
import com.example.util.Translations

class MainActivity : ComponentActivity() {

    private val viewModel: AlHudaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val currentTheme by viewModel.settingsManager.theme.collectAsState()
            val currentAccent by viewModel.settingsManager.accent.collectAsState()
            val currentLang by viewModel.settingsManager.lang.collectAsState()
            val showSplash by viewModel.showSplash.collectAsState()
            val accentColor = getAccentColor(currentAccent)

            AlHudaTheme(themeId = currentTheme, accentId = currentAccent) {
                val layoutDirection = if (currentLang == "ar" || currentLang == "ur") LayoutDirection.Rtl else LayoutDirection.Ltr

                CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (showSplash) {
                            StartupSplashScreen(accentColor = accentColor)
                        } else {
                            MainAppScaffold(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainAppScaffold(viewModel: AlHudaViewModel) {
    val activeTab by viewModel.activeTab.collectAsState()
    val activeSubFeature by viewModel.activeSubFeature.collectAsState()
    val selectedSurah by viewModel.selectedSurah.collectAsState()
    val showSettings by viewModel.showSettings.collectAsState()
    val isOffline by viewModel.isOfflineMode.collectAsState()
    val currentLang by viewModel.settingsManager.lang.collectAsState()
    val currentAccent by viewModel.settingsManager.accent.collectAsState()
    val accentColor = getAccentColor(currentAccent)
    val t = Translations.get(currentLang)

    // Back handling for drilldowns and readers
    BackHandler(enabled = activeSubFeature != null || selectedSurah != null) {
        if (activeSubFeature != null) {
            viewModel.closeSubFeature()
        } else if (selectedSurah != null) {
            viewModel.clearSelectedSurah()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("main_scaffold"),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (activeSubFeature == null) {
                BottomNavDock(
                    activeTab = activeTab,
                    onSelectTab = { viewModel.setActiveTab(it) },
                    accentColor = accentColor,
                    t = t
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Offline notification banner
            AnimatedVisibility(
                visible = isOffline,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF59E0B))
                        .padding(vertical = 6.dp, horizontal = 12.dp)
                        .testTag("offline_banner"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Offline",
                            tint = Color.Black,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = t.offlineMode,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }

            // Top Header (shown on main tabs, hidden during sub-feature drilldowns)
            if (activeSubFeature == null) {
                AppHeader(
                    title = t.appName,
                    tagline = t.tagline,
                    accentColor = accentColor,
                    onSettingsClick = { viewModel.setShowSettings(true) }
                )
            }

            // Main Content Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (activeSubFeature != null) {
                    when (activeSubFeature) {
                        "qibla" -> QiblaCompassView(viewModel = viewModel) { viewModel.closeSubFeature() }
                        "names99" -> NamesOfAllahView(viewModel = viewModel) { viewModel.closeSubFeature() }
                        "hadith" -> HadithLibraryView(viewModel = viewModel) { viewModel.closeSubFeature() }
                        "duas" -> DuaCollectionView(viewModel = viewModel) { viewModel.closeSubFeature() }
                        "quiz" -> IslamicQuizView(viewModel = viewModel) { viewModel.closeSubFeature() }
                        "arabic" -> LearnArabicView(viewModel = viewModel) { viewModel.closeSubFeature() }
                        "seerah" -> SeerahTimelineView(viewModel = viewModel) { viewModel.closeSubFeature() }
                        "kids" -> KidsCornerView(viewModel = viewModel) { viewModel.closeSubFeature() }
                        "ramadan" -> RamadanTrackerView(viewModel = viewModel) { viewModel.closeSubFeature() }
                        "hajj" -> HajjUmrahView(viewModel = viewModel) { viewModel.closeSubFeature() }
                        "tracker" -> PrayerTrackerView(viewModel = viewModel) { viewModel.closeSubFeature() }
                        "deeds" -> GoodDeedsView(viewModel = viewModel) { viewModel.closeSubFeature() }
                        "istikharah" -> IstikharahView(viewModel = viewModel) { viewModel.closeSubFeature() }
                        "fatwa" -> FatwaFaqView(viewModel = viewModel) { viewModel.closeSubFeature() }
                        "mosques" -> NearbyMosquesView(viewModel = viewModel) { viewModel.closeSubFeature() }
                        "news" -> IslamicNewsView(viewModel = viewModel) { viewModel.closeSubFeature() }
                        "community" -> CommunityReflectionsView(viewModel = viewModel) { viewModel.closeSubFeature() }
                        "hifz" -> HifzTrackerView(viewModel = viewModel) { viewModel.closeSubFeature() }
                        else -> HomeScreen(viewModel = viewModel)
                    }
                } else {
                    when (activeTab) {
                        "home" -> HomeScreen(viewModel = viewModel)
                        "quran" -> QuranScreen(viewModel = viewModel)
                        "dhikr" -> DhikrScreen(viewModel = viewModel)
                        "features" -> FeaturesHubScreen(viewModel = viewModel)
                        "festivals" -> FestivalsScreen(viewModel = viewModel)
                        "library" -> LibraryScreen(viewModel = viewModel)
                        else -> HomeScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }

    // Settings Dialog
    if (showSettings) {
        SettingsDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.setShowSettings(false) }
        )
    }
}

@Composable
fun AppHeader(
    title: String,
    tagline: String,
    accentColor: Color,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 10.dp)
            .testTag("app_header"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Enhanced Master Logo Emblem
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(accentColor.copy(alpha = 0.2f))
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.alhuda_minimal_icon_1791477132069),
                    contentDescription = "Al-Huda Minimalist Emblem",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(13.dp))
                )
            }

            Column {
                Text(
                    text = title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    lineHeight = 26.sp
                )
                Text(
                    text = tagline.uppercase(),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp,
                    color = accentColor
                )
            }
        }

        // Settings Button
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .size(44.dp)
                .background(GlassWhite10, RoundedCornerShape(14.dp))
                .testTag("settings_button")
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
fun BottomNavDock(
    activeTab: String,
    onSelectTab: (String) -> Unit,
    accentColor: Color,
    t: com.example.util.AppTranslations
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("bottom_nav_dock"),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp),
            shape = RoundedCornerShape(34.dp),
            color = GlassWhite15,
            border = BorderStroke(1.dp, BorderWhite10),
            shadowElevation = 12.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavTabItem(
                    id = "home",
                    label = t.home,
                    icon = Icons.Default.Home,
                    isActive = activeTab == "home",
                    accentColor = accentColor,
                    onClick = { onSelectTab("home") }
                )

                NavTabItem(
                    id = "quran",
                    label = t.quran,
                    icon = Icons.Default.MenuBook,
                    isActive = activeTab == "quran",
                    accentColor = accentColor,
                    onClick = { onSelectTab("quran") }
                )

                NavTabItem(
                    id = "dhikr",
                    label = t.dhikr,
                    icon = Icons.Default.AddCircle,
                    isActive = activeTab == "dhikr",
                    accentColor = accentColor,
                    onClick = { onSelectTab("dhikr") }
                )

                NavTabItem(
                    id = "features",
                    label = "Hub",
                    icon = Icons.Default.Widgets,
                    isActive = activeTab == "features",
                    accentColor = accentColor,
                    onClick = { onSelectTab("features") }
                )

                NavTabItem(
                    id = "festivals",
                    label = t.festivals,
                    icon = Icons.Default.Event,
                    isActive = activeTab == "festivals",
                    accentColor = accentColor,
                    onClick = { onSelectTab("festivals") }
                )
            }
        }
    }
}

@Composable
fun NavTabItem(
    id: String,
    label: String,
    icon: ImageVector,
    isActive: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    val cleanLabel = remember(label) {
        label.split(" ")[0].trim()
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(22.dp))
            .clickable { onClick() }
            .then(
                if (isActive) {
                    Modifier.background(accentColor.copy(alpha = 0.22f), RoundedCornerShape(22.dp))
                } else Modifier
            )
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("nav_tab_$id"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = cleanLabel,
                tint = if (isActive) accentColor else TextMuted,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = cleanLabel,
                fontSize = 10.sp,
                fontWeight = if (isActive) FontWeight.Black else FontWeight.SemiBold,
                color = if (isActive) Color.White else TextMuted
            )
        }
    }
}
