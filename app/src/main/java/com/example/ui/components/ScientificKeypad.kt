package com.example.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalKeypadColors

enum class KeyType {
    NUMBER, OPERATOR, FUNCTION, EQUALS, CLEAR, SCIENTIFIC
}

@Composable
fun ScientificKeypad(
    isScientificExpanded: Boolean,
    isSecondFunction: Boolean,
    onToggleScientific: () -> Unit,
    onToggleSecondFunction: () -> Unit,
    onKeyPressed: (String) -> Unit,
    onClear: () -> Unit,
    onAllClear: () -> Unit,
    onCalculate: () -> Unit,
    angleMode: com.example.engine.AngleMode = com.example.engine.AngleMode.DEG,
    onAngleModeToggle: (() -> Unit)? = null,
    onAngleModeSelected: ((com.example.engine.AngleMode) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val keypadColors = LocalKeypadColors.current
    val view = LocalView.current

    val triggerHaptic = {
        try {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        } catch (_: Exception) {}
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Scientific Toggle Bar / Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = {
                    triggerHaptic()
                    onToggleScientific()
                },
                shape = RoundedCornerShape(12.dp),
                color = keypadColors.functionBg.copy(alpha = 0.6f),
                modifier = Modifier.testTag("toggle_scientific_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (isScientificExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle Scientific Panel",
                        tint = keypadColors.functionText,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isScientificExpanded) "Hide Scientific" else "Scientific Mode",
                        color = keypadColors.functionText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (isScientificExpanded) {
                // When expanded, show dedicated DEG/RAD toggle right above scientific buttons
                if (onAngleModeSelected != null) {
                    AngleModeSegmentedToggle(
                        currentMode = angleMode,
                        onSelectMode = { mode ->
                            triggerHaptic()
                            onAngleModeSelected(mode)
                        },
                        modifier = Modifier.testTag("keypad_angle_mode_toggle")
                    )
                } else if (onAngleModeToggle != null) {
                    Surface(
                        onClick = {
                            triggerHaptic()
                            onAngleModeToggle()
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = keypadColors.functionBg.copy(alpha = 0.5f),
                        modifier = Modifier.testTag("keypad_angle_mode_button")
                    ) {
                        Text(
                            text = if (angleMode == com.example.engine.AngleMode.DEG) "MODE: DEG" else "MODE: RAD",
                            color = keypadColors.functionText,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            } else {
                // Quick Insert Buttons for Pi, E, and Sqrt when collapsed
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    QuickChip(label = "π", onClick = { triggerHaptic(); onKeyPressed("π") })
                    QuickChip(label = "e", onClick = { triggerHaptic(); onKeyPressed("e") })
                    QuickChip(label = "√", onClick = { triggerHaptic(); onKeyPressed("√(") })
                    QuickChip(label = "^", onClick = { triggerHaptic(); onKeyPressed("^") })
                }
            }
        }

        // Expandable Scientific Function Pad
        AnimatedVisibility(
            visible = isScientificExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Sci Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CalcKey(
                        text = "2nd",
                        type = KeyType.FUNCTION,
                        isHighlighted = isSecondFunction,
                        modifier = Modifier.weight(1f),
                        onClick = { triggerHaptic(); onToggleSecondFunction() }
                    )
                    CalcKey(
                        text = if (isSecondFunction) "sin⁻¹" else "sin",
                        type = KeyType.SCIENTIFIC,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            triggerHaptic()
                            onKeyPressed(if (isSecondFunction) "asin(" else "sin(")
                        }
                    )
                    CalcKey(
                        text = if (isSecondFunction) "cos⁻¹" else "cos",
                        type = KeyType.SCIENTIFIC,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            triggerHaptic()
                            onKeyPressed(if (isSecondFunction) "acos(" else "cos(")
                        }
                    )
                    CalcKey(
                        text = if (isSecondFunction) "tan⁻¹" else "tan",
                        type = KeyType.SCIENTIFIC,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            triggerHaptic()
                            onKeyPressed(if (isSecondFunction) "atan(" else "tan(")
                        }
                    )
                    CalcKey(
                        text = if (isSecondFunction) "10^x" else "log",
                        type = KeyType.SCIENTIFIC,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            triggerHaptic()
                            onKeyPressed(if (isSecondFunction) "10^" else "log(")
                        }
                    )
                }

                // Sci Row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CalcKey(
                        text = if (isSecondFunction) "e^x" else "ln",
                        type = KeyType.SCIENTIFIC,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            triggerHaptic()
                            onKeyPressed(if (isSecondFunction) "e^" else "ln(")
                        }
                    )
                    CalcKey(
                        text = if (isSecondFunction) "x³" else "x²",
                        type = KeyType.SCIENTIFIC,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            triggerHaptic()
                            onKeyPressed(if (isSecondFunction) "^3" else "^2")
                        }
                    )
                    CalcKey(
                        text = if (isSecondFunction) "∛" else "√",
                        type = KeyType.SCIENTIFIC,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            triggerHaptic()
                            onKeyPressed(if (isSecondFunction) "∛(" else "√(")
                        }
                    )
                    CalcKey(
                        text = "^",
                        type = KeyType.SCIENTIFIC,
                        modifier = Modifier.weight(1f),
                        onClick = { triggerHaptic(); onKeyPressed("^") }
                    )
                    CalcKey(
                        text = "x!",
                        type = KeyType.SCIENTIFIC,
                        modifier = Modifier.weight(1f),
                        onClick = { triggerHaptic(); onKeyPressed("!") }
                    )
                }

                // Sci Row 3
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CalcKey(
                        text = "π",
                        type = KeyType.SCIENTIFIC,
                        modifier = Modifier.weight(1f),
                        onClick = { triggerHaptic(); onKeyPressed("π") }
                    )
                    CalcKey(
                        text = "e",
                        type = KeyType.SCIENTIFIC,
                        modifier = Modifier.weight(1f),
                        onClick = { triggerHaptic(); onKeyPressed("e") }
                    )
                    CalcKey(
                        text = "1/x",
                        type = KeyType.SCIENTIFIC,
                        modifier = Modifier.weight(1f),
                        onClick = { triggerHaptic(); onKeyPressed("1/") }
                    )
                    CalcKey(
                        text = "|x|",
                        type = KeyType.SCIENTIFIC,
                        modifier = Modifier.weight(1f),
                        onClick = { triggerHaptic(); onKeyPressed("abs(") }
                    )
                    CalcKey(
                        text = "mod",
                        type = KeyType.SCIENTIFIC,
                        modifier = Modifier.weight(1f),
                        onClick = { triggerHaptic(); onKeyPressed(" mod ") }
                    )
                }
            }
        }

        // Standard Keypad Rows (4 rows of 4 columns)
        // Row 1: AC, ( ), %, ÷
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalcKey(
                text = "AC",
                type = KeyType.CLEAR,
                modifier = Modifier.weight(1f),
                onClick = { triggerHaptic(); onAllClear() }
            )
            CalcKey(
                text = "( )",
                type = KeyType.FUNCTION,
                modifier = Modifier.weight(1f),
                onClick = { triggerHaptic(); onKeyPressed("()") }
            )
            CalcKey(
                text = "%",
                type = KeyType.FUNCTION,
                modifier = Modifier.weight(1f),
                onClick = { triggerHaptic(); onKeyPressed("%") }
            )
            CalcKey(
                text = "÷",
                type = KeyType.OPERATOR,
                modifier = Modifier.weight(1f),
                onClick = { triggerHaptic(); onKeyPressed("÷") }
            )
        }

        // Row 2: 7, 8, 9, ×
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalcKey(
                text = "7",
                type = KeyType.NUMBER,
                modifier = Modifier.weight(1f),
                onClick = { triggerHaptic(); onKeyPressed("7") }
            )
            CalcKey(
                text = "8",
                type = KeyType.NUMBER,
                modifier = Modifier.weight(1f),
                onClick = { triggerHaptic(); onKeyPressed("8") }
            )
            CalcKey(
                text = "9",
                type = KeyType.NUMBER,
                modifier = Modifier.weight(1f),
                onClick = { triggerHaptic(); onKeyPressed("9") }
            )
            CalcKey(
                text = "×",
                type = KeyType.OPERATOR,
                modifier = Modifier.weight(1f),
                onClick = { triggerHaptic(); onKeyPressed("×") }
            )
        }

        // Row 3: 4, 5, 6, −
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalcKey(
                text = "4",
                type = KeyType.NUMBER,
                modifier = Modifier.weight(1f),
                onClick = { triggerHaptic(); onKeyPressed("4") }
            )
            CalcKey(
                text = "5",
                type = KeyType.NUMBER,
                modifier = Modifier.weight(1f),
                onClick = { triggerHaptic(); onKeyPressed("5") }
            )
            CalcKey(
                text = "6",
                type = KeyType.NUMBER,
                modifier = Modifier.weight(1f),
                onClick = { triggerHaptic(); onKeyPressed("6") }
            )
            CalcKey(
                text = "−",
                type = KeyType.OPERATOR,
                modifier = Modifier.weight(1f),
                onClick = { triggerHaptic(); onKeyPressed("−") }
            )
        }

        // Row 4: 1, 2, 3, +
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalcKey(
                text = "1",
                type = KeyType.NUMBER,
                modifier = Modifier.weight(1f),
                onClick = { triggerHaptic(); onKeyPressed("1") }
            )
            CalcKey(
                text = "2",
                type = KeyType.NUMBER,
                modifier = Modifier.weight(1f),
                onClick = { triggerHaptic(); onKeyPressed("2") }
            )
            CalcKey(
                text = "3",
                type = KeyType.NUMBER,
                modifier = Modifier.weight(1f),
                onClick = { triggerHaptic(); onKeyPressed("3") }
            )
            CalcKey(
                text = "+",
                type = KeyType.OPERATOR,
                modifier = Modifier.weight(1f),
                onClick = { triggerHaptic(); onKeyPressed("+") }
            )
        }

        // Row 5: 0, 00, ., =
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalcKey(
                text = "0",
                type = KeyType.NUMBER,
                modifier = Modifier.weight(1f),
                onClick = { triggerHaptic(); onKeyPressed("0") }
            )
            CalcKey(
                text = "00",
                type = KeyType.NUMBER,
                modifier = Modifier.weight(1f),
                onClick = { triggerHaptic(); onKeyPressed("00") }
            )
            CalcKey(
                text = ".",
                type = KeyType.NUMBER,
                modifier = Modifier.weight(1f),
                onClick = { triggerHaptic(); onKeyPressed(".") }
            )
            CalcKey(
                text = "=",
                type = KeyType.EQUALS,
                modifier = Modifier.weight(1f),
                onClick = { triggerHaptic(); onCalculate() }
            )
        }
    }
}

