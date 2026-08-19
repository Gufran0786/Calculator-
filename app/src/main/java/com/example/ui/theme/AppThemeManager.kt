package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

enum class ThemePreset(val displayName: String, val description: String) {
    PROFESSIONAL_POLISH("Professional Polish", "Sleek obsidian with M3 lavender, refined contrast & high precision"),
    OBSIDIAN_NEON("Obsidian Neon", "Deep slate with electric cyan and neon accents"),
    MIDNIGHT_OLED("Midnight OLED", "Pure true pitch black with emerald & mint"),
    CYBERPUNK("Cyberpunk Void", "Dark violet with electric magenta & amber"),
    NORDIC_SLATE("Nordic Slate", "Cool slate blue with arctic sapphire accents"),
    TITANIUM_GOLD("Titanium Gold", "Carbon dark with luxury warm gold")
}

data class KeypadColors(
    val numberBg: Color,
    val numberText: Color,
    val operatorBg: Color,
    val operatorText: Color,
    val functionBg: Color,
    val functionText: Color,
    val equalsBg: Color,
    val equalsText: Color,
    val clearBg: Color,
    val clearText: Color,
    val displayBg: Color,
    val displayText: Color,
    val displaySubText: Color,
    val accentGlow: Color
)

object ThemePalettes {

    fun getKeypadColors(preset: ThemePreset): KeypadColors {
        return when (preset) {
            ThemePreset.PROFESSIONAL_POLISH -> KeypadColors(
                numberBg = Color(0xFF2B2930),
                numberText = Color(0xFFE6E1E5),
                operatorBg = Color(0xFF4F378B),
                operatorText = Color(0xFFEADDFF),
                functionBg = Color(0xFF36343B),
                functionText = Color(0xFFD0BCFF),
                equalsBg = Color(0xFFD0BCFF),
                equalsText = Color(0xFF381E72),
                clearBg = Color(0xFF601410),
                clearText = Color(0xFFF2B8B5),
                displayBg = Color(0xFF141218),
                displayText = Color(0xFFE6E1E5),
                displaySubText = Color(0xFF938F99),
                accentGlow = Color(0xFFD0BCFF)
            )
            ThemePreset.OBSIDIAN_NEON -> KeypadColors(
                numberBg = Color(0xFF161F30),
                numberText = Color(0xFFE2E8F0),
                operatorBg = Color(0xFF0F3956),
                operatorText = Color(0xFF38BDF8),
                functionBg = Color(0xFF1E293B),
                functionText = Color(0xFFC084FC),
                equalsBg = Color(0xFF00E5FF),
                equalsText = Color(0xFF021B2B),
                clearBg = Color(0xFF4C1D24),
                clearText = Color(0xFFF87171),
                displayBg = Color(0xFF0A0E17),
                displayText = Color(0xFFF8FAFC),
                displaySubText = Color(0xFF94A3B8),
                accentGlow = Color(0xFF00E5FF)
            )
            ThemePreset.MIDNIGHT_OLED -> KeypadColors(
                numberBg = Color(0xFF121212),
                numberText = Color(0xFFF3F4F6),
                operatorBg = Color(0xFF064E3B),
                operatorText = Color(0xFF34D399),
                functionBg = Color(0xFF1F2421),
                functionText = Color(0xFFA7F3D0),
                equalsBg = Color(0xFF10B981),
                equalsText = Color(0xFF022C22),
                clearBg = Color(0xFF3F1218),
                clearText = Color(0xFFFB7185),
                displayBg = Color(0xFF000000),
                displayText = Color(0xFFFFFFFF),
                displaySubText = Color(0xFF9CA3AF),
                accentGlow = Color(0xFF10B981)
            )
            ThemePreset.CYBERPUNK -> KeypadColors(
                numberBg = Color(0xFF1C132B),
                numberText = Color(0xFFF1EAFF),
                operatorBg = Color(0xFF431259),
                operatorText = Color(0xFFE879F9),
                functionBg = Color(0xFF2A1B4E),
                functionText = Color(0xFFFCD34D),
                equalsBg = Color(0xFFF43F5E),
                equalsText = Color(0xFFFFFFFF),
                clearBg = Color(0xFF501323),
                clearText = Color(0xFFFDA4AF),
                displayBg = Color(0xFF0E0818),
                displayText = Color(0xFFFAF5FF),
                displaySubText = Color(0xFFD8B4FE),
                accentGlow = Color(0xFFE879F9)
            )
            ThemePreset.NORDIC_SLATE -> KeypadColors(
                numberBg = Color(0xFF1E293B),
                numberText = Color(0xFFF8FAFC),
                operatorBg = Color(0xFF1E3A8A),
                operatorText = Color(0xFF93C5FD),
                functionBg = Color(0xFF334155),
                functionText = Color(0xFF818CF8),
                equalsBg = Color(0xFF38BDF8),
                equalsText = Color(0xFF0C243C),
                clearBg = Color(0xFF451A20),
                clearText = Color(0xFFFCA5A5),
                displayBg = Color(0xFF0F172A),
                displayText = Color(0xFFFFFFFF),
                displaySubText = Color(0xFF94A3B8),
                accentGlow = Color(0xFF38BDF8)
            )
            ThemePreset.TITANIUM_GOLD -> KeypadColors(
                numberBg = Color(0xFF27272A),
                numberText = Color(0xFFF4F4F5),
                operatorBg = Color(0xFF451A03),
                operatorText = Color(0xFFFBBF24),
                functionBg = Color(0xFF3F3F46),
                functionText = Color(0xFFE4E4E7),
                equalsBg = Color(0xFFF59E0B),
                equalsText = Color(0xFF291A00),
                clearBg = Color(0xFF4C1D24),
                clearText = Color(0xFFFCA5A5),
                displayBg = Color(0xFF121214),
                displayText = Color(0xFFFFFFFF),
                displaySubText = Color(0xFFA1A1AA),
                accentGlow = Color(0xFFF59E0B)
            )
        }
    }

