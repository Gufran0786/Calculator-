package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.HistoryRepository
import com.example.engine.AngleMode
import com.example.engine.MathEvaluator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CalculatorUiState(
    val expression: String = "",
    val livePreview: String = "",
    val angleMode: AngleMode = AngleMode.DEG,
    val isScientificExpanded: Boolean = true,
    val isSecondFunction: Boolean = false,
    val errorMessage: String? = null,
    val lastResult: String? = null,
    val precision: Int = 8
)

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HistoryRepository
    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = HistoryRepository(db.calculationDao())
    }

    fun onKeyPressed(key: String) {
        _uiState.update { state ->
            var newExpr = state.expression

            // If expression was empty and starting with operator (except minus or function), keep it or prefix 0
            if (newExpr.isEmpty() && key in listOf("×", "÷", "+", "%", "^")) {
                return@update state
            }

            // Handle Smart Parentheses
            if (key == "()") {
                val openCount = newExpr.count { it == '(' }
                val closeCount = newExpr.count { it == ')' }
                val lastChar = newExpr.lastOrNull()

                newExpr = if (openCount > closeCount && (lastChar?.isDigit() == true || lastChar == ')' || lastChar == 'π' || lastChar == 'e')) {
                    "$newExpr)"
                } else {
                    if (lastChar?.isDigit() == true || lastChar == ')' || lastChar == 'π' || lastChar == 'e') {
                        "$newExpr×("
                    } else {
                        "$newExpr("
                    }
                }
            } else {
                newExpr += key
            }

            // Compute live preview
            val preview = calculateLivePreview(newExpr, state.angleMode, state.precision)
            state.copy(
                expression = newExpr,
                livePreview = preview,
                errorMessage = null
            )
        }
    }

    fun onBackspace() {
        _uiState.update { state ->
            if (state.expression.isEmpty()) return@update state

            var expr = state.expression
            // Check for multi-character function deletions like 'sin(', 'sqrt(', 'asin(', etc.
            val functionSuffixes = listOf("asin(", "acos(", "atan(", "sinh(", "cosh(", "tanh(", "sqrt(", "cbrt(", "log10(", "log2(", "abs(", "sin(", "cos(", "tan(", "log(", "ln(", "mod ")
            var matched = false
            for (suffix in functionSuffixes) {
                if (expr.endsWith(suffix)) {
                    expr = expr.dropLast(suffix.length)
                    matched = true
                    break
                }
            }

            if (!matched) {
                expr = expr.dropLast(1)
            }

            val preview = calculateLivePreview(expr, state.angleMode, state.precision)
            state.copy(
                expression = expr,
                livePreview = preview,
                errorMessage = null
            )
        }
    }

    fun onClear() {
        _uiState.update { it.copy(expression = "", livePreview = "", errorMessage = null) }
    }

    fun onAllClear() {
        _uiState.update {
            it.copy(
                expression = "",
                livePreview = "",
                lastResult = null,
                errorMessage = null
            )
        }
    }

    fun onCalculate() {
        val state = _uiState.value
        val expr = state.expression.trim()
        if (expr.isEmpty()) return

        val evalResult = MathEvaluator.evaluate(expr, state.angleMode)
        evalResult.fold(
            onSuccess = { res ->
                val formatted = MathEvaluator.formatResult(res, state.precision)
                _uiState.update {
                    it.copy(
                        expression = formatted,
                        livePreview = "",
                        lastResult = formatted,
                        errorMessage = null
                    )
                }

                // Save to Room History
                viewModelScope.launch {
                    repository.insert(
                        expression = expr,
                        result = formatted,
                        category = if (state.isScientificExpanded) "Scientific" else "Basic"
                    )
                }
            },
            onFailure = { error ->
                _uiState.update {
                    it.copy(
                        errorMessage = error.localizedMessage ?: "Math Error",
                        livePreview = ""
                    )
                }
            }
        )
    }

    fun insertExpression(customExpr: String) {
        _uiState.update { state ->
            val newExpr = if (state.expression.isEmpty() || state.expression == "0") {
                customExpr
            } else {
                state.expression + customExpr
            }
            val preview = calculateLivePreview(newExpr, state.angleMode, state.precision)
            state.copy(
                expression = newExpr,
                livePreview = preview,
                errorMessage = null
            )
        }
    }

    fun setAngleMode(mode: AngleMode) {
        _uiState.update { state ->
            if (state.angleMode == mode) return@update state
            val preview = calculateLivePreview(state.expression, mode, state.precision)
            state.copy(angleMode = mode, livePreview = preview)
        }
    }

    fun toggleAngleMode() {
        _uiState.update { state ->
            val nextMode = if (state.angleMode == AngleMode.DEG) AngleMode.RAD else AngleMode.DEG
            val preview = calculateLivePreview(state.expression, nextMode, state.precision)
            state.copy(angleMode = nextMode, livePreview = preview)
        }
    }

    fun toggleScientificPanel() {
        _uiState.update { it.copy(isScientificExpanded = !it.isScientificExpanded) }
    }

    fun toggleSecondFunction() {
        _uiState.update { it.copy(isSecondFunction = !it.isSecondFunction) }
    }

    fun setPrecision(precision: Int) {
        _uiState.update { it.copy(precision = precision) }
    }

    private fun calculateLivePreview(expr: String, angleMode: AngleMode, precision: Int): String {
        if (expr.isBlank()) return ""
        val res = MathEvaluator.evaluate(expr, angleMode)
        return res.fold(
            onSuccess = { MathEvaluator.formatResult(it, precision) },
            onFailure = { "" }
        )
    }
}