@Composable
private fun QuickChip(
    label: String,
    onClick: () -> Unit
) {
    val keypadColors = LocalKeypadColors.current
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = keypadColors.functionBg.copy(alpha = 0.5f),
        modifier = Modifier.testTag("quick_chip_$label")
    ) {
        Text(
            text = label,
            color = keypadColors.functionText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun CalcKey(
    text: String,
    type: KeyType,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false,
    onClick: () -> Unit
) {
    val keypadColors = LocalKeypadColors.current

    val (bg, textColor, fontSize) = when (type) {
        KeyType.NUMBER -> Triple(keypadColors.numberBg, keypadColors.numberText, 22.sp)
        KeyType.OPERATOR -> Triple(keypadColors.operatorBg, keypadColors.operatorText, 24.sp)
        KeyType.FUNCTION -> Triple(
            if (isHighlighted) keypadColors.functionText.copy(alpha = 0.3f) else keypadColors.functionBg,
            keypadColors.functionText,
            16.sp
        )
        KeyType.SCIENTIFIC -> Triple(
            keypadColors.functionBg,
            keypadColors.functionText,
            15.sp
        )
        KeyType.EQUALS -> Triple(keypadColors.equalsBg, keypadColors.equalsText, 26.sp)
        KeyType.CLEAR -> Triple(keypadColors.clearBg, keypadColors.clearText, 18.sp)
    }

    Box(
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = textColor),
                onClick = onClick
            )
            .testTag("key_$text"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = fontSize,
            fontWeight = if (type == KeyType.NUMBER) FontWeight.Normal else FontWeight.SemiBold
        )
    }
}