    fun getColorScheme(preset: ThemePreset): ColorScheme {
        return when (preset) {
            ThemePreset.PROFESSIONAL_POLISH -> darkColorScheme(
                primary = Color(0xFFD0BCFF),
                onPrimary = Color(0xFF381E72),
                primaryContainer = Color(0xFF4F378B),
                onPrimaryContainer = Color(0xFFEADDFF),
                secondary = Color(0xFFCCC2DC),
                onSecondary = Color(0xFF332D41),
                secondaryContainer = Color(0xFF4A4458),
                onSecondaryContainer = Color(0xFFE8DEF8),
                tertiary = Color(0xFFEFB8C8),
                onTertiary = Color(0xFF492532),
                background = Color(0xFF1C1B1F),
                onBackground = Color(0xFFE6E1E5),
                surface = Color(0xFF1C1B1F),
                onSurface = Color(0xFFE6E1E5),
                surfaceVariant = Color(0xFF49454F),
                onSurfaceVariant = Color(0xFFCAC4D0),
                error = Color(0xFFF2B8B5),
                onError = Color(0xFF601410)
            )
            ThemePreset.OBSIDIAN_NEON -> darkColorScheme(
                primary = Color(0xFF00E5FF),
                onPrimary = Color(0xFF00363D),
                primaryContainer = Color(0xFF0F3956),
                onPrimaryContainer = Color(0xFFBCE9FF),
                secondary = Color(0xFFC084FC),
                onSecondary = Color(0xFF381E72),
                tertiary = Color(0xFF38BDF8),
                background = Color(0xFF090D16),
                onBackground = Color(0xFFE2E8F0),
                surface = Color(0xFF111827),
                onSurface = Color(0xFFF1F5F9),
                surfaceVariant = Color(0xFF1E293B),
                onSurfaceVariant = Color(0xFF94A3B8),
                error = Color(0xFFF87171),
                onError = Color(0xFF490013)
            )
            ThemePreset.MIDNIGHT_OLED -> darkColorScheme(
                primary = Color(0xFF10B981),
                onPrimary = Color(0xFF003822),
                primaryContainer = Color(0xFF064E3B),
                onPrimaryContainer = Color(0xFFA7F3D0),
                secondary = Color(0xFF34D399),
                onSecondary = Color(0xFF003822),
                tertiary = Color(0xFF6EE7B7),
                background = Color(0xFF000000),
                onBackground = Color(0xFFF3F4F6),
                surface = Color(0xFF0A0A0A),
                onSurface = Color(0xFFFFFFFF),
                surfaceVariant = Color(0xFF18181B),
                onSurfaceVariant = Color(0xFF9CA3AF),
                error = Color(0xFFFB7185),
                onError = Color(0xFF4C0519)
            )
            ThemePreset.CYBERPUNK -> darkColorScheme(
                primary = Color(0xFFF43F5E),
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFF431259),
                onPrimaryContainer = Color(0xFFF5D0FE),
                secondary = Color(0xFFE879F9),
                onSecondary = Color(0xFF38004D),
                tertiary = Color(0xFFFCD34D),
                background = Color(0xFF0A0512),
                onBackground = Color(0xFFF5F3FF),
                surface = Color(0xFF130D22),
                onSurface = Color(0xFFFAF5FF),
                surfaceVariant = Color(0xFF23163E),
                onSurfaceVariant = Color(0xFFD8B4FE),
                error = Color(0xFFFDA4AF),
                onError = Color(0xFF4C0519)
            )
            ThemePreset.NORDIC_SLATE -> darkColorScheme(
                primary = Color(0xFF38BDF8),
                onPrimary = Color(0xFF003549),
                primaryContainer = Color(0xFF1E3A8A),
                onPrimaryContainer = Color(0xFFD0E4FF),
                secondary = Color(0xFF818CF8),
                onSecondary = Color(0xFF1E1E66),
                tertiary = Color(0xFF67E8F9),
                background = Color(0xFF0B1120),
                onBackground = Color(0xFFF8FAFC),
                surface = Color(0xFF0F172A),
                onSurface = Color(0xFFFFFFFF),
                surfaceVariant = Color(0xFF1E293B),
                onSurfaceVariant = Color(0xFF94A3B8),
                error = Color(0xFFF87171),
                onError = Color(0xFF490013)
            )
            ThemePreset.TITANIUM_GOLD -> darkColorScheme(
                primary = Color(0xFFF59E0B),
                onPrimary = Color(0xFF451A00),
                primaryContainer = Color(0xFF451A03),
                onPrimaryContainer = Color(0xFFFFDDB8),
                secondary = Color(0xFFFBBF24),
                onSecondary = Color(0xFF451A00),
                tertiary = Color(0xFFE4E4E7),
                background = Color(0xFF0F0F11),
                onBackground = Color(0xFFF4F4F5),
                surface = Color(0xFF18181B),
                onSurface = Color(0xFFFFFFFF),
                surfaceVariant = Color(0xFF27272A),
                onSurfaceVariant = Color(0xFFA1A1AA),
                error = Color(0xFFF87171),
                onError = Color(0xFF490013)
            )
        }
    }
}
