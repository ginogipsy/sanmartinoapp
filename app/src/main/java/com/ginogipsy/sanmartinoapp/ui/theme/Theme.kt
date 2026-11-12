package com.ginogipsy.sanmartinoapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = SanMartinoWine,
    onPrimary = Color.White,
    primaryContainer = SanMartinoDeep,
    onPrimaryContainer = Color.White,
    secondary = SanMartinoGold,
    onSecondary = SanMartinoInk,
    secondaryContainer = SanMartinoGold,
    onSecondaryContainer = SanMartinoInk,
    tertiary = SanMartinoMarkerRed,
    onTertiary = Color.White,
    background = SanMartinoCream,
    onBackground = SanMartinoInk,
    surface = SanMartinoCream,
    onSurface = SanMartinoInk,
    surfaceVariant = SanMartinoLine,
    onSurfaceVariant = SanMartinoMuted,
)

private val DarkColors = darkColorScheme(
    primary = SanMartinoWineDark,
    onPrimary = SanMartinoCream,
    primaryContainer = SanMartinoWine,
    onPrimaryContainer = SanMartinoCream,
    secondary = SanMartinoGoldDark,
    onSecondary = SanMartinoCreamDark,
    tertiary = SanMartinoMarkerRed,
    onTertiary = Color.White,
    background = SanMartinoCreamDark,
    onBackground = SanMartinoInkDark,
    surface = SanMartinoCreamDark,
    onSurface = SanMartinoInkDark,
    surfaceVariant = SanMartinoDeep,
    onSurfaceVariant = SanMartinoMuted,
)

@Composable
fun SanMartinoAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
