package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.components.FloatingAiBubble
import com.example.ui.components.FloatingAiWindow
import com.example.ui.components.VoiceInputDialog
import com.example.ui.screens.*
import com.example.ui.theme.ScientificCalculatorTheme
import com.example.ui.viewmodel.*

enum class AppNavDestination(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    CALCULATOR("calculator", "Calculator", Icons.Default.Calculate),
    ALGEBRA("algebra", "Algebra", Icons.Default.Functions),
    GRAPH("graph", "Graph", Icons.Default.ShowChart),
    CONVERTER("converter", "Converter", Icons.Default.Transform),
    HISTORY("history", "History", Icons.Default.History),
    SETTINGS("settings", "Settings", Icons.Default.Tune)
}

class MainActivity : ComponentActivity() {

    private val calculatorViewModel: CalculatorViewModel by viewModels()
    private val algebraSolverViewModel: AlgebraSolverViewModel by viewModels()
    private val graphViewModel: GraphViewModel by viewModels()
    private val unitConverterViewModel: UnitConverterViewModel by viewModels()
    private val historyViewModel: HistoryViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val floatingAiViewModel: FloatingAiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settingsState by settingsViewModel.uiState.collectAsState()
            val aiUiState by floatingAiViewModel.uiState.collectAsState()
            val calcUiState by calculatorViewModel.uiState.collectAsState()

            var showAiVoiceDialog by remember { mutableStateOf(false) }

            // Update calculator precision & angle mode when settings change
            LaunchedEffect(settingsState.decimalPrecision) {
                calculatorViewModel.setPrecision(settingsState.decimalPrecision)
            }
            LaunchedEffect(settingsState.defaultAngleMode) {
                calculatorViewModel.setAngleMode(settingsState.defaultAngleMode)
            }

            ScientificCalculatorTheme(preset = settingsState.currentTheme) {
                var currentScreen by remember { mutableStateOf(AppNavDestination.CALCULATOR) }

                Box(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        bottomBar = {
                            NavigationBar(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .windowInsetsPadding(WindowInsets.navigationBars)
                                    .testTag("main_bottom_nav"),
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 6.dp
                            ) {
                                AppNavDestination.values().forEach { destination ->
                                    val selected = currentScreen == destination
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = { currentScreen = destination },
                                        icon = {
                                            Icon(
                                                imageVector = destination.icon,
                                                contentDescription = destination.title
                                            )
                                        },
                                        label = {
                                            Text(text = destination.title)
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.testTag("nav_tab_${destination.route}")
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        Crossfade(
                            targetState = currentScreen,
                            label = "screen_crossfade",
                            modifier = Modifier.padding(innerPadding)
                        ) { screen ->
                            when (screen) {
                                AppNavDestination.CALCULATOR -> CalculatorScreen(
                                    viewModel = calculatorViewModel,
                                    onNavigateToAlgebra = { currentScreen = AppNavDestination.ALGEBRA },
                                    onNavigateToGraph = { currentScreen = AppNavDestination.GRAPH },
                                    onNavigateToHistory = { currentScreen = AppNavDestination.HISTORY },
                                    onOpenAiSolver = { floatingAiViewModel.openFloatingWindow() }
                                )
                                AppNavDestination.ALGEBRA -> AlgebraSolverScreen(
                                    viewModel = algebraSolverViewModel,
                                    onInsertToCalculator = { valExpr ->
                                        calculatorViewModel.insertExpression(valExpr)
                                        currentScreen = AppNavDestination.CALCULATOR
                                    }
                                )
                                AppNavDestination.GRAPH -> GraphScreen(
                                    viewModel = graphViewModel
                                )
                                AppNavDestination.CONVERTER -> UnitConverterScreen(
                                    viewModel = unitConverterViewModel
                                )
                                AppNavDestination.HISTORY -> HistoryScreen(
                                    viewModel = historyViewModel,
                                    onUseExpression = { expr ->
                                        calculatorViewModel.insertExpression(expr)
                                        currentScreen = AppNavDestination.CALCULATOR
                                    }
                                )
                                AppNavDestination.SETTINGS -> SettingsScreen(
                                    viewModel = settingsViewModel
                                )
                            }
                        }
                    }

                    // Draggable Floating Rounded AI Bubble (accessible and placable anywhere on screen)
                    if (aiUiState.isBubbleVisible) {
                        FloatingAiBubble(
                            offsetX = aiUiState.bubbleOffsetX,
                            offsetY = aiUiState.bubbleOffsetY,
                            onOffsetChange = { x, y ->
                                floatingAiViewModel.setBubbleOffset(x, y)
                            },
                            onClick = {
                                floatingAiViewModel.openFloatingWindow()
                            }
                        )
                    }

                    // Interactive Floating Window Dialog for AI Math & Science Solver
                    if (aiUiState.isWindowOpen) {
                        FloatingAiWindow(
                            uiState = aiUiState,
                            calculatorCurrentExpression = calcUiState.expression,
                            onDismiss = { floatingAiViewModel.closeFloatingWindow() },
                            onQueryChanged = { floatingAiViewModel.onQueryChanged(it) },
                            onSolve = { query -> floatingAiViewModel.solveQuestion(query) },
                            onClearSolution = { floatingAiViewModel.clearCurrentSolution() },
                            onSelectHistory = { floatingAiViewModel.selectHistorySolution(it) },
                            onInsertToCalculator = { solutionAnswer ->
                                calculatorViewModel.insertExpression(solutionAnswer)
                                currentScreen = AppNavDestination.CALCULATOR
                            },
                            onTriggerVoice = { showAiVoiceDialog = true }
                        )
                    }

                    // Voice Input for AI Window
                    if (showAiVoiceDialog) {
                        VoiceInputDialog(
                            onDismiss = { showAiVoiceDialog = false },
                            onMathResult = { voiceText ->
                                floatingAiViewModel.onQueryChanged(voiceText)
                                floatingAiViewModel.solveQuestion(voiceText)
                            }
                        )
                    }
                }
            }
        }
    }
}

