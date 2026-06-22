package com.example.macnatic_tape_swipe_reader.view.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val SoftWhiteBackground = Color.White // Pure White Background
val CardWhite = Color(0xFFFFFFFF)           // Pure white for cards
val SoftTeal = Color(0xFF2563EB)            // Royal Blue (Primary Accent)
val OceanBlue = Color(0xFF0284C7)           // Sky Blue (Secondary Accent)
val SuccessGreen = Color(0xFF059669)        // Emerald Green (Success Indicator)
val SlateDark = Color(0xFF0F172A)           // Very Dark Slate for primary text
val SlateLight = Color(0xFF475569)          // Medium Slate for secondary text
val BorderLight = Color(0xFFE2E8F0)         // Soft gray border
val AlertAmber = Color(0xFFD97706)          // Amber 600
val DangerRed = Color(0xFFEF4444)           // Red 500
val OffWhite = Color(0xFFF8FAFC)

private val LightColorScheme = lightColorScheme(
    primary = SoftTeal,
    secondary = OceanBlue,
    background = SoftWhiteBackground,
    surface = CardWhite,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = SlateDark,
    onSurface = SlateDark
)

private val DarkColorScheme = darkColorScheme(
    primary = SoftTeal,
    secondary = OceanBlue,
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = OffWhite,
    onSurface = OffWhite
)

@Composable
fun MacnaticTheme(
    darkTheme: Boolean = false, // Default to light mode (white UI) as requested
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    androidx.compose.material3.MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
