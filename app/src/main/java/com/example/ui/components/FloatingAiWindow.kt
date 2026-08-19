package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.engine.AiMathSolution
import com.example.ui.viewmodel.FloatingAiUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloatingAiWindow(
    uiState: FloatingAiUiState,
    calculatorCurrentExpression: String,
    onDismiss: () -> Unit,
    onQueryChanged: (String) -> Unit,
    onSolve: (String?) -> Unit,
    onClearSolution: () -> Unit,
    onSelectHistory: (AiMathSolution) -> Unit,
    onInsertToCalculator: (String) -> Unit,
    onTriggerVoice: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var showHistorySheet by remember { mutableStateOf(false) }

    val quickQuestions = listOf(
        "Solve 3x + 12 = 30",
        "Roots of x² - 5x + 6 = 0",
        "Derivative of x³ + 4x² - 7",
        "Integrate 2x + cos(x)",
        "Area of circle with radius 7",
        "Convert 100°C to Fahrenheit",
        "Volume of sphere radius 3",
        "Sin(30) + Cos(60) explanation"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .shadow(24.dp, RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF8B5CF6).copy(alpha = 0.8f),
                            Color(0xFF38BDF8).copy(alpha = 0.8f)
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .testTag("floating_ai_window_dialog"),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top Header with Drag Handle, Title, and Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI Solver",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "AI Problem Solver",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Gufran Khan Edition • Step-by-Step AI",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (uiState.solutionHistory.isNotEmpty()) {
                            IconButton(
                                onClick = { showHistorySheet = !showHistorySheet },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("ai_history_toggle_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = "AI History",
                                    tint = if (showHistorySheet) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("close_ai_window_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Floating Window",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Divider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                // Quick Import Expression from Calculator Display (if available)
                if (calculatorCurrentExpression.isNotBlank() && calculatorCurrentExpression != "0") {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clickable {
                                onQueryChanged("Solve and explain step-by-step: $calculatorCurrentExpression")
                                onSolve("Solve and explain step-by-step: $calculatorCurrentExpression")
                            }
                            .testTag("import_calc_expression_chip")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Send Calculator Expr: $calculatorCurrentExpression",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    maxLines = 1
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Quick Question Suggestions Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    items(quickQuestions) { sampleQ ->
                        SuggestionChip(
                            onClick = {
                                onQueryChanged(sampleQ)
                                onSolve(sampleQ)
                            },
                            label = {
                                Text(
                                    text = sampleQ,
                                    fontSize = 12.sp
                                )
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        )
                    }
                }

                // Main Content Area (Solution View, History, or Empty state)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when {
                        uiState.isLoading -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "AI is computing step-by-step solution...",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Analyzing mathematical formulas & principles",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        showHistorySheet && uiState.solutionHistory.isNotEmpty() -> {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Solved Questions History",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    TextButton(onClick = { showHistorySheet = false }) {
                                        Text("Back to Solution")
                                    }
                                }

                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(uiState.solutionHistory) { hist ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    onSelectHistory(hist)
                                                    showHistorySheet = false
                                                },
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                            )
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text(
                                                    text = hist.question,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Answer: ${hist.finalAnswer}",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        uiState.currentSolution != null -> {
                            val solution = uiState.currentSolution
                            val scrollState = rememberScrollState()

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(scrollState)
                            ) {
                                // Question Banner
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "Question:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = solution.question,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                // Formula Badge if present
                                if (!solution.formulaOrConcept.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.MenuBook,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.tertiary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = solution.formulaOrConcept,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onTertiaryContainer
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Step-by-Step Explanation
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = solution.stepByStepExplanation,
                                            fontSize = 13.sp,
                                            lineHeight = 19.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Final Answer Highlight Card
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "FINAL ANSWER",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 11.sp,
                                            letterSpacing = 1.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = solution.finalAnswer,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Action Buttons (Insert to Calc, Copy, Clear)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            // Extract numeric/algebraic value from final answer to insert
                                            val toInsert = solution.finalAnswer
                                                .replace("x =", "")
                                                .replace("x=", "")
                                                .trim()
                                            onInsertToCalculator(toInsert)
                                            Toast.makeText(context, "Inserted '$toInsert' into Calculator!", Toast.LENGTH_SHORT).show()
                                            onDismiss()
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("ai_insert_calc_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Input,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Insert in Calc", fontSize = 12.sp)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            val copyText = "Question: ${solution.question}\n\n${solution.stepByStepExplanation}\n\nFinal Answer: ${solution.finalAnswer}"
                                            clipboardManager.setText(AnnotatedString(copyText))
                                            Toast.makeText(context, "Full solution copied to clipboard!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.testTag("ai_copy_solution_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    FilledTonalButton(
                                        onClick = onClearSolution,
                                        modifier = Modifier.testTag("ai_new_question_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "New",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        else -> {
                            // Empty welcome state
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(vertical = 12.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    modifier = Modifier.size(64.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Ask Any Math & Science Problem",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Type or speak equations, algebra, calculus, physics, or word problems in English or Hindi/Hinglish.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                }

                // Error Banner if present
                if (uiState.errorMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = uiState.errorMessage,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom Input Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.currentQuery,
                        onValueChange = onQueryChanged,
                        placeholder = {
                            Text(
                                text = "Type your question or equation...",
                                fontSize = 13.sp
                            )
                        },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (uiState.currentQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { onQueryChanged("") },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = onTriggerVoice,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .testTag("ai_voice_input_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = "Voice Input",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        },
                        maxLines = 3,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ai_question_input_field")
                    )

                    Button(
                        onClick = { onSolve(null) },
                        enabled = uiState.currentQuery.isNotBlank() && !uiState.isLoading,
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("ai_solve_submit_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Solve Problem",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
