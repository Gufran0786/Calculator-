package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.MathScienceSymbol
import com.example.engine.ScienceConstant
import com.example.engine.ScienceConstants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConstantsSheet(
    onDismiss: () -> Unit,
    onInsertConstant: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Constants, 1: Symbols
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    val clipboardManager = LocalClipboardManager.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.testTag("constants_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            // Sheet Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Math & Science Constants",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tab Row (Constants vs Symbols)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0; selectedCategory = "All" },
                    text = { Text("Physical & Math Constants", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1; selectedCategory = "All" },
                    text = { Text("Symbols Catalog", fontWeight = FontWeight.SemiBold) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by name, symbol, category...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_constants_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (selectedTab == 0) {
                // Category Filter Chips
                val categories = listOf("All", "Universal", "Quantum", "Astrophysics", "Electromagnetism", "Thermodynamics", "Atomic", "Chemistry", "Math")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Constants List
                val filteredConstants = ScienceConstants.CONSTANTS.filter { item ->
                    (selectedCategory == "All" || item.category.equals(selectedCategory, ignoreCase = true)) &&
                            (searchQuery.isBlank() ||
                                    item.name.contains(searchQuery, ignoreCase = true) ||
                                    item.symbol.contains(searchQuery, ignoreCase = true) ||
                                    item.category.contains(searchQuery, ignoreCase = true))
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                ) {
                    items(filteredConstants) { constItem ->
                        ConstantItemCard(
                            item = constItem,
                            onInsert = {
                                onInsertConstant(constItem.value.toString())
                                onDismiss()
                            },
                            onCopy = {
                                clipboardManager.setText(AnnotatedString(constItem.value.toString()))
                            }
                        )
                    }
                }
            } else {
                // Symbols List
                val categories = listOf("All", "Calculus", "Algebra", "Relations", "Set Theory", "Logic", "Greek Alphabet")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val filteredSymbols = ScienceConstants.SYMBOLS.filter { item ->
                    (selectedCategory == "All" || item.category.equals(selectedCategory, ignoreCase = true)) &&
                            (searchQuery.isBlank() ||
                                    item.name.contains(searchQuery, ignoreCase = true) ||
                                    item.symbol.contains(searchQuery, ignoreCase = true) ||
                                    item.category.contains(searchQuery, ignoreCase = true))
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                ) {
                    items(filteredSymbols) { symbolItem ->
                        SymbolItemCard(
                            item = symbolItem,
                            onInsert = {
                                onInsertConstant(symbolItem.insertableAs)
                                onDismiss()
                            },
                            onCopy = {
                                clipboardManager.setText(AnnotatedString(symbolItem.symbol))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConstantItemCard(
    item: ScienceConstant,
    onInsert: () -> Unit,
    onCopy: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onInsert)
            .testTag("constant_card_${item.symbol}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = item.symbol,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = FontFamily.Serif
                    )
                    Text(
                        text = item.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "${item.valueStr} ${item.unit}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.secondary
                )

                Text(
                    text = item.description,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onCopy) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Button(
                    onClick = onInsert,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Insert", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun SymbolItemCard(
    item: MathScienceSymbol,
    onInsert: () -> Unit,
    onCopy: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onInsert)
            .testTag("symbol_card_${item.symbol}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = item.symbol,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Example: ${item.example}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = item.description,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onCopy) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy Symbol",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
