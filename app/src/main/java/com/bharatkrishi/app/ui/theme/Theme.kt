package com.bharatkrishi.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Define your app colors
private val BharatKrishiGreen = Color(0xFF2E7D32)
private val BharatKrishiLightGreen = Color(0xFF4CAF50)
private val BharatKrishiBlue = Color(0xFF1976D2)
private val BharatKrishiOrange = Color(0xFFFF9800)

private val DarkColorScheme = darkColorScheme(
    primary = BharatKrishiGreen,
    secondary = BharatKrishiLightGreen,
    tertiary = BharatKrishiBlue,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = BharatKrishiGreen,
    onPrimary = Color.White,
    secondary = BharatKrishiLightGreen,
    onSecondary = Color.White,
    tertiary = BharatKrishiBlue,
    onTertiary = Color.White,
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

@Composable
fun BharatKrishiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disable dynamic colors for consistent branding
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}