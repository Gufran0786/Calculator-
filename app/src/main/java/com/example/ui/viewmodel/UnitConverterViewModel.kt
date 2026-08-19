package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.engine.ConversionUnit
import com.example.engine.UnitCategory
import com.example.engine.UnitConverter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class UnitConverterUiState(
    val selectedCategory: UnitCategory = UnitConverter.CATEGORIES[0],
    val fromUnit: ConversionUnit = UnitConverter.CATEGORIES[0].units[0],
    val toUnit: ConversionUnit = UnitConverter.CATEGORIES[0].units[1],
    val inputValue: String = "1",
    val convertedResult: String = "",
    val formulaExplanation: String = ""
)

class UnitConverterViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(UnitConverterUiState())
    val uiState: StateFlow<UnitConverterUiState> = _uiState.asStateFlow()

    init {
        recalculate()
    }

    fun selectCategory(category: UnitCategory) {
        _uiState.update {
            it.copy(
                selectedCategory = category,
                fromUnit = category.units[0],
                toUnit = if (category.units.size > 1) category.units[1] else category.units[0]
            )
        }
        recalculate()
    }

    fun setFromUnit(unit: ConversionUnit) {
        _uiState.update { it.copy(fromUnit = unit) }
        recalculate()
    }

    fun setToUnit(unit: ConversionUnit) {
        _uiState.update { it.copy(toUnit = unit) }
        recalculate()
    }

    fun onInputValueChange(newInput: String) {
        // Allow valid numeric input with one decimal point
        val sanitized = newInput.filter { it.isDigit() || it == '.' || it == '-' }
        if (sanitized.count { it == '.' } > 1 || sanitized.count { it == '-' } > 1) return

        _uiState.update { it.copy(inputValue = sanitized) }
        recalculate()
    }

    fun onKeyPadInput(key: String) {
        _uiState.update { state ->
            val curr = state.inputValue
            val nextVal = when (key) {
                "C" -> "0"
                "⌫" -> if (curr.length > 1) curr.dropLast(1) else "0"
                "." -> if (!curr.contains('.')) "$curr." else curr
                "±" -> if (curr.startsWith('-')) curr.drop(1) else if (curr != "0") "-$curr" else curr
                else -> if (curr == "0") key else "$curr$key"
            }
            state.copy(inputValue = nextVal)
        }
        recalculate()
    }

    fun swapUnits() {
        _uiState.update {
            val prevFrom = it.fromUnit
            val prevTo = it.toUnit
            it.copy(fromUnit = prevTo, toUnit = prevFrom)
        }
        recalculate()
    }

    private fun recalculate() {
        val state = _uiState.value
        val num = state.inputValue.toDoubleOrNull() ?: 0.0
        val converted = UnitConverter.convert(num, state.fromUnit, state.toUnit)
        val formatted = UnitConverter.formatConvertedValue(converted)

        val explanation = if (state.fromUnit.id == state.toUnit.id) {
            "Same units (1 : 1)"
        } else if (state.fromUnit.customConvert != null) {
            "Formula: ${state.fromUnit.name} to ${state.toUnit.name}"
        } else {
            val ratio = state.fromUnit.toBaseFactor / state.toUnit.toBaseFactor
            "1 ${state.fromUnit.symbol} = ${UnitConverter.formatConvertedValue(ratio)} ${state.toUnit.symbol}"
        }

        _uiState.update {
            it.copy(
                convertedResult = formatted,
                formulaExplanation = explanation
            )
        }
    }
}
