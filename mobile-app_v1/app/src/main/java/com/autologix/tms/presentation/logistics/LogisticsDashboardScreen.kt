package com.autologix.tms.presentation.logistics

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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.autologix.tms.core.navigation.Screen
import com.autologix.tms.data.models.MovementRecordDto
import com.autologix.tms.ui.components.EmptyStateView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogisticsDashboardScreen(
    viewModel: LogisticsDashboardViewModel,
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
            text = { Text("Are you sure you want to sign out from Logistics Operations?") },
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Logistics & Movements", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { onNavigateToRoute(Screen.CustomerScan.route) }) {
                        Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = "Scan Trolley")
                    }
                    IconButton(onClick = { viewModel.loadLogisticsData() }) {
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // KPI Counters Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LogisticsMetricCard("Today's IN", "${uiState.kpis.todayInCount}", Color(0xFF10B981), Modifier.weight(1f))
                    LogisticsMetricCard("Today's OUT", "${uiState.kpis.todayOutCount}", Color(0xFF0284C7), Modifier.weight(1f))
                    LogisticsMetricCard("Yard Balance", "${uiState.kpis.netYardBalance}", Color(0xFF6366F1), Modifier.weight(1f))
                    LogisticsMetricCard("In Transit", "${uiState.kpis.inTransitCount}", Color(0xFFF59E0B), Modifier.weight(1f))
                }
            }

            // Big Action Buttons for Gate IN / OUT
            item {
                Text("Gate Operations", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GateActionLargeButton(
                        title = "Trolley IN",
                        subtitle = "Dock Receiving",
                        icon = Icons.Default.Login,
                        containerColor = Color(0xFF10B981),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.setMovementFormType("IN")
                            onNavigateToRoute(Screen.LogisticsMovement.createRoute("IN"))
                        }
                    )
                    GateActionLargeButton(
                        title = "Trolley OUT",
                        subtitle = "Dock Dispatch",
                        icon = Icons.Default.Logout,
                        containerColor = Color(0xFF0284C7),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.setMovementFormType("OUT")
                            onNavigateToRoute(Screen.LogisticsMovement.createRoute("OUT"))
                        }
                    )
                }
            }

            // Quick Tools Bar
            item {
                Text("Operational Tools", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickToolCard(
                        title = "Scan Barcode",
                        icon = Icons.Default.QrCodeScanner,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToRoute(Screen.CustomerScan.route) }
                    )
                    QuickToolCard(
                        title = "Movements Log",
                        icon = Icons.Default.History,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToRoute(Screen.LogisticsHistory.route) }
                    )
                    QuickToolCard(
                        title = "Trolley 360°",
                        icon = Icons.Default.Refresh,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToRoute(Screen.CustomerTrolley360.createRoute("TRL-2024-001")) }
                    )
                }
            }

            // Recent Movements Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Recent Gate Activity", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    TextButton(onClick = { onNavigateToRoute(Screen.LogisticsHistory.route) }) {
                        Text("View Full Log")
                    }
                }
            }

            if (uiState.movements.isEmpty()) {
                item {
                    EmptyStateView("No Recent Gate Activity", "Recorded dock movements will appear here.")
                }
            } else {
                items(uiState.movements.take(5), key = { it.id }) { mov ->
                    MovementItemRow(record = mov)
                }
            }
        }
    }
}

@Composable
fun LogisticsMetricCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = color)
            Text(title, style = MaterialTheme.typography.labelSmall, maxLines = 1)
        }
    }
}

@Composable
fun GateActionLargeButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun QuickToolCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, maxLines = 1)
        }
    }
}

@Composable
fun MovementItemRow(record: MovementRecordDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val (icon, color, bg) = if (record.movementType == "IN") {
                Triple(Icons.Default.Login, Color(0xFF10B981), Color(0xFFD1FAE5))
            } else {
                Triple(Icons.Default.Logout, Color(0xFF0284C7), Color(0xFFE0F2FE))
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(bg),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("${record.trolleyNumber}  •  Ref: ${record.referenceDocNumber}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text("Truck: ${record.carrierTruck}  •  Driver: ${record.driverName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${record.sourceLocation} ➔ ${record.destinationLocation}  (${record.timestamp})", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
