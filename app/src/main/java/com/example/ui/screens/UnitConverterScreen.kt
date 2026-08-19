package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.ConversionUnit
import com.example.engine.UnitConverter
import com.example.ui.viewmodel.UnitConverterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterScreen(
    viewModel: UnitConverterViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Unit Converter",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Category Selector Horizontal Carousel
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(UnitConverter.CATEGORIES) { category ->
                    val isSelected = uiState.selectedCategory.id == category.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectCategory(category) },
                        label = { Text(category.name, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("category_chip_${category.id}")
                    )
                }
            }

            // From Unit Card
            UnitInputCard(
                title = "From",
                unit = uiState.fromUnit,
                value = uiState.inputValue,
                allUnits = uiState.selectedCategory.units,
                onUnitSelected = { viewModel.setFromUnit(it) },
                isEditable = false,
                modifier = Modifier.fillMaxWidth()
            )

            // Swap Units Floating Action in between
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                IconButton(
                    onClick = { viewModel.swapUnits() },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .testTag("swap_units_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "Swap Units",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            // To Unit Card (Converted Result)
            UnitOutputCard(
                title = "To",
                unit = uiState.toUnit,
                value = uiState.convertedResult,
                allUnits = uiState.selectedCategory.units,
                onUnitSelected = { viewModel.setToUnit(it) },
                onCopy = {
                    clipboardManager.setText(AnnotatedString(uiState.convertedResult))
                    Toast.makeText(context, "Copied result: ${uiState.convertedResult}", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Formula Explanation Chip
            if (uiState.formulaExplanation.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = uiState.formulaExplanation,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Dedicated Conversion Numeric Keypad
            ConversionKeypad(
                onKeyPressed = { viewModel.onKeyPadInput(it) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun UnitInputCard(
    title: String,
    unit: ConversionUnit,
    value: String,
    allUnits: List<ConversionUnit>,
    onUnitSelected: (ConversionUnit) -> Unit,
    isEditable: Boolean,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Unit Selector Dropdown
                Box {
                    Surface(
                        onClick = { expanded = true },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.testTag("unit_dropdown_${title.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "${unit.name} (${unit.symbol})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select Unit",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        allUnits.forEach { u ->
                            DropdownMenuItem(
                                text = { Text("${u.name} (${u.symbol})", fontSize = 14.sp) },
                                onClick = {
                                    onUnitSelected(u)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Value Display
            Text(
                text = if (value.isEmpty()) "0" else value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag("converter_input_value")
            )
        }
    }
}

@Composable
private fun UnitOutputCard(
    title: String,
    unit: ConversionUnit,
    value: String,
    allUnits: List<ConversionUnit>,
    onUnitSelected: (ConversionUnit) -> Unit,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box {
                        Surface(
                            onClick = { expanded = true },
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.testTag("unit_dropdown_to")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "${unit.name} (${unit.symbol})",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Select Unit",
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            allUnits.forEach { u ->
                                DropdownMenuItem(
                                    text = { Text("${u.name} (${u.symbol})", fontSize = 14.sp) },
                                    onClick = {
                                        onUnitSelected(u)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Result",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Text(
                text = if (value.isEmpty()) "0" else value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("converter_output_value")
            )
        }
    }
}

@Composable
private fun ConversionKeypad(
    onKeyPressed: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        val rows = listOf(
            listOf("7", "8", "9", "⌫"),
            listOf("4", "5", "6", "C"),
            listOf("1", "2", "3", "±"),
            listOf("0", "00", ".", "100")
        )

        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                row.forEach { key ->
                    Surface(
                        onClick = { onKeyPressed(key) },
                        shape = RoundedCornerShape(12.dp),
                        color = when (key) {
                            "C", "⌫" -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
                            "±", "100" -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("converter_key_$key")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = key,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = when (key) {
                                    "C", "⌫" -> MaterialTheme.colorScheme.onErrorContainer
                                    "±", "100" -> MaterialTheme.colorScheme.onPrimaryContainer
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
