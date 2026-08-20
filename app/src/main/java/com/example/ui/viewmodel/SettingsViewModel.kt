package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.engine.AngleMode
import com.example.ui.theme.ThemePreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class AppLanguage(
    val code: String,
    val nativeName: String,
    val englishName: String,
    val flagEmoji: String
) {
    ENGLISH("en", "English", "English", "🇺🇸"),
    HINDI("hi", "हिंदी", "Hindi", "🇮🇳"),
    HINGLISH("hinglish", "Hinglish", "Hinglish (Hindi+English)", "🇮🇳"),
    SPANISH("es", "Español", "Spanish", "🇪🇸"),
    FRENCH("fr", "Français", "French", "🇫🇷"),
    GERMAN("de", "Deutsch", "German", "🇩🇪"),
    ARABIC("ar", "العربية", "Arabic", "🇸🇦"),
    BENGALI("bn", "বাংলা", "Bengali", "🇮🇳"),
    MARATHI("mr", "मराठी", "Marathi", "🇮🇳"),
    GUJARATI("gu", "ગુજરાતી", "Gujarati", "🇮🇳")
}

data class SettingsUiState(
    val currentTheme: ThemePreset = ThemePreset.PROFESSIONAL_POLISH,
    val decimalPrecision: Int = 8,
    val defaultAngleMode: AngleMode = AngleMode.DEG,
    val hapticFeedbackEnabled: Boolean = true,
    val autoClearOnNewInput: Boolean = true,
    val selectedLanguage: AppLanguage = AppLanguage.ENGLISH
)

class SettingsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun setTheme(theme: ThemePreset) {
        _uiState.update { it.copy(currentTheme = theme) }
    }

    fun setPrecision(precision: Int) {
        _uiState.update { it.copy(decimalPrecision = precision.coerceIn(2, 12)) }
    }

    fun setDefaultAngleMode(mode: AngleMode) {
        _uiState.update { it.copy(defaultAngleMode = mode) }
    }

    fun toggleHapticFeedback() {
        _uiState.update { it.copy(hapticFeedbackEnabled = !it.hapticFeedbackEnabled) }
    }

    fun setLanguage(language: AppLanguage) {
        _uiState.update { it.copy(selectedLanguage = language) }
    }
}
