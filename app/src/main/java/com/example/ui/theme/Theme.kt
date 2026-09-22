package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ForgeCyanBright,
    onPrimary = Color(0xFF003554),
    primaryContainer = ForgeCyanPrimary,
    onPrimaryContainer = Color(0xFFE0F2FE),
    secondary = ForgeGoldAccent,
    onSecondary = Color(0xFF452B00),
    secondaryContainer = Color(0xFF784D00),
    onSecondaryContainer = Color(0xFFFFDEA8),
    tertiary = ForgeEmerald,
    background = ForgeNavyDark,
    onBackground = TextPrimaryDark,
    surface = ForgeSurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = ForgeSurfaceElevated,
    onSurfaceVariant = TextSecondaryDark,
    outline = ForgeBorder,
    error = ForgeRose
)

private val LightColorScheme = lightColorScheme(
    primary = ForgeCyanPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF003554),
    secondary = ForgeGoldAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDEA8),
    onSecondaryContainer = Color(0xFF452B00),
    tertiary = ForgeEmerald,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    error = ForgeRose
)

@Composable
fun MiniAppForgeTheme(
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
