package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CalculatorDisplay
import com.example.ui.components.ConstantsSheet
import com.example.ui.components.ScientificKeypad
import com.example.ui.components.VoiceInputDialog
import com.example.ui.viewmodel.CalculatorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    onNavigateToAlgebra: (() -> Unit)? = null,
    onNavigateToGraph: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onOpenAiSolver: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var showConstantsSheet by remember { mutableStateOf(false) }
    var showVoiceDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Scientific Calculator",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Gufran Khan",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                actions = {
                    if (onNavigateToAlgebra != null) {
                        IconButton(
                            onClick = onNavigateToAlgebra,
                            modifier = Modifier.testTag("nav_to_algebra_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Functions,
                                contentDescription = "Algebra Solver",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (onOpenAiSolver != null) {
                        IconButton(
                            onClick = onOpenAiSolver,
                            modifier = Modifier.testTag("nav_to_ai_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Solver",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    IconButton(
                        onClick = onNavigateToGraph,
                        modifier = Modifier.testTag("nav_to_graph_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = "Graph Plotter",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = onNavigateToHistory,
                        modifier = Modifier.testTag("nav_to_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Calculation History",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Display Area
            CalculatorDisplay(
                expression = uiState.expression,
                livePreview = uiState.livePreview,
                angleMode = uiState.angleMode,
                isSecondFunction = uiState.isSecondFunction,
                onAngleModeToggle = { viewModel.toggleAngleMode() },
                onAngleModeSelected = { viewModel.setAngleMode(it) },
                onVoiceClick = { showVoiceDialog = true },
                onConstantsClick = { showConstantsSheet = true },
                onBackspaceClick = { viewModel.onBackspace() },
                onCopyClick = {
                    val toCopy = uiState.lastResult ?: uiState.expression
                    if (toCopy.isNotBlank()) {
                        clipboardManager.setText(AnnotatedString(toCopy))
                        Toast.makeText(context, "Copied: $toCopy", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.weight(1f)
            )

            // Scientific and Number Keypad Area
            ScientificKeypad(
                isScientificExpanded = uiState.isScientificExpanded,
                isSecondFunction = uiState.isSecondFunction,
                onToggleScientific = { viewModel.toggleScientificPanel() },
                onToggleSecondFunction = { viewModel.toggleSecondFunction() },
                onKeyPressed = { key -> viewModel.onKeyPressed(key) },
                onClear = { viewModel.onClear() },
                onAllClear = { viewModel.onAllClear() },
                onCalculate = { viewModel.onCalculate() },
                angleMode = uiState.angleMode,
                onAngleModeToggle = { viewModel.toggleAngleMode() },
                onAngleModeSelected = { viewModel.setAngleMode(it) },
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
    }

    // Constants & Symbols Bottom Sheet
    if (showConstantsSheet) {
        ConstantsSheet(
            onDismiss = { showConstantsSheet = false },
            onInsertConstant = { constVal ->
                viewModel.insertExpression(constVal)
            }
        )
    }

    // Voice Input Speech Recognizer Dialog
    if (showVoiceDialog) {
        VoiceInputDialog(
            onDismiss = { showVoiceDialog = false },
            onMathResult = { voiceMathExpr ->
                viewModel.insertExpression(voiceMathExpr)
            }
        )
    }
}
