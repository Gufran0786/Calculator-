package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.engine.AiMathSolution
import com.example.engine.GeminiMathSolver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FloatingAiUiState(
    val isWindowOpen: Boolean = false,
    val isBubbleVisible: Boolean = true,
    val bubbleOffsetX: Float = 0f,
    val bubbleOffsetY: Float = 0f,
    val currentQuery: String = "",
    val isLoading: Boolean = false,
    val currentSolution: AiMathSolution? = null,
    val solutionHistory: List<AiMathSolution> = emptyList(),
    val errorMessage: String? = null
)

class FloatingAiViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FloatingAiUiState())
    val uiState: StateFlow<FloatingAiUiState> = _uiState.asStateFlow()

    fun openFloatingWindow() {
        _uiState.update { it.copy(isWindowOpen = true) }
    }

    fun closeFloatingWindow() {
        _uiState.update { it.copy(isWindowOpen = false, errorMessage = null) }
    }

    fun toggleFloatingWindow() {
        _uiState.update { it.copy(isWindowOpen = !it.isWindowOpen) }
    }

    fun setBubbleOffset(x: Float, y: Float) {
        _uiState.update { it.copy(bubbleOffsetX = x, bubbleOffsetY = y) }
    }

    fun setBubbleVisible(visible: Boolean) {
        _uiState.update { it.copy(isBubbleVisible = visible) }
    }

    fun resetBubblePosition() {
        _uiState.update { it.copy(bubbleOffsetX = 0f, bubbleOffsetY = 0f, isBubbleVisible = true) }
    }

    fun onQueryChanged(query: String) {
        _uiState.update { it.copy(currentQuery = query, errorMessage = null) }
    }

    fun solveQuestion(query: String? = null) {
        val q = (query ?: _uiState.value.currentQuery).trim()
        if (q.isEmpty()) return

        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null,
                currentQuery = q,
                isWindowOpen = true
            )
        }

        viewModelScope.launch {
            val result = GeminiMathSolver.solveProblem(q)
            result.fold(
                onSuccess = { solution ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            currentSolution = solution,
                            solutionHistory = listOf(solution) + state.solutionHistory.take(15),
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "Failed to solve problem."
                        )
                    }
                }
            )
        }
    }

    fun clearCurrentSolution() {
        _uiState.update { it.copy(currentSolution = null, currentQuery = "", errorMessage = null) }
    }

    fun selectHistorySolution(solution: AiMathSolution) {
        _uiState.update {
            it.copy(
                currentSolution = solution,
                currentQuery = solution.question,
                isWindowOpen = true
            )
        }
    }
}
