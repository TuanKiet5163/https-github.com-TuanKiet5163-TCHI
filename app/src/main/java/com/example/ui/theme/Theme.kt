package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = TchiPrimary,
    onPrimary = Color(0xFF031625),
    primaryContainer = TchiSecondaryBlue,
    onPrimaryContainer = Color(0xFFBAE6FD),
    secondary = TchiSecondaryBlue,
    onSecondary = Color(0xFFE0F2FE),
    background = TchiBackground,
    onBackground = TchiWhite,
    surface = TchiCard,
    onSurface = TchiWhite,
    surfaceVariant = TchiSurfaceVariant,
    onSurfaceVariant = TchiSecondaryText,
    outline = TchiBorder,
    error = TchiExpense,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0284C7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBAE6FD),
    onPrimaryContainer = Color(0xFF0C4A6E),
    secondary = Color(0xFF0369A1),
    onSecondary = Color.White,
    background = Color(0xFFF0F6FA),
    onBackground = Color(0xFF071521),
    surface = Color.White,
    onSurface = Color(0xFF071521),
    surfaceVariant = Color(0xFFE2EBF1),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    error = TchiExpense,
    onError = Color.White
)

@Composable
fun TchiTheme(
    darkTheme: Boolean = true, // Dark mode default as specified
    dynamicColor: Boolean = false, // Keep branded TCHI look
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
