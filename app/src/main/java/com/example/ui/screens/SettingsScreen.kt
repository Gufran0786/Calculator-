package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.AngleMode
import com.example.ui.theme.ThemePalettes
import com.example.ui.theme.ThemePreset
import com.example.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings & Customization",
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp
                    )
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Theme Customization Section
            item {
                Text(
                    text = "Dark Mode Interface Theme",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemePreset.values().forEach { preset ->
                        ThemePresetCard(
                            preset = preset,
                            isSelected = uiState.currentTheme == preset,
                            onSelect = { viewModel.setTheme(preset) }
                        )
                    }
                }
            }

            // Calculation Precision Slider
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Decimal Precision",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "${uiState.decimalPrecision} decimals",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Slider(
                            value = uiState.decimalPrecision.toFloat(),
                            onValueChange = { viewModel.setPrecision(it.toInt()) },
                            valueRange = 2f..12f,
                            steps = 9,
                            modifier = Modifier.testTag("precision_slider")
                        )
                    }
                }
            }

            // Default Angle Mode
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Default Angle Unit",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "For trigonometric sin/cos/tan evaluations",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = uiState.defaultAngleMode == AngleMode.DEG,
                                onClick = { viewModel.setDefaultAngleMode(AngleMode.DEG) },
                                label = { Text("Degrees (DEG)") }
                            )
                            FilterChip(
                                selected = uiState.defaultAngleMode == AngleMode.RAD,
                                onClick = { viewModel.setDefaultAngleMode(AngleMode.RAD) },
                                label = { Text("Radians (RAD)") }
                            )
                        }
                    }
                }
            }

            // Haptic Feedback Toggle
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.Vibration, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Column {
                                Text("Keypad Haptic Vibration", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                Text("Tactile click vibration on key tap", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = uiState.hapticFeedbackEnabled,
                            onCheckedChange = { viewModel.toggleHapticFeedback() },
                            modifier = Modifier.testTag("haptic_switch")
                        )
                    }
                }
            }

            // Voice-to-Text Syntax Tips & Help
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("Voice Math Input Capabilities", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        Text(
                            text = "You can speak full calculations in English or Hindi/Hinglish:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        val tips = listOf(
                            "\"square root of 256 plus 44\" → √(256) + 44",
                            "\"sin of 45 degrees multiplied by pi\" → sin(45) × π",
                            "\"five power three divided by ten\" → 5^3 ÷ 10",
                            "\"log of 1000 minus natural log of e\" → log(1000) - ln(e)",
                            "\"pachaas guna bees bhaag paanch\" → 50 × 20 ÷ 5"
                        )

                        tips.forEach { tip ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.background,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = tip,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ThemePresetCard(
    preset: ThemePreset,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val keypadColors = remember(preset) { ThemePalettes.getKeypadColors(preset) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .testTag("theme_card_${preset.name}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Color dots preview
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(keypadColors.displayBg))
                    Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(keypadColors.equalsBg))
                    Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(keypadColors.functionText))
                    Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(keypadColors.operatorText))
                }

                Column {
                    Text(
                        text = preset.displayName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = preset.description,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            RadioButton(
                selected = isSelected,
                onClick = onSelect
            )
        }
    }
}
