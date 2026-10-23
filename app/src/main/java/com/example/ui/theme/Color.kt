package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Base Brand Colors
val Emerald500 = Color(0xFF10B981)
val Emerald600 = Color(0xFF059669)
val Emerald400 = Color(0xFF34D399)

val Amber500 = Color(0xFFF59E0B)
val Blue500 = Color(0xFF3B82F6)
val Rose500 = Color(0xFFF43F5E)
val Indigo500 = Color(0xFF6366F1)
val Purple500 = Color(0xFFA855F7)
val Cyan500 = Color(0xFF06B6D4)
val Orange500 = Color(0xFFF97316)

// Theme Backgrounds
val BgSlate950 = Color(0xFF020617)
val BgStone950 = Color(0xFF0C0A09)
val BgSlate900 = Color(0xFF0F172A)
val BgZinc950 = Color(0xFF09090B)

// Surface Colors
val SurfaceSlate900 = Color(0xFF0F172A)
val SurfaceStone900 = Color(0xFF1C1917)
val SurfaceSlate800 = Color(0xFF1E293B)
val SurfaceZinc900 = Color(0xFF18181B)

// Glass and Card Colors
val GlassWhite05 = Color(0x0DFFFFFF)
val GlassWhite10 = Color(0x1AFFFFFF)
val GlassWhite15 = Color(0x26FFFFFF)
val BorderWhite10 = Color(0x1AFFFFFF)
val BorderWhite20 = Color(0x33FFFFFF)

// Text Colors
val TextWhite = Color(0xFFFFFFFF)
val TextMuted = Color(0xFF94A3B8)
val TextSubtle = Color(0xFF64748B)

data class ThemePalette(
    val id: String,
    val name: String,
    val background: Color,
    val surface: Color,
    val defaultAccent: Color
)

val AvailableThemes = listOf(
    ThemePalette("emerald", "Emerald Night", BgSlate950, SurfaceSlate900, Emerald500),
    ThemePalette("gold", "Desert Gold", BgStone950, SurfaceStone900, Amber500),
    ThemePalette("sapphire", "Royal Sapphire", BgSlate900, SurfaceSlate800, Blue500),
    ThemePalette("ruby", "Crimson Sunset", BgZinc950, SurfaceZinc900, Rose500)
)

data class AccentOption(
    val id: String,
    val name: String,
    val color: Color
)

val AvailableAccents = listOf(
    AccentOption("emerald", "Emerald", Emerald500),
    AccentOption("amber", "Amber", Amber500),
    AccentOption("blue", "Sapphire", Blue500),
    AccentOption("rose", "Rose", Rose500),
    AccentOption("indigo", "Indigo", Indigo500),
    AccentOption("purple", "Purple", Purple500),
    AccentOption("cyan", "Cyan", Cyan500),
    AccentOption("orange", "Orange", Orange500)
)

fun getThemePalette(id: String): ThemePalette {
    return AvailableThemes.firstOrNull { it.id == id } ?: AvailableThemes[0]
}

fun getAccentColor(id: String): Color {
    return AvailableAccents.firstOrNull { it.id == id }?.color ?: Emerald500
}

/** Quran reading page color schemes — WeMuslim-style "reading theme" presets. */
data class QuranReadingColors(
    val id: String,
    val label: String,
    val pageBackground: Color,
    val cardBackground: Color,
    val arabicText: Color,
    val isLight: Boolean = false
)

val QuranReadingThemes = listOf(
    QuranReadingColors("default", "Default", BgSlate950, Color(0x0DFFFFFF), Color.White),
    QuranReadingColors("night", "Night", Color.Black, Color(0x0DFFFFFF), Color(0xFFE5E5E5)),
    QuranReadingColors("green", "Classic Green", Color(0xFF07210F), Color(0x1A10B981), Color(0xFFECFDF5)),
    QuranReadingColors("sepia", "Sepia", Color(0xFFF1E5C8), Color(0x33FFFFFF), Color(0xFF2A1B06), isLight = true)
)

fun getQuranReadingColors(id: String): QuranReadingColors =
    QuranReadingThemes.firstOrNull { it.id == id } ?: QuranReadingThemes[0]
