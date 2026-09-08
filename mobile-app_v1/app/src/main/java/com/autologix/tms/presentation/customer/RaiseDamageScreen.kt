package com.autologix.tms.presentation.customer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autologix.tms.data.models.DamageChecklistItemDto
import com.autologix.tms.data.models.DamageReportDto
import com.autologix.tms.ui.components.AppButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RaiseDamageScreen(
    trolleyId: String?,
    viewModel: RaiseDamageViewModel,
    onNavigateBack: () -> Unit,
    onScanClick: () -> Unit,
    onViewSubmittedRequest: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(trolleyId) {
        if (!trolleyId.isNullOrBlank()) {
            viewModel.initializeTrolley(trolleyId)
        }
    }

    // Submission Confirmation Dialog (Mandatory UX Requirement)
    if (uiState.showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissConfirmDialog() },
            title = {
                Text(
                    text = "Confirm Damage Request",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Are you sure you want to submit this incident report for trolley ${uiState.trolleySerialNo.ifBlank { uiState.trolleyId }}?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Severity: ${uiState.severity}\nChecklist items flagged: ${uiState.selectedChecklistIds.size}\nAttached photos: ${uiState.photos.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "This will immediately notify TMS plant supervisors and dispatch a maintenance technician.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFEF4444)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmAndSubmit() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Confirm & Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissConfirmDialog() }) {
                    Text("Review")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Raise Damage Request", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (uiState.submittedReport != null) {
                // Feature 19: View Request Number & Feature 20: View Request Status
                SubmittedSuccessView(
                    report = uiState.submittedReport!!,
                    onViewRequest = { onViewSubmittedRequest(uiState.submittedReport!!.id) },
                    onDone = onNavigateBack
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Step 1: Trolley Identifier Card
                    item {
                        TrolleyTargetCard(
                            trolleyId = uiState.trolleyId,
                            serialNo = uiState.trolleySerialNo,
                            onScanClick = onScanClick
                        )
                    }

                    // Step 2: Severity Selector
                    item {
                        SeveritySelectorCard(
                            selectedSeverity = uiState.severity,
                            onSelect = { viewModel.setSeverity(it) }
                        )
                    }

                    // Step 3: Damage/Repair Checklist (Feature 13)
                    item {
                        DamageChecklistCard(
                            checklist = uiState.availableChecklist,
                            selectedIds = uiState.selectedChecklistIds,
                            onToggle = { viewModel.toggleChecklistItem(it) }
                        )
                    }

                    // Step 4: Concern Details & Description (Features 14 & 15)
                    item {
                        ConcernDetailsCard(
                            concernDetails = uiState.concernDetails,
                            description = uiState.damageDescription,
                            locationNotes = uiState.locationNotes,
                            onConcernChange = { viewModel.setConcernDetails(it) },
                            onDescriptionChange = { viewModel.setDamageDescription(it) },
                            onLocationChange = { viewModel.setLocationNotes(it) }
                        )
                    }

                    // Step 5: Capture & Upload Damage Photos (Features 16 & 17)
                    item {
                        PhotoCaptureCard(
                            photos = uiState.photos,
                            isUploading = uiState.isUploadingPhotos,
                            onAddPhoto = {
                                val simulatedUri = "content://media/photos/${System.currentTimeMillis()}.jpg"
                                viewModel.addPhoto(simulatedUri)
                            },
                            onRemovePhoto = { viewModel.removePhoto(it) }
                        )
                    }

                    // Error message banner if any
                    if (uiState.errorMessage != null) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFDC2626))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = uiState.errorMessage!!,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFDC2626)
                                    )
                                }
                            }
                        }
                    }

                    // Submit Action with Confirmation Trigger
                    item {
                        Button(
                            onClick = { viewModel.requestSubmitConfirmation() },
                            enabled = uiState.isFormValid && !uiState.isSubmitting,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                        ) {
                            if (uiState.isSubmitting) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Submitting to TMS...")
                            } else {
                                Icon(imageVector = Icons.Default.ReportProblem, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Submit Damage Request")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrolleyTargetCard(
    trolleyId: String,
    serialNo: String,
    onScanClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = "TROLLEY IDENTIFIER", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                Text(
                    text = if (serialNo.isNotBlank()) serialNo else if (trolleyId.isNotBlank()) trolleyId else "No Trolley Selected",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                )
            }
            OutlinedButton(
                onClick = onScanClick,
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (trolleyId.isBlank()) "Scan QR" else "Change")
            }
        }
    }
}

@Composable
private fun SeveritySelectorCard(
    selectedSeverity: String,
    onSelect: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(text = "Damage Severity Level", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val levels = listOf("MINOR", "MODERATE", "HIGH", "CRITICAL")
                levels.forEach { level ->
                    val isSelected = selectedSeverity == level
                    val color = when (level) {
                        "MINOR" -> Color(0xFF10B981)
                        "MODERATE" -> Color(0xFFF59E0B)
                        "HIGH" -> Color(0xFFEA580C)
                        else -> Color(0xFFEF4444)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) color else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onSelect(level) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = level,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DamageChecklistCard(
    checklist: List<DamageChecklistItemDto>,
    selectedIds: Set<String>,
    onToggle: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Damage & Repair Checklist",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Select all observed defects to guide the maintenance repair team:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            checklist.forEach { item ->
                val isChecked = selectedIds.contains(item.id)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onToggle(item.id) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { onToggle(item.id) },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFFEF4444))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal
                            )
                        )
                        if (item.isCritical) {
                            Text(
                                text = "CRITICAL SAFETY HAZARD",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFEF4444)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConcernDetailsCard(
    concernDetails: String,
    description: String,
    locationNotes: String,
    onConcernChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onLocationChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(text = "Concern Details & Description", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))

            OutlinedTextField(
                value = concernDetails,
                onValueChange = onConcernChange,
                label = { Text("Concern Subject / Summary") },
                placeholder = { Text("e.g. Front castor seized under load") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = { Text("Detailed Damage Description *") },
                placeholder = { Text("Describe observed damage, noises, structural bending, or operational impact (min 10 chars)...") },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
                value = locationNotes,
                onValueChange = onLocationChange,
                label = { Text("Incident Location / Plant Bay") },
                placeholder = { Text("e.g. Press Shop Bay 3 or In-transit Dock 2") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )
        }
    }
}

@Composable
private fun PhotoCaptureCard(
    photos: List<PhotoUploadItem>,
    isUploading: Boolean,
    onAddPhoto: () -> Unit,
    onRemovePhoto: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Damage Photos & Evidence", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                if (isUploading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Uploading...", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    // Camera / Add Button
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                            .clickable { onAddPhoto() },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Add Photo", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                items(photos) { photo ->
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1E293B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = Color(0xFF94A3B8))
                            Text("Evidence", style = MaterialTheme.typography.labelSmall, color = Color.White)
                        }

                        IconButton(
                            onClick = { onRemovePhoto(photo.id) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f))
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubmittedSuccessView(
    report: DamageReportDto,
    onViewRequest: () -> Unit,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF10B981),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Damage Request Submitted",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Ticket generated and assigned to plant maintenance supervisors.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Request Number Card (Feature 19 & Feature 20)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Request Number", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(report.ticketNo, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace))
                }
                Divider()
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Status", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFFEF3C7))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = report.status,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                        )
                    }
                }
                Divider()
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Trolley", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(report.trolleySerialNo.ifBlank { report.trolleyId }, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onViewRequest,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Track Request Lifecycle")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Return to Customer Portal")
        }
    }
}
