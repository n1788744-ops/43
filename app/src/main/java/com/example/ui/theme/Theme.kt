package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val WitchDarkColorScheme = darkColorScheme(
    primary = LanternAmber,
    onPrimary = Color(0xFF1A1105),
    primaryContainer = LanternAmberDim,
    onPrimaryContainer = ParchmentText,
    secondary = EmeraldWitchlight,
    onSecondary = Color(0xFF041D14),
    secondaryContainer = EmeraldDeep,
    onSecondaryContainer = ParchmentText,
    tertiary = ArcaneViolet,
    onTertiary = Color(0xFF130B24),
    background = ObsidianBg,
    onBackground = ParchmentText,
    surface = ManorSurface,
    onSurface = ParchmentText,
    surfaceVariant = ManorSurfaceElevated,
    onSurfaceVariant = MistSecondary,
    outline = ManorCardBorder,
    error = BloodCrimson,
    onError = Color.White,
    errorContainer = BloodDark,
    onErrorContainer = Color(0xFFFFDAD9)
)

@Composable
fun WitchShadowTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = WitchDarkColorScheme,
        typography = Typography,
        content = content
    )
}
