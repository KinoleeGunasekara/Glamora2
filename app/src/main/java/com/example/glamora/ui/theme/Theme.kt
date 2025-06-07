package com.example.glamora.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Adjusted LightPinkColorScheme for more visibility
private val LightPinkColorScheme = lightColorScheme(
    primary = Color(0xFFE91E63), // A more vibrant and visible pink (similar to Pink 500)
    onPrimary = Color.White,
    secondary = Color(0xFFF06292), // A slightly softer, but still distinct pink
    onSecondary = Color.Black,
    background = Color(0xFFFFF0F5), // Keeping a very light background for the light theme
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black
)

// DarkPinkColorScheme colors are already quite strong, keeping them as they are
private val DarkPinkColorScheme = darkColorScheme(
    primary = Color(0xFFFF69B4), // Hot Pink
    onPrimary = Color.Black,
    secondary = Color(0xFFFF1493), // Deep Pink
    onSecondary = Color.White,
    background = Color(0xFF121212),
    onBackground = Color.White,
    surface = Color(0xFF1F1F1F),
    onSurface = Color.White
)

@Composable
fun GlamoraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkPinkColorScheme else LightPinkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Assuming Typography is defined elsewhere or using Material3 defaults
        content = content
    )
}
