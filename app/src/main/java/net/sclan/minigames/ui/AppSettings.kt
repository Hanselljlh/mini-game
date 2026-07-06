package net.sclan.minigames.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/**
 * App-wide accessibility settings, read directly by Composables so a change
 * recomposes everything that depends on it. Persistence is handled by
 * [net.sclan.minigames.data.ScoreRepository], which hydrates these on launch.
 */
object AppSettings {
    var colorBlind by mutableStateOf(false)
}

// Default vibrant palette used across the color-matching games.
internal val sortColorsDefault = listOf(
    Color(0xFFE53935), Color(0xFF1E88E5), Color(0xFF43A047), Color(0xFFFDD835),
    Color(0xFF8E24AA), Color(0xFFFB8C00), Color(0xFF00ACC1), Color(0xFFEC407A),
    Color(0xFF7CB342), Color(0xFF5C6BC0)
)

// Colorblind-friendly palette (Okabe–Ito based) — hues chosen to stay
// distinguishable across deuteranopia / protanopia / tritanopia.
internal val sortColorsColorBlind = listOf(
    Color(0xFF000000), Color(0xFFE69F00), Color(0xFF56B4E9), Color(0xFF009E73),
    Color(0xFFF0E442), Color(0xFF0072B2), Color(0xFFD55E00), Color(0xFFCC79A7),
    Color(0xFF999999), Color(0xFFFFFFFF)
)

/** The active color-matching palette; switches when the colorblind option is on. */
internal val sortColors: List<Color>
    get() = if (AppSettings.colorBlind) sortColorsColorBlind else sortColorsDefault
