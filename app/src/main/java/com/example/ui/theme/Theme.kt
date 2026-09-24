package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = ClaudeTerracotta,
    onPrimary = Color.White,
    primaryContainer = ClaudeTerracottaSoft,
    onPrimaryContainer = ClaudeTerracottaHover,
    secondary = ClaudeAmber,
    onSecondary = Color.White,
    background = ClaudeCanvasLight,
    onBackground = ClaudeTextDark,
    surface = ClaudeCanvasLight,
    onSurface = ClaudeTextDark,
    surfaceVariant = ClaudeUserBubbleLight,
    onSurfaceVariant = ClaudeBodyLight,
    outline = ClaudePillBorderLight
)

private val DarkColorScheme = darkColorScheme(
    primary = ClaudeTerracotta,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF38231C),
    onPrimaryContainer = Color(0xFFFFDBCF),
    secondary = ClaudeAmber,
    onSecondary = Color.Black,
    background = ClaudeCanvasDark,
    onBackground = ClaudeTextLight,
    surface = ClaudeCanvasDark,
    onSurface = ClaudeTextLight,
    surfaceVariant = ClaudeUserBubbleDark,
    onSurfaceVariant = ClaudeBodyDark,
    outline = ClaudePillBorderDark
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
