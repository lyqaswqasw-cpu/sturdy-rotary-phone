package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val FoulDarkColorScheme = darkColorScheme(
    primary = FoulPurpleBright,
    onPrimary = Color.White,
    primaryContainer = FoulPurpleDeep,
    onPrimaryContainer = Color.White,
    secondary = FoulCardElevated,
    onSecondary = Color.White,
    secondaryContainer = FoulCardElevated,
    onSecondaryContainer = Color.White,
    tertiary = FoulGoldStar,
    background = FoulBlackBg,
    onBackground = FoulTextPrimary,
    surface = FoulSurfaceDark,
    onSurface = FoulTextPrimary,
    surfaceVariant = FoulCardDark,
    onSurfaceVariant = FoulTextSecondary,
    outline = FoulDivider
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = FoulDarkColorScheme,
            typography = Typography,
            content = content
        )
    }
}
