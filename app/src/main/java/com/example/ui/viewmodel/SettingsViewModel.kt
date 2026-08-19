package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.engine.AngleMode
import com.example.ui.theme.ThemePreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SettingsUiState(
    val currentTheme: ThemePreset = ThemePreset.PROFESSIONAL_POLISH,
    val decimalPrecision: Int = 8,
    val defaultAngleMode: AngleMode = AngleMode.DEG,
    val hapticFeedbackEnabled: Boolean = true,
    val autoClearOnNewInput: Boolean = true
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
}
