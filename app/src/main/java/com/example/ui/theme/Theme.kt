package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalKeypadColors = staticCompositionLocalOf {
    ThemePalettes.getKeypadColors(ThemePreset.PROFESSIONAL_POLISH)
}

@Composable
fun ScientificCalculatorTheme(
    preset: ThemePreset = ThemePreset.PROFESSIONAL_POLISH,
    content: @Composable () -> Unit
) {
    val colorScheme = ThemePalettes.getColorScheme(preset)
    val keypadColors = ThemePalettes.getKeypadColors(preset)

    CompositionLocalProvider(LocalKeypadColors provides keypadColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
