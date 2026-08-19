package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.engine.AlgebraEquationSolution
import com.example.engine.GeminiMathSolver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AlgebraPreset(
    val title: String,
    val equation: String,
    val category: String,
    val description: String
)

data class AlgebraSolverUiState(
    val equationInput: String = "2x + 5 = 15",
    val targetVariable: String = "x",
    val isLoading: Boolean = false,
    val currentSolution: AlgebraEquationSolution? = null,
    val solutionHistory: List<AlgebraEquationSolution> = emptyList(),
    val errorMessage: String? = null
)

class AlgebraSolverViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AlgebraSolverUiState())
    val uiState: StateFlow<AlgebraSolverUiState> = _uiState.asStateFlow()

    val presets = listOf(
        AlgebraPreset("Linear Basic", "3x + 12 = 42", "Linear", "Solve for x in one-step linear form"),
        AlgebraPreset("Linear Both Sides", "5x - 8 = 2x + 16", "Linear", "Variables on both left and right sides"),
        AlgebraPreset("Quadratic Standard", "x² - 5x + 6 = 0", "Quadratic", "Two distinct real roots (x=2, x=3)"),
        AlgebraPreset("Quadratic Formula", "2x² - 4x - 6 = 0", "Quadratic", "Coefficients with a != 1"),
        AlgebraPreset("System of 2 Eq", "2x + y = 7, x - y = 2", "System", "Simultaneous equations solving for x and y"),
        AlgebraPreset("Brackets & Terms", "3(2x - 4) = 18", "Linear", "Distributive property with parentheses"),
        AlgebraPreset("Fraction / Ratio", "x/4 + 7 = 12", "Linear", "Fractional coefficients"),
        AlgebraPreset("Complex Roots", "x² + 4 = 0", "Quadratic", "Roots involving imaginary numbers (±2i)")
    )

    init {
        // Automatically solve the default starter equation
        solveCurrentEquation()
    }

    fun onEquationChanged(newEquation: String) {
        _uiState.update { it.copy(equationInput = newEquation, errorMessage = null) }
    }

    fun onTargetVariableChanged(newVar: String) {
        _uiState.update { it.copy(targetVariable = newVar, errorMessage = null) }
    }

    fun insertSymbol(symbol: String) {
        _uiState.update { state ->
            val updated = state.equationInput + symbol
            state.copy(equationInput = updated, errorMessage = null)
        }
    }

    fun applyPreset(preset: AlgebraPreset) {
        _uiState.update {
            it.copy(
                equationInput = preset.equation,
                errorMessage = null
            )
        }
        solveCurrentEquation(preset.equation)
    }

    fun clearInput() {
        _uiState.update { it.copy(equationInput = "", errorMessage = null) }
    }

    fun solveCurrentEquation(equationToSolve: String? = null) {
        val eq = (equationToSolve ?: _uiState.value.equationInput).trim()
        if (eq.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please enter an algebraic equation (e.g. 2x + 5 = 15)") }
            return
        }

        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null,
                equationInput = eq
            )
        }

        viewModelScope.launch {
            val result = GeminiMathSolver.solveAlgebraicEquation(
                equation = eq,
                variableHint = _uiState.value.targetVariable.ifBlank { null }
            )

            result.fold(
                onSuccess = { solution ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            currentSolution = solution,
                            solutionHistory = (listOf(solution) + state.solutionHistory.filter { it.originalEquation != solution.originalEquation }).take(20),
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "Failed to solve equation."
                        )
                    }
                }
            )
        }
    }

    fun selectHistorySolution(solution: AlgebraEquationSolution) {
        _uiState.update {
            it.copy(
                equationInput = solution.originalEquation,
                currentSolution = solution,
                errorMessage = null
            )
        }
    }
}
