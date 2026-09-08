package com.autologix.tms.presentation.logistics

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCodeScanner
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
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrolleyMovementScreen(
    initialType: String,
    initialTrolleyId: String?,
    viewModel: LogisticsDashboardViewModel,
    onNavigateBack: () -> Unit,
    onOpenScan: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(initialType) {
        if (initialType.isNotBlank()) {
            viewModel.setMovementFormType(initialType)
        }
    }

    LaunchedEffect(initialTrolleyId) {
        if (!initialTrolleyId.isNullOrBlank()) {
            viewModel.setFormTrolley(initialTrolleyId)
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Gate ${uiState.movementFormType}") },
            text = {
                Text("Are you sure you want to record Gate-${uiState.movementFormType} for trolley ${uiState.trolleyNumber} on truck ${uiState.truckNumber}?")
            },
            confirmButton = {
                Button(onClick = {
                    showConfirmDialog = false
                    viewModel.submitMovement(onSuccess = onNavigateBack)
                }) {
                    Text("Confirm & Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gate ${uiState.movementFormType}: ${uiState.trolleyNumber}") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onOpenScan) {
                        Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = "Scan Trolley")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    val btnColor = if (uiState.movementFormType == "IN") Color(0xFF10B981) else Color(0xFF0284C7)
                    Button(
                        onClick = { showConfirmDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = btnColor),
                        enabled = !uiState.isSubmitting
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Record Gate-${uiState.movementFormType} Entry")
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Movement Type Toggle (IN vs OUT)
            item {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = uiState.movementFormType == "IN",
                        onClick = { viewModel.setMovementFormType("IN") },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Text("Trolley IN (Receiving)")
                    }
                    SegmentedButton(
                        selected = uiState.movementFormType == "OUT",
                        onClick = { viewModel.setMovementFormType("OUT") },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Text("Trolley OUT (Dispatch)")
                    }
                }
            }

            // Trolley Identification Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Target Trolley", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            OutlinedButton(onClick = onOpenScan) {
                                Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Scan QR", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = uiState.trolleyNumber,
                            onValueChange = { viewModel.setFormTrolley(it) },
                            label = { Text("Trolley Number / Serial") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Location & Document Details Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = if (uiState.movementFormType == "IN") "Source & Gate Pass" else "Destination & Dispatch",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )

                        OutlinedTextField(
                            value = uiState.location,
                            onValueChange = { viewModel.updateFormField(location = it) },
                            label = { Text(if (uiState.movementFormType == "IN") "From Location / Vendor Plant" else "To Location / Delivery Dock") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = uiState.referenceNumber,
                            onValueChange = { viewModel.updateFormField(referenceNumber = it) },
                            label = { Text(if (uiState.movementFormType == "IN") "Gate Pass Number" else "Dispatch / DC Number") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = uiState.project,
                            onValueChange = { viewModel.updateFormField(project = it) },
                            label = { Text("Project / Assembly Line") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = uiState.customer,
                            onValueChange = { viewModel.updateFormField(customer = it) },
                            label = { Text("Customer / Organization") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Carrier / Transport Details Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Carrier & Vehicle Info", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

                        OutlinedTextField(
                            value = uiState.truckNumber,
                            onValueChange = { viewModel.updateFormField(truckNumber = it) },
                            label = { Text("Truck / Vehicle Registration Number") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = uiState.driverName,
                            onValueChange = { viewModel.updateFormField(driverName = it) },
                            label = { Text("Driver Name") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (uiState.movementFormType == "OUT") {
                            OutlinedTextField(
                                value = uiState.sealNumber,
                                onValueChange = { viewModel.updateFormField(sealNumber = it) },
                                label = { Text("Security Seal Number") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Condition Status & Photos (Particularly for Gate IN)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Condition Status & Inspection", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = uiState.conditionStatus == "GOOD",
                                onClick = { viewModel.updateFormField(conditionStatus = "GOOD") },
                                label = { Text("GOOD") }
                            )
                            FilterChip(
                                selected = uiState.conditionStatus == "MINOR_DAMAGE",
                                onClick = { viewModel.updateFormField(conditionStatus = "MINOR_DAMAGE") },
                                label = { Text("MINOR DAMAGE") }
                            )
                            FilterChip(
                                selected = uiState.conditionStatus == "MAJOR_DAMAGE",
                                onClick = { viewModel.updateFormField(conditionStatus = "MAJOR_DAMAGE") },
                                label = { Text("MAJOR DAMAGE") }
                            )
                        }

                        OutlinedTextField(
                            value = uiState.remarks,
                            onValueChange = { viewModel.updateFormField(remarks = it) },
                            label = { Text("Dock Operator Remarks / Damage Notes") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )

                        OutlinedButton(
                            onClick = { viewModel.addMockPhoto() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.AddAPhoto, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Attach Inspection Photo (${uiState.photoUrls.size})")
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
