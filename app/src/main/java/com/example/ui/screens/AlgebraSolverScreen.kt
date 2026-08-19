package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.AlgebraEquationSolution
import com.example.ui.components.VoiceInputDialog
import com.example.ui.viewmodel.AlgebraSolverViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlgebraSolverScreen(
    viewModel: AlgebraSolverViewModel,
    onInsertToCalculator: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var showVoiceDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }

    val quickAlgebraSymbols = listOf(
        "x", "y", "z", "a", "b", "x²", "x³", "=", "+", "−", "×", "/", "^", "√(", "(", ")", "0", "1", "2", "3"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Functions,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "Algebra Equation Solver",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Text(
                            text = "Solve for Variables with Gemini AI & Offline Engine",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                actions = {
                    if (uiState.solutionHistory.isNotEmpty()) {
                        IconButton(
                            onClick = { showHistoryDialog = true },
                            modifier = Modifier.testTag("algebra_history_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Equation History",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Main Input Card with Input Field & Solve Button
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Enter Algebraic Equation:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Input Field with Clear, Voice, and Paste Action
                        OutlinedTextField(
                            value = uiState.equationInput,
                            onValueChange = { viewModel.onEquationChanged(it) },
                            placeholder = {
                                Text(
                                    text = "e.g. 2x + 5 = 15 or x² - 5x + 6 = 0",
                                    fontSize = 14.sp
                                )
                            },
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace
                            ),
                            trailingIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (uiState.equationInput.isNotEmpty()) {
                                        IconButton(
                                            onClick = { viewModel.clearInput() },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Clear Equation",
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    IconButton(
                                        onClick = { showVoiceDialog = true },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .testTag("algebra_voice_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Mic,
                                            contentDescription = "Voice Equation",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                keyboardController?.hide()
                                viewModel.solveCurrentEquation()
                            }),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = false,
                            maxLines = 3,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("algebra_equation_input_field")
                        )

                        // Quick Algebra Symbol Insertion Ribbon
                        val symbolScrollState = rememberScrollState()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(symbolScrollState),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            quickAlgebraSymbols.forEach { sym ->
                                Surface(
                                    onClick = { viewModel.insertSymbol(sym) },
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                    modifier = Modifier.testTag("algebra_sym_$sym")
                                ) {
                                    Text(
                                        text = sym,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        // Solve Action Row with Solve Button & Target Variable Hint
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Target variable selector
                            OutlinedTextField(
                                value = uiState.targetVariable,
                                onValueChange = { viewModel.onTargetVariableChanged(it) },
                                label = { Text("Solve for", fontSize = 11.sp) },
                                singleLine = true,
                                textStyle = LocalTextStyle.current.copy(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .width(95.dp)
                                    .testTag("algebra_target_variable_field")
                            )

                            // Prominent Solve Button
                            Button(
                                onClick = {
                                    keyboardController?.hide()
                                    viewModel.solveCurrentEquation()
                                },
                                enabled = uiState.equationInput.isNotBlank() && !uiState.isLoading,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .testTag("solve_algebra_button")
                            ) {
                                if (uiState.isLoading) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Solving...", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Solve Equation",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Error banner if any
            if (uiState.errorMessage != null) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = uiState.errorMessage ?: "",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Presets / Example Equation Templates
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Example Algebraic Equations:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(viewModel.presets) { preset ->
                            FilterChip(
                                selected = uiState.equationInput == preset.equation,
                                onClick = { viewModel.applyPreset(preset) },
                                label = {
                                    Column(modifier = Modifier.padding(vertical = 2.dp)) {
                                        Text(
                                            text = preset.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = preset.equation,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Functions,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("preset_${preset.title}")
                            )
                        }
                    }
                }
            }

            // Solved Result Section (Variable Values & Step-by-Step AI Explanation)
            if (uiState.currentSolution != null) {
                val solution = uiState.currentSolution!!

                // Computed Variables Card
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("algebra_result_card")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "COMPUTED VARIABLE VALUES",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp,
                                        letterSpacing = 0.8.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                if (!solution.formulaOrMethod.isNullOrBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = solution.formulaOrMethod,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }

                            // Variable values chips / badges
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                solution.variableValues.forEach { varVal ->
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        shadowElevation = 2.dp,
                                        modifier = Modifier
                                            .weight(1f, fill = false)
                                            .testTag("var_value_${varVal.variableName}")
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = varVal.variableName,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = varVal.value,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontFamily = FontFamily.Monospace,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }

                            // Action buttons: Copy & Insert into Calculator
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val primaryVal = solution.variableValues.firstOrNull()?.value ?: ""
                                Button(
                                    onClick = {
                                        onInsertToCalculator(primaryVal)
                                        Toast.makeText(context, "Inserted '$primaryVal' into Calculator", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("insert_variable_to_calc_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Input,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Use in Calculator", fontSize = 13.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        val copyText = solution.variableValues.joinToString(", ") { "${it.variableName} = ${it.value}" }
                                        clipboardManager.setText(AnnotatedString(copyText))
                                        Toast.makeText(context, "Copied: $copyText", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("copy_variable_value_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy Value",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy", fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }

                // Step-by-Step AI Derivation Card
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("algebra_explanation_card")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Step-by-Step Derivation & Verification:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Text(
                                text = solution.stepByStepExplanation,
                                fontSize = 13.5.sp,
                                lineHeight = 20.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Voice Input Speech Recognizer Dialog
    if (showVoiceDialog) {
        VoiceInputDialog(
            onDismiss = { showVoiceDialog = false },
            onMathResult = { voiceResult ->
                viewModel.onEquationChanged(voiceResult)
                viewModel.solveCurrentEquation(voiceResult)
            }
        )
    }

    // Solved Equations History Bottom Sheet
    if (showHistoryDialog) {
        ModalBottomSheet(
            onDismissRequest = { showHistoryDialog = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Algebraic Solution History",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(uiState.solutionHistory) { item ->
                        Card(
                            onClick = {
                                viewModel.selectHistorySolution(item)
                                showHistoryDialog = false
                            },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = item.originalEquation,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                val solStr = item.variableValues.joinToString(", ") { "${it.variableName} = ${it.value}" }
                                Text(
                                    text = "Solution: $solStr",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
