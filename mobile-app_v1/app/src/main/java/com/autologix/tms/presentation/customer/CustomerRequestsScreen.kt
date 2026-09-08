package com.autologix.tms.presentation.customer

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autologix.tms.data.models.DamageReportDto
import com.autologix.tms.ui.components.EmptyStateView
import com.autologix.tms.ui.components.LoadingIndicator
import com.autologix.tms.ui.components.NetworkErrorView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerRequestsScreen(
    initialFilter: String = "ALL",
    viewModel: CustomerRequestsViewModel,
    onNavigateBack: () -> Unit,
    onRaiseDamageClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(initialFilter) {
        if (initialFilter.isNotBlank()) {
            viewModel.setFilter(initialFilter)
        }
    }

    val tabs = listOf("ALL", "PENDING", "OPEN", "RESOLVED")
    val selectedTabIndex = tabs.indexOf(uiState.selectedFilter).coerceAtLeast(0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Damage & Incident Requests", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadRequests(isRefresh = true) }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Filter Tabs
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { viewModel.setFilter(tab) },
                        text = {
                            Text(
                                text = tab,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when {
                    uiState.isLoading && !uiState.isRefreshing -> {
                        LoadingIndicator(message = "Loading customer requests...")
                    }
                    uiState.isNetworkError && uiState.errorMessage != null -> {
                        NetworkErrorView(
                            message = uiState.errorMessage ?: "Network error",
                            onRetry = { viewModel.loadRequests() }
                        )
                    }
                    uiState.filteredRequests.isEmpty() -> {
                        EmptyStateView(
                            title = "No ${uiState.selectedFilter} Requests",
                            message = "No customer incident requests found under status '${uiState.selectedFilter}'.",
                            actionLabel = "Raise New Damage Request",
                            onActionClick = onRaiseDamageClick
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.filteredRequests, key = { it.id }) { request ->
                                RequestCard(
                                    request = request,
                                    onClick = { viewModel.selectRequest(request) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Request Detail Bottom Sheet
        if (uiState.selectedRequest != null) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { viewModel.selectRequest(null) },
                sheetState = sheetState
            ) {
                RequestDetailSheetContent(
                    request = uiState.selectedRequest!!,
                    onClose = { viewModel.selectRequest(null) }
                )
            }
        }
    }
}

@Composable
private fun RequestCard(
    request: DamageReportDto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Ticket No & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    request.status.contains("RESOLVED", ignoreCase = true) -> Color(0xFF10B981)
                                    request.status.contains("PROGRESS", ignoreCase = true) || request.status.contains("OPEN", ignoreCase = true) -> Color(0xFF3B82F6)
                                    else -> Color(0xFFF59E0B)
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = request.ticketNo,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                RequestStatusBadge(status = request.status)
            }

            // Trolley & Severity
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Trolley: ${request.trolleySerialNo.ifBlank { request.trolleyId }}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )

                Text(
                    text = "Severity: ${request.severity}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = when (request.severity) {
                            "CRITICAL", "HIGH" -> Color(0xFFEF4444)
                            "MODERATE" -> Color(0xFFF59E0B)
                            else -> Color(0xFF10B981)
                        }
                    )
                )
            }

            // Description summary
            Text(
                text = request.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            // Work Order info if available
            if (!request.workOrderId.isNullOrBlank()) {
                Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Build, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Work Order: ${request.workOrderId}", style = MaterialTheme.typography.labelSmall)
                    }

                    if (!request.assignedTechnician.isNullOrBlank()) {
                        Text(text = "Tech: ${request.assignedTechnician}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun RequestStatusBadge(status: String) {
    val (bg, fg) = when {
        status.contains("RESOLVED", ignoreCase = true) || status.contains("CLOSED", ignoreCase = true) ->
            Pair(Color(0xFFDCFCE7), Color(0xFF16A34A))
        status.contains("PROGRESS", ignoreCase = true) || status.contains("OPEN", ignoreCase = true) ->
            Pair(Color(0xFFDBEAFE), Color(0xFF2563EB))
        else ->
            Pair(Color(0xFFFEF3C7), Color(0xFFD97706))
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = status.replace("_", " "),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = fg
        )
    }
}

@Composable
private fun RequestDetailSheetContent(
    request: DamageReportDto,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = request.ticketNo,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                )
                Text(
                    text = "Reported on ${request.createdAt}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            RequestStatusBadge(status = request.status)
        }

        Divider()

        // Lifecycle Stages Flow
        Text(text = "Resolution Lifecycle Timeline", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))

        LifecycleStepRow(
            title = "1. Customer Incident Logged",
            detail = "Request recorded by customer user for trolley ${request.trolleySerialNo.ifBlank { request.trolleyId }}",
            isDone = true
        )
        LifecycleStepRow(
            title = "2. Supervisor Triage & Review",
            detail = "Damage severity evaluated as ${request.severity}",
            isDone = true
        )
        LifecycleStepRow(
            title = "3. Maintenance Work Order",
            detail = if (request.workOrderId != null) "Assigned to work order ${request.workOrderId}" else "Pending work order scheduling",
            isDone = request.workOrderId != null
        )
        LifecycleStepRow(
            title = "4. Technician Repair & Testing",
            detail = if (request.assignedTechnician != null) "Lead Tech: ${request.assignedTechnician}" else "Awaiting bay technician assignment",
            isDone = request.assignedTechnician != null
        )
        LifecycleStepRow(
            title = "5. Quality Inspection & Closure",
            detail = if (request.status.contains("RESOLVED", ignoreCase = true)) request.resolutionNotes ?: "Repairs completed and validated by QA inspector" else "Final handover pending completion",
            isDone = request.status.contains("RESOLVED", ignoreCase = true)
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun LifecycleStepRow(
    title: String,
    detail: String,
    isDone: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (isDone) Color(0xFF10B981) else Color(0xFF94A3B8)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.HourglassEmpty,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDone) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
