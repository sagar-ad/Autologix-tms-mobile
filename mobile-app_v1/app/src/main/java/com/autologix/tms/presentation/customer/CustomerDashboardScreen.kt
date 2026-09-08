package com.autologix.tms.presentation.customer

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autologix.tms.core.navigation.Screen
import com.autologix.tms.ui.components.LoadingIndicator
import com.autologix.tms.ui.components.NetworkErrorView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDashboardScreen(
    viewModel: CustomerDashboardViewModel,
    onNavigateToRoute: (String) -> Unit,
    onLogoutSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "AutoLogix Customer Portal",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = uiState.session?.activeOrganizationName ?: "Authorized Facility",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onNavigateToRoute(Screen.CustomerNotifications.route) }
                    ) {
                        BadgedBox(
                            badge = {
                                if (uiState.unreadNotificationCount > 0) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError
                                    ) {
                                        Text(uiState.unreadNotificationCount.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications"
                            )
                        }
                    }
                    IconButton(onClick = { viewModel.loadDashboardData(isRefresh = true) }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Dashboard"
                        )
                    }
                    IconButton(onClick = { viewModel.logout(onLogoutSuccess) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Sign Out"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                uiState.isLoading && !uiState.isRefreshing -> {
                    LoadingIndicator(message = "Loading Customer Fleet & KPIs...")
                }
                uiState.isNetworkError && uiState.errorMessage != null -> {
                    NetworkErrorView(
                        message = uiState.errorMessage ?: "Network connection error",
                        onRetry = { viewModel.loadDashboardData() }
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 1. Primary Hero Action: Large QR/Barcode Scan Button
                        item {
                            LargeScannerHeroCard(
                                onScanClick = { onNavigateToRoute(Screen.CustomerScan.route) }
                            )
                        }

                        // 2. Search Trolley Bar
                        item {
                            CustomerSearchCard(
                                query = uiState.searchQuery,
                                onQueryChange = { viewModel.onSearchQueryChanged(it) },
                                onSearchSubmit = {
                                    onNavigateToRoute(Screen.CustomerSearch.route)
                                }
                            )
                        }

                        // 3. Section Title: Dashboard Counters
                        item {
                            Text(
                                text = "Fleet & Request Status Counters",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        // 4. Six Dashboard Counters Grid
                        item {
                            DashboardCountersGrid(
                                kpis = uiState.kpis,
                                onFilterClick = { filter ->
                                    onNavigateToRoute(Screen.CustomerRequests.createRoute(filter))
                                },
                                onTrolleysClick = {
                                    onNavigateToRoute(Screen.CustomerSearch.route)
                                }
                            )
                        }

                        // 5. Customer Quick Action Modules
                        item {
                            Text(
                                text = "Customer Actions & Services",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        item {
                            CustomerActionCards(
                                onRaiseDamageClick = { onNavigateToRoute(Screen.CustomerRaiseDamage.createRoute()) },
                                onViewRequestsClick = { onNavigateToRoute(Screen.CustomerRequests.createRoute("ALL")) },
                                onViewReportsClick = { onNavigateToRoute(Screen.CustomerReports.route) },
                                onViewPmClick = { onNavigateToRoute(Screen.CustomerSearch.route) }
                            )
                        }

                        // Security Governance Badge
                        item {
                            CustomerSecurityFooter(
                                customerRole = uiState.session?.role ?: "CUSTOMER_USER",
                                orgName = uiState.session?.activeOrganizationName ?: "AutoLogix Multi-Tenant"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LargeScannerHeroCard(
    onScanClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onScanClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "SCAN TROLLEY QR / BARCODE",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Instant 360° & Details",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Scan hardware code to inspect specs, PM history, or raise damage reports instantly.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                    )
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = "Scan Trolley",
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun CustomerSearchCard(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchSubmit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        "Search by trolley number or serial...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )
            Button(
                onClick = onSearchSubmit,
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Search", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun DashboardCountersGrid(
    kpis: com.autologix.tms.data.models.CustomerDashboardKpiDto,
    onFilterClick: (String) -> Unit,
    onTrolleysClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Row 1: Fleet counters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CounterCard(
                title = "Total Trolleys",
                value = kpis.totalTrolleys.toString(),
                icon = Icons.Default.ShoppingCart,
                accentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
                onClick = onTrolleysClick
            )
            CounterCard(
                title = "Active Trolleys",
                value = kpis.activeTrolleys.toString(),
                icon = Icons.Default.CheckCircle,
                accentColor = Color(0xFF10B981),
                modifier = Modifier.weight(1f),
                onClick = onTrolleysClick
            )
        }

        // Row 2: Customer Raised Requests & Open Requests
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CounterCard(
                title = "Customer Raised",
                value = kpis.customerRaisedRequests.toString(),
                icon = Icons.Default.ReportProblem,
                accentColor = Color(0xFFF59E0B),
                modifier = Modifier.weight(1f),
                onClick = { onFilterClick("ALL") }
            )
            CounterCard(
                title = "Open Requests",
                value = kpis.openRequests.toString(),
                icon = Icons.Default.HourglassEmpty,
                accentColor = Color(0xFFEF4444),
                modifier = Modifier.weight(1f),
                onClick = { onFilterClick("OPEN") }
            )
        }

        // Row 3: Resolved & Pending Requests
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CounterCard(
                title = "Resolved Requests",
                value = kpis.resolvedRequests.toString(),
                icon = Icons.Default.CheckCircle,
                accentColor = Color(0xFF10B981),
                modifier = Modifier.weight(1f),
                onClick = { onFilterClick("RESOLVED") }
            )
            CounterCard(
                title = "Pending Requests",
                value = kpis.pendingRequests.toString(),
                icon = Icons.Default.History,
                accentColor = Color(0xFF6366F1),
                modifier = Modifier.weight(1f),
                onClick = { onFilterClick("PENDING") }
            )
        }
    }
}

@Composable
private fun CounterCard(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CustomerActionCards(
    onRaiseDamageClick: () -> Unit,
    onViewRequestsClick: () -> Unit,
    onViewReportsClick: () -> Unit,
    onViewPmClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ActionCardItem(
            title = "Raise Damage / Incident Request",
            subtitle = "Report defects with checklist, descriptions & photos",
            icon = Icons.Default.ReportProblem,
            accentColor = Color(0xFFEF4444),
            onClick = onRaiseDamageClick
        )
        ActionCardItem(
            title = "View Request Status & History",
            subtitle = "Track lifecycle: Review, Work Order, Repair, Resolved",
            icon = Icons.Default.History,
            accentColor = Color(0xFF6366F1),
            onClick = onViewRequestsClick
        )
        ActionCardItem(
            title = "Preventive Maintenance & Inspection",
            subtitle = "Check PM cycles, audit checklists & health score",
            icon = Icons.Default.Build,
            accentColor = Color(0xFF10B981),
            onClick = onViewPmClick
        )
        ActionCardItem(
            title = "Customer Analytics & Reports",
            subtitle = "Fleet utilization, incident turnaround & compliance",
            icon = Icons.Default.Assessment,
            accentColor = Color(0xFF0284C7),
            onClick = onViewReportsClick
        )
    }
}

@Composable
private fun ActionCardItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun CustomerSecurityFooter(
    customerRole: String,
    orgName: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "CUSTOMER SECURITY GUARANTEE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Restricted to $orgName data. Backend token authorization enforced.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = customerRole,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
