package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun AlHudaTheme(
    themeId: String = "emerald",
    accentId: String = "emerald",
    content: @Composable () -> Unit
) {
    val palette = getThemePalette(themeId)
    val accentColor = getAccentColor(accentId)

    val colorScheme = darkColorScheme(
        primary = accentColor,
        onPrimary = Color.White,
        primaryContainer = accentColor.copy(alpha = 0.2f),
        onPrimaryContainer = Color.White,
        secondary = accentColor,
        onSecondary = Color.White,
        tertiary = Color(0xFFF59E0B),
        onTertiary = Color.White,
        background = palette.background,
        onBackground = Color.White,
        surface = palette.surface,
        onSurface = Color.White,
        surfaceVariant = palette.surface.copy(alpha = 0.8f),
        onSurfaceVariant = TextMuted,
        outline = BorderWhite10,
        outlineVariant = BorderWhite20
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
