package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.AngleMode
import com.example.ui.theme.LocalKeypadColors

@Composable
fun CalculatorDisplay(
    expression: String,
    livePreview: String,
    angleMode: AngleMode,
    isSecondFunction: Boolean,
    onAngleModeToggle: () -> Unit,
    onAngleModeSelected: (AngleMode) -> Unit = { mode ->
        if (mode != angleMode) onAngleModeToggle()
    },
    onVoiceClick: () -> Unit,
    onConstantsClick: () -> Unit,
    onBackspaceClick: () -> Unit,
    onCopyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keypadColors = LocalKeypadColors.current
    val scrollState = rememberScrollState()

    LaunchedEffect(expression) {
        if (expression.isNotEmpty()) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(
            containerColor = keypadColors.displayBg
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Status & Quick Action Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dedicated Angle Mode Dual-Segment Toggle & 2nd indicator
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Dedicated Deg / Rad Segmented Switch
                    AngleModeSegmentedToggle(
                        currentMode = angleMode,
                        onSelectMode = onAngleModeSelected
                    )

                    AnimatedVisibility(visible = isSecondFunction) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = keypadColors.functionText.copy(alpha = 0.2f),
                            modifier = Modifier.padding(horizontal = 2.dp)
                        ) {
                            Text(
                                text = "2nd",
                                color = keypadColors.functionText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = keypadColors.functionBg.copy(alpha = 0.35f),
                        modifier = Modifier.padding(start = 2.dp)
                    ) {
                        Text(
                            text = "Gufran Khan",
                            color = keypadColors.accentGlow,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                // Quick Action Buttons (Constants, Voice, Copy, Backspace)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onConstantsClick,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("constants_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = "Constants & Symbols",
                            tint = keypadColors.functionText,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onVoiceClick,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("voice_input_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Input",
                            tint = keypadColors.accentGlow,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onCopyClick,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("copy_display_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Result",
                            tint = keypadColors.displaySubText,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onBackspaceClick,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("backspace_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Backspace,
                            contentDescription = "Backspace",
                            tint = keypadColors.clearText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Input Expression Display (Horizontally scrollable)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (expression.isEmpty()) "0" else expression,
                    color = if (expression.isEmpty()) keypadColors.displaySubText.copy(alpha = 0.6f) else keypadColors.displayText,
                    fontSize = if (expression.length > 14) 28.sp else 38.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Default,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    modifier = Modifier.testTag("calculator_expression_text")
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Live Preview of Calculation Result
            AnimatedVisibility(
                visible = livePreview.isNotEmpty() && livePreview != expression,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "= $livePreview",
                        color = keypadColors.accentGlow,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        modifier = Modifier.testTag("calculator_preview_text")
                    )
                }
            }
        }
    }
}

/**
 * Dedicated Segmented Dual-Button UI Toggle for switching between Radians and Degrees
 */
@Composable
fun AngleModeSegmentedToggle(
    currentMode: AngleMode,
    onSelectMode: (AngleMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val keypadColors = LocalKeypadColors.current

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = keypadColors.functionBg.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, keypadColors.functionText.copy(alpha = 0.18f)),
        modifier = modifier.testTag("angle_mode_segmented_toggle")
    ) {
        Row(
            modifier = Modifier.padding(2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            AngleToggleSegmentItem(
                label = "DEG",
                isSelected = currentMode == AngleMode.DEG,
                onClick = { onSelectMode(AngleMode.DEG) },
                testTag = "angle_toggle_deg"
            )

            AngleToggleSegmentItem(
                label = "RAD",
                isSelected = currentMode == AngleMode.RAD,
                onClick = { onSelectMode(AngleMode.RAD) },
                testTag = "angle_toggle_rad"
            )
        }
    }
}

@Composable
private fun AngleToggleSegmentItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val keypadColors = LocalKeypadColors.current
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) keypadColors.operatorBg else Color.Transparent,
        animationSpec = tween(durationMillis = 180),
        label = "segment_bg_$label"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) keypadColors.operatorText else keypadColors.displaySubText.copy(alpha = 0.8f),
        animationSpec = tween(durationMillis = 180),
        label = "segment_text_$label"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(9.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 4.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
