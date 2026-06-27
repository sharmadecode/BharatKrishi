package com.bharatkrishi.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DarkOrangeAccent,
    onPrimary = Color.Black, 
    secondary = DarkSurfaceCard, 
    onSecondary = DarkTextMain,
    tertiary = DarkOrangeAccent,
    background = DarkGreyBackground,
    onBackground = DarkTextMain,
    surface = DarkSurfaceCard,
    onSurface = DarkTextMain,
    surfaceVariant = Color(0xFF333333),
    onSurfaceVariant = DarkTextSecondary,
    error = ErrorRed
)

private val LightColorScheme = lightColorScheme(
    primary = UserOrange,
    onPrimary = Color.White,
    secondary = UserBeigeSecondary, 
    onSecondary = UserDarkText,
    tertiary = UserOrange,
    background = UserBeigeMain, 
    onBackground = UserDarkText,

    surface = Color.White, 
    onSurface = UserDarkText,
    surfaceVariant = UserBeigeSecondary, 
    onSurfaceVariant = UserDarkText,
    error = ErrorRed
)

@Composable
fun BharatKrishiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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