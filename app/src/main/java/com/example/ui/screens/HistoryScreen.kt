package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CalculationRecord
import com.example.ui.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onUseExpression: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var showClearDialog by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var restoreInputJson by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Calculation History",
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.generateBackupJson()
                            showBackupDialog = true
                        },
                        modifier = Modifier.testTag("cloud_backup_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = "Cloud Sync & Backup",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (uiState.records.isNotEmpty()) {
                        IconButton(
                            onClick = { showClearDialog = true },
                            modifier = Modifier.testTag("clear_history_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear All History",
                                tint = MaterialTheme.colorScheme.error
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Search Bar & Filter Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search expressions or results...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("search_history_input")
                )

                FilterChip(
                    selected = uiState.showOnlyFavorites,
                    onClick = { viewModel.toggleShowOnlyFavorites() },
                    label = {
                        Icon(
                            imageVector = if (uiState.showOnlyFavorites) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorites",
                            tint = if (uiState.showOnlyFavorites) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("filter_favorites_chip")
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // History List or Empty State
            if (uiState.records.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.HistoryEdu,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = if (uiState.searchQuery.isNotEmpty() || uiState.showOnlyFavorites) "No matching calculations found" else "No calculation history yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Calculations and results are automatically saved here offline",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.records, key = { it.id }) { record ->
                        HistoryItemCard(
                            record = record,
                            onToggleFavorite = { viewModel.toggleFavorite(record) },
                            onDelete = { viewModel.deleteRecord(record.id) },
                            onUseExpression = {
                                onUseExpression(record.expression)
                                Toast.makeText(context, "Loaded into calculator", Toast.LENGTH_SHORT).show()
                            },
                            onCopyResult = {
                                clipboardManager.setText(AnnotatedString(record.result))
                                Toast.makeText(context, "Copied result: ${record.result}", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }

    // Clear All Confirmation Dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All History?") },
            text = { Text("This will delete all saved calculation logs from this device.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllHistory()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Cloud Sync & Export Dialog
    if (showBackupDialog) {
        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.CloudSync, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Cloud Sync & Backup", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Export your calculation records or sync with cloud backup bundle:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = { viewModel.performCloudSync() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    ) {
                        if (uiState.isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Syncing to Cloud...")
                        } else {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Trigger Real-time Cloud Sync")
                        }
                    }

                    uiState.lastSyncMessage?.let { msg ->
                        Text(
                            text = msg,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Divider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                uiState.backupJsonString?.let { json ->
                                    clipboardManager.setText(AnnotatedString(json))
                                    Toast.makeText(context, "Backup JSON copied to clipboard!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy JSON", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                showBackupDialog = false
                                showRestoreDialog = true
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Restore", fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBackupDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Restore Backup Dialog
    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("Restore Calculations Backup") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Paste your exported JSON backup string below:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = restoreInputJson,
                        onValueChange = { restoreInputJson = it },
                        placeholder = { Text("{\"app\": \"ScientificCalculator\", ...}") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (restoreInputJson.isNotBlank()) {
                            viewModel.restoreFromBackupJson(restoreInputJson) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                if (success) showRestoreDialog = false
                            }
                        }
                    },
                    enabled = restoreInputJson.isNotBlank()
                ) {
                    Text("Import & Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun HistoryItemCard(
    record: CalculationRecord,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
    onUseExpression: () -> Unit,
    onCopyResult: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()) }
    val formattedDate = remember(record.timestamp) { sdf.format(Date(record.timestamp)) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onUseExpression)
            .testTag("history_item_${record.id}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Header Row: Category Badge + Timestamp + Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = record.category,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = formattedDate,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (record.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (record.isFavorite) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Calculation Expression
            Text(
                text = record.expression,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Result
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "= ${record.result}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onCopyResult,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Result",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onUseExpression,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = "Use in Calculator",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
