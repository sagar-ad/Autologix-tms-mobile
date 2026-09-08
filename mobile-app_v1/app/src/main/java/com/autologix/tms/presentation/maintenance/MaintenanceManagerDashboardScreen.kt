package com.autologix.tms.presentation.maintenance

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.autologix.tms.core.navigation.Screen
import com.autologix.tms.data.models.DamageReportDto
import com.autologix.tms.data.models.PmScheduleDto
import com.autologix.tms.data.models.TechnicianDto
import com.autologix.tms.data.models.WorkOrderDto
import com.autologix.tms.ui.components.EmptyStateView
import com.autologix.tms.ui.components.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceManagerDashboardScreen(
    viewModel: MaintenanceManagerDashboardViewModel,
    onNavigateToRoute: (String) -> Unit,
    onLogoutSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.actionMessage) {
        uiState.actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissActionMessage()
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sign Out Confirmation") },
            text = { Text("Are you sure you want to sign out of the Maintenance Manager Console?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    viewModel.logout(onLogoutSuccess)
                }) {
                    Text("Sign Out", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Review Work Order Modal (Approve or Send for Rework)
    uiState.selectedWorkOrderForReview?.let { wo ->
        AlertDialog(
            onDismissRequest = { viewModel.selectWorkOrderForReview(null) },
            title = {
                Text("Review Completed Work: ${wo.workOrderNumber}", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Trolley: ${wo.trolleyNumber} (${wo.trolleyType})", style = MaterialTheme.typography.bodyMedium)
                    Text("Technician: ${wo.assignedTechnicianName ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
                    Text("Tech Remarks: ${wo.technicianRemarks ?: "None"}", style = MaterialTheme.typography.bodySmall)

                    OutlinedTextField(
                        value = uiState.reviewNotes,
                        onValueChange = { viewModel.updateReviewNotes(it) },
                        label = { Text("Manager Approval Notes (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = uiState.reworkReason,
                        onValueChange = { viewModel.updateReworkReason(it) },
                        label = { Text("Rework Reason (Required if sending back)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.approveWorkOrder() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("Approve Work")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { viewModel.selectWorkOrderForReview(null) }) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { viewModel.sendForRework() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Send for Rework")
                    }
                }
            }
        )
    }

    // Assign / Reassign Technician Modal
    uiState.selectedWorkOrderForAssign?.let { wo ->
        AlertDialog(
            onDismissRequest = { viewModel.selectWorkOrderForAssign(null) },
            title = { Text("Assign Work Order: ${wo.workOrderNumber}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select Technician for ${wo.trolleyNumber}:", style = MaterialTheme.typography.bodyMedium)
                    uiState.technicians.forEach { tech ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectTechnician(tech.id) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = uiState.selectedTechnicianId == tech.id,
                                onClick = { viewModel.selectTechnician(tech.id) }
                            )
                            Column(modifier = Modifier.padding(start = 8.dp)) {
                                Text(tech.name, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                Text("Active Work Orders: ${tech.activeWorkOrdersCount}  •  ${if (tech.isAvailable) "Available" else "Busy"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (tech.isAvailable) Color(0xFF10B981) else Color(0xFFF59E0B)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.confirmAssignTechnician() }) {
                    Text("Confirm Assignment")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.selectWorkOrderForAssign(null) }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Maintenance Manager", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { onNavigateToRoute(Screen.CustomerScan.route) }) {
                        Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = "Scan Trolley")
                    }
                    IconButton(onClick = { viewModel.loadDashboardData() }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Sign Out")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // KPI Summary Row
            ManagerKpiBar(kpis = uiState.kpis)

            // Tabs
            val tabs = listOf("PM Due / Overdue", "Damage Requests", "Technician Workload", "Review Queue")
            PrimaryTabRow(selectedTabIndex = uiState.selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = uiState.selectedTab == index,
                        onClick = { viewModel.selectTab(index) },
                        text = { Text(title, style = MaterialTheme.typography.labelSmall, maxLines = 1) }
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                if (uiState.isLoading) {
                    LoadingIndicator(message = "Syncing maintenance records...")
                } else {
                    when (uiState.selectedTab) {
                        0 -> PmScheduleList(
                            schedules = uiState.pmSchedules,
                            onScanTrolley = { onNavigateToRoute(Screen.CustomerScan.route) },
                            onTrolley360 = { trolleyId -> onNavigateToRoute(Screen.CustomerTrolley360.createRoute(trolleyId)) }
                        )
                        1 -> DamageRequestsQueue(
                            damages = uiState.damageRequests,
                            onTrolley360 = { trolleyId -> onNavigateToRoute(Screen.CustomerTrolley360.createRoute(trolleyId)) }
                        )
                        2 -> TechnicianWorkloadList(
                            technicians = uiState.technicians,
                            workOrders = uiState.workOrders,
                            onReassign = { wo -> viewModel.selectWorkOrderForAssign(wo) }
                        )
                        3 -> ReviewCompletedWorkQueue(
                            workOrders = uiState.workOrders.filter { it.status == "IN_REVIEW" || it.status == "COMPLETED" },
                            onReview = { wo -> viewModel.selectWorkOrderForReview(wo) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ManagerKpiBar(kpis: com.autologix.tms.data.models.MaintenanceManagerKpisDto) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        KpiMiniCard("PM Due", "${kpis.pmDueCount}", Color(0xFF0284C7), Modifier.weight(1f))
        KpiMiniCard("Overdue", "${kpis.pmOverdueCount}", Color(0xFFEF4444), Modifier.weight(1f))
        KpiMiniCard("Incidents", "${kpis.customerDamageRequestsCount}", Color(0xFFF59E0B), Modifier.weight(1f))
        KpiMiniCard("For Review", "${kpis.pendingReviewCount}", Color(0xFF6366F1), Modifier.weight(1f))
    }
}

@Composable
private fun KpiMiniCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = color)
            Text(text = title, style = MaterialTheme.typography.labelSmall, maxLines = 1)
        }
    }
}

@Composable
private fun PmScheduleList(
    schedules: List<PmScheduleDto>,
    onScanTrolley: () -> Unit,
    onTrolley360: (String) -> Unit
) {
    if (schedules.isEmpty()) {
        EmptyStateView("No PM Schedules", "All trolleys are compliant with PM cycles.")
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(schedules, key = { it.id }) { pm ->
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
                        Text(pm.trolleyNumber, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        val badgeColor = if (pm.status == "OVERDUE") Color(0xFFEF4444) else Color(0xFF0284C7)
                        Text(
                            text = pm.status,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = badgeColor
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Type: ${pm.trolleyType}  •  Due: ${pm.dueDate}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onTrolley360(pm.trolleyId) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Trolley 360°", style = MaterialTheme.typography.labelSmall)
                        }
                        Button(
                            onClick = onScanTrolley,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Scan & Inspect", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DamageRequestsQueue(
    damages: List<DamageReportDto>,
    onTrolley360: (String) -> Unit
) {
    if (damages.isEmpty()) {
        EmptyStateView("No Open Damage Requests", "No pending customer damage tickets.")
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(damages, key = { it.id }) { dmg ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(dmg.ticketNumber, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text(dmg.severity, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFFEF4444))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Trolley: ${dmg.trolleyNumber}", style = MaterialTheme.typography.bodySmall)
                    Text(dmg.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onTrolley360(dmg.trolleyId) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("View 360°", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TechnicianWorkloadList(
    technicians: List<TechnicianDto>,
    workOrders: List<WorkOrderDto>,
    onReassign: (WorkOrderDto) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Technician Workload Roster", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
        }
        items(technicians, key = { it.id }) { tech ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Engineering, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(tech.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("${tech.email}  •  ${tech.phone ?: ""}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Active Tasks: ${tech.activeWorkOrdersCount}  •  ${if (tech.isAvailable) "Available" else "At Capacity"}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (tech.isAvailable) Color(0xFF10B981) else Color(0xFFF59E0B)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text("Active Assigned Work Orders (Tap to Reassign)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
        }
        items(workOrders, key = { it.id }) { wo ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onReassign(wo) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${wo.workOrderNumber}  •  ${wo.trolleyNumber}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Tech: ${wo.assignedTechnicianName ?: "Unassigned"}  •  Status: ${wo.status}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(onClick = { onReassign(wo) }) {
                        Text("Reassign", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewCompletedWorkQueue(
    workOrders: List<WorkOrderDto>,
    onReview: (WorkOrderDto) -> Unit
) {
    if (workOrders.isEmpty()) {
        EmptyStateView("No Pending Reviews", "No work orders awaiting manager approval.")
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(workOrders, key = { it.id }) { wo ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(wo.workOrderNumber, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text(wo.status, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = Color(0xFF6366F1))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Trolley: ${wo.trolleyNumber} (${wo.trolleyType})", style = MaterialTheme.typography.bodySmall)
                    Text("Completed by: ${wo.assignedTechnicianName ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
                    Text("Remarks: ${wo.technicianRemarks ?: "None"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { onReview(wo) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Review & Approve / Rework")
                    }
                }
            }
        }
    }
}
