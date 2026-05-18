package com.ginogipsy.sanmartinoapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = AutumnOrange,
    onPrimary = Color.White,
    primaryContainer = AutumnYellow,
    onPrimaryContainer = WoodBrownDark,
    secondary = WoodBrown,
    onSecondary = Color.White,
    secondaryContainer = WoodBrownLight,
    onSecondaryContainer = WoodBrownDark,
    tertiary = WineRed,
    onTertiary = Color.White,
    background = ParchmentCream,
    onBackground = WoodBrownDark,
    surface = ParchmentCream,
    onSurface = WoodBrownDark,
    surfaceVariant = ParchmentSurface,
    onSurfaceVariant = WoodBrown,
)

private val DarkColors = darkColorScheme(
    primary = AutumnOrangeDark,
    onPrimary = WoodBrownDark,
    primaryContainer = AutumnYellowDark,
    onPrimaryContainer = WoodBrownDark,
    secondary = WoodBrownLight,
    onSecondary = WoodBrownDark,
    tertiary = WineRedDark,
    onTertiary = WoodBrownDark,
    background = Color(0xFF1F140A),
    onBackground = Color(0xFFFFE9C2),
    surface = Color(0xFF2A1B0D),
    onSurface = Color(0xFFFFE9C2),
    surfaceVariant = Color(0xFF3E2814),
    onSurfaceVariant = Color(0xFFE0C28E),
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
