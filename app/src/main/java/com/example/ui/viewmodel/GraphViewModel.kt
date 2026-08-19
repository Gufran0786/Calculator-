package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.engine.AngleMode
import com.example.engine.MathEvaluator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class GraphPoint(val x: Double, val y: Double)

data class PresetFunction(
    val title: String,
    val formula: String,
    val category: String
)

data class GraphUiState(
    val formula: String = "sin(x)",
    val inputFormula: String = "sin(x)",
    val angleMode: AngleMode = AngleMode.RAD,
    val xMin: Double = -10.0,
    val xMax: Double = 10.0,
    val yMin: Double = -6.0,
    val yMax: Double = 6.0,
    val zoomLevel: Float = 1.0f,
    val panOffsetX: Float = 0f,
    val panOffsetY: Float = 0f,
    val selectedPoint: GraphPoint? = null,
    val isEvaluating: Boolean = false,
    val errorMessage: String? = null
)

class GraphViewModel : ViewModel() {

    val PRESET_FUNCTIONS = listOf(
        PresetFunction("Sine Wave", "sin(x)", "Trigonometry"),
        PresetFunction("Damped Oscillation", "cos(x) * exp(-0.15 * x)", "Physics"),
        PresetFunction("Parabola", "x^2 - 4", "Polynomial"),
        PresetFunction("Cubic Curve", "x^3 - 3*x", "Polynomial"),
        PresetFunction("Hyperbola (Rational)", "1 / x", "Rational"),
        PresetFunction("Gaussian Bell Curve", "exp(-0.5 * x^2)", "Statistics"),
        PresetFunction("Tangent", "tan(x)", "Trigonometry"),
        PresetFunction("Absolute V-Shape", "abs(x) - 2", "Basic"),
        PresetFunction("Square Root", "sqrt(x)", "Radical"),
        PresetFunction("Exponential Growth", "2^x - 3", "Exponential")
    )

    private val _uiState = MutableStateFlow(GraphUiState())
    val uiState: StateFlow<GraphUiState> = _uiState.asStateFlow()

    fun setInputFormula(input: String) {
        _uiState.update { it.copy(inputFormula = input, errorMessage = null) }
    }

    fun applyFormula() {
        val input = _uiState.value.inputFormula.trim()
        if (input.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter a function formula") }
            return
        }

        // Validate on sample points
        val testY = MathEvaluator.evaluateForX(input, 1.0, _uiState.value.angleMode)
        if (testY.isNaN() && MathEvaluator.evaluateForX(input, 0.5, _uiState.value.angleMode).isNaN()) {
            _uiState.update { it.copy(errorMessage = "Invalid function syntax. Use 'x' as variable (e.g. sin(x), x^2)") }
            return
        }

        _uiState.update {
            it.copy(
                formula = input,
                errorMessage = null,
                selectedPoint = null
            )
        }
    }

    fun selectPreset(preset: PresetFunction) {
        _uiState.update {
            it.copy(
                formula = preset.formula,
                inputFormula = preset.formula,
                errorMessage = null,
                selectedPoint = null
            )
        }
    }

    fun zoomIn() {
        _uiState.update { state ->
            val factor = 0.75
            val xCenter = (state.xMin + state.xMax) / 2
            val yCenter = (state.yMin + state.yMax) / 2
            val xSpan = (state.xMax - state.xMin) * factor
            val ySpan = (state.yMax - state.yMin) * factor

            state.copy(
                xMin = xCenter - xSpan / 2,
                xMax = xCenter + xSpan / 2,
                yMin = yCenter - ySpan / 2,
                yMax = yCenter + ySpan / 2
            )
        }
    }

    fun zoomOut() {
        _uiState.update { state ->
            val factor = 1.333
            val xCenter = (state.xMin + state.xMax) / 2
            val yCenter = (state.yMin + state.yMax) / 2
            val xSpan = (state.xMax - state.xMin) * factor
            val ySpan = (state.yMax - state.yMin) * factor

            state.copy(
                xMin = xCenter - xSpan / 2,
                xMax = xCenter + xSpan / 2,
                yMin = yCenter - ySpan / 2,
                yMax = yCenter + ySpan / 2
            )
        }
    }

    fun resetView() {
        _uiState.update {
            it.copy(
                xMin = -10.0,
                xMax = 10.0,
                yMin = -6.0,
                yMax = 6.0,
                panOffsetX = 0f,
                panOffsetY = 0f,
                selectedPoint = null
            )
        }
    }

    fun onPan(deltaXFraction: Float, deltaYFraction: Float) {
        _uiState.update { state ->
            val xSpan = state.xMax - state.xMin
            val ySpan = state.yMax - state.yMin

            val dx = deltaXFraction * xSpan
            val dy = deltaYFraction * ySpan

            state.copy(
                xMin = state.xMin - dx,
                xMax = state.xMax - dx,
                yMin = state.yMin + dy,
                yMax = state.yMax + dy
            )
        }
    }

    fun onInspectPoint(xVal: Double) {
        val state = _uiState.value
        val yVal = MathEvaluator.evaluateForX(state.formula, xVal, state.angleMode)
        _uiState.update {
            it.copy(
                selectedPoint = if (!yVal.isNaN() && !yVal.isInfinite()) GraphPoint(xVal, yVal) else null
            )
        }
    }

    fun toggleAngleMode() {
        _uiState.update {
            it.copy(angleMode = if (it.angleMode == AngleMode.RAD) AngleMode.DEG else AngleMode.RAD)
        }
    }
}
