package com.autologix.tms.presentation.technician

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.autologix.tms.data.models.ChecklistItemDto
import com.autologix.tms.data.models.WorkOrderDto
import com.autologix.tms.ui.components.EmptyStateView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechnicianWorkOrderExecutionScreen(
    workOrderId: String,
    viewModel: TechnicianDashboardViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var newPartName by remember { mutableStateOf("") }
    var newPartQty by remember { mutableStateOf("1") }
    var showAddPartDialog by remember { mutableStateOf(false) }

    val currentOrder = uiState.activeExecutionOrder ?: uiState.workOrders.find { it.id == workOrderId }

    LaunchedEffect(workOrderId) {
        viewModel.openWorkOrderExecution(workOrderId)
    }

    LaunchedEffect(uiState.actionMessage) {
        uiState.actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissActionMessage()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissActionMessage()
        }
    }

    if (showAddPartDialog) {
        AlertDialog(
            onDismissRequest = { showAddPartDialog = false },
            title = { Text("Add Replaced Spare Part") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newPartName,
                        onValueChange = { newPartName = it },
                        label = { Text("Part Name (e.g. PU Castor Wheel 150mm)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPartQty,
                        onValueChange = { newPartQty = it },
                        label = { Text("Quantity") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val qty = newPartQty.toIntOrNull() ?: 1
                    viewModel.addSparePart(newPartName, qty)
                    newPartName = ""
                    newPartQty = "1"
                    showAddPartDialog = false
                }) {
                    Text("Add Part")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPartDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentOrder?.workOrderNumber ?: "Work Order Execution") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (currentOrder != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (currentOrder.status == "ASSIGNED") {
                            Button(
                                onClick = { viewModel.startWorkOrder() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                            ) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Start Work")
                            }
                        } else {
                            Button(
                                onClick = { viewModel.submitForManagerReview() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                            ) {
                                Icon(imageVector = Icons.Default.Send, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Submit for Review")
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (currentOrder == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                EmptyStateView("Work Order Not Found", "The requested work order could not be located.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Rework Notice Banner
                if (currentOrder.status == "REWORK_REQUESTED") {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("REWORK REQUIRED", fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C))
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = currentOrder.reworkReason ?: "Manager requested revision on this work order.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF7F1D1D)
                                )
                            }
                        }
                    }
                }

                // Trolley Info Header Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Trolley: ${currentOrder.trolleyNumber}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("Type: ${currentOrder.trolleyType}  •  Status: ${currentOrder.status}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (!currentOrder.damageDescription.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Reported Concern: ${currentOrder.damageDescription}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFEF4444))
                            }
                        }
                    }
                }

                // Checklist Section
                item {
                    Text("Inspection & Repair Checklist", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                }

                items(uiState.checklist, key = { it.id }) { item ->
                    ChecklistExecutionItemCard(
                        item = item,
                        onStatusChanged = { status -> viewModel.updateChecklistItemStatus(item.id, status) }
                    )
                }

                // Technician Remarks
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Technician Diagnostic & Repair Remarks", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = uiState.remarks,
                        onValueChange = { viewModel.updateRemarks(it) },
                        label = { Text("Enter actions taken, adjustments made, torque verified...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }

                // Parts Replaced Section
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Parts / Spares Replaced", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        TextButton(onClick = { showAddPartDialog = true }) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Spare")
                        }
                    }

                    if (uiState.partsReplaced.isEmpty()) {
                        Text("No spare parts logged for this task.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            uiState.partsReplaced.forEach { part ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${part.partName} (x${part.quantity})", style = MaterialTheme.typography.bodyMedium)
                                    IconButton(
                                        onClick = { viewModel.removeSparePart(part.partId) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Close, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }

                // Before & After Photos Section
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Work Order Photos (Before & After)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.uploadBeforePhotoMock() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Before (${uiState.beforePhotoUrls.size})", style = MaterialTheme.typography.labelSmall)
                        }
                        OutlinedButton(
                            onClick = { viewModel.uploadAfterPhotoMock() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("After (${uiState.afterPhotoUrls.size})", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}

@Composable
fun ChecklistExecutionItemCard(
    item: ChecklistItemDto,
    onStatusChanged: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(item.section, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text(item.title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
            Text("Criteria: ${item.criteria}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = item.status == "PASS",
                    onClick = { onStatusChanged("PASS") },
                    label = { Text("PASS") },
                    leadingIcon = if (item.status == "PASS") {
                        { Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF10B981)) }
                    } else null
                )
                FilterChip(
                    selected = item.status == "FAIL",
                    onClick = { onStatusChanged("FAIL") },
                    label = { Text("FAIL") },
                    leadingIcon = if (item.status == "FAIL") {
                        { Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFEF4444)) }
                    } else null
                )
                FilterChip(
                    selected = item.status == "FLAGGED",
                    onClick = { onStatusChanged("FLAGGED") },
                    label = { Text("FLAG") },
                    leadingIcon = if (item.status == "FLAGGED") {
                        { Icon(Icons.Default.Flag, contentDescription = null, tint = Color(0xFFF59E0B)) }
                    } else null
                )
            }
        }
    }
}
