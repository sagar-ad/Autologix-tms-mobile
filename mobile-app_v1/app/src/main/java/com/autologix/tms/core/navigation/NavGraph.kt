package com.autologix.tms.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.autologix.tms.core.di.AppContainer
import com.autologix.tms.presentation.admin.SuperAdminDashboardScreen
import com.autologix.tms.presentation.admin.SuperAdminDashboardViewModel
import com.autologix.tms.presentation.auth.LoginScreen
import com.autologix.tms.presentation.auth.LoginViewModel
import com.autologix.tms.presentation.auth.SplashScreen
import com.autologix.tms.presentation.customer.CustomerDashboardScreen
import com.autologix.tms.presentation.customer.CustomerDashboardViewModel
import com.autologix.tms.presentation.customer.CustomerNotificationsScreen
import com.autologix.tms.presentation.customer.CustomerNotificationsViewModel
import com.autologix.tms.presentation.customer.CustomerReportsScreen
import com.autologix.tms.presentation.customer.CustomerReportsViewModel
import com.autologix.tms.presentation.customer.CustomerRequestsScreen
import com.autologix.tms.presentation.customer.CustomerRequestsViewModel
import com.autologix.tms.presentation.customer.RaiseDamageScreen
import com.autologix.tms.presentation.customer.RaiseDamageViewModel
import com.autologix.tms.presentation.customer.Trolley360Screen
import com.autologix.tms.presentation.customer.TrolleyDetailScreen
import com.autologix.tms.presentation.customer.TrolleyDetailViewModel
import com.autologix.tms.presentation.customer.TrolleyScanScreen
import com.autologix.tms.presentation.customer.TrolleyScanViewModel
import com.autologix.tms.presentation.customer.TrolleySearchScreen
import com.autologix.tms.presentation.customer.TrolleySearchViewModel
import com.autologix.tms.presentation.damage.DamageScreen
import com.autologix.tms.presentation.dashboard.DashboardScreen
import com.autologix.tms.presentation.dashboard.DashboardViewModel
import com.autologix.tms.presentation.logistics.LogisticsDashboardScreen
import com.autologix.tms.presentation.logistics.LogisticsDashboardViewModel
import com.autologix.tms.presentation.logistics.LogisticsScreen
import com.autologix.tms.presentation.logistics.MovementHistoryScreen
import com.autologix.tms.presentation.logistics.TrolleyMovementScreen
import com.autologix.tms.presentation.maintenance.MaintenanceManagerDashboardScreen
import com.autologix.tms.presentation.maintenance.MaintenanceManagerDashboardViewModel
import com.autologix.tms.presentation.maintenance.MaintenanceScreen
import com.autologix.tms.presentation.notifications.NotificationsScreen
import com.autologix.tms.presentation.profile.ProfileScreen
import com.autologix.tms.presentation.reports.ReportsScreen
import com.autologix.tms.presentation.technician.TechnicianDashboardScreen
import com.autologix.tms.presentation.technician.TechnicianDashboardViewModel
import com.autologix.tms.presentation.technician.TechnicianWorkOrderExecutionScreen
import com.autologix.tms.presentation.trolley.TrolleyScreen

fun getDestinationForRole(role: String?): String {
    val r = role?.uppercase() ?: ""
    return when {
        r.contains("SUPER") || r.contains("ADMIN") -> Screen.SuperAdminDashboard.route
        r.contains("MANAGER") -> Screen.MaintenanceManagerDashboard.route
        r.contains("TECH") -> Screen.TechnicianDashboard.route
        r.contains("LOGISTICS") -> Screen.LogisticsDashboard.route
        r.contains("CUSTOMER") -> Screen.CustomerDashboard.route
        else -> Screen.Dashboard.route
    }
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    appContainer: AppContainer,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                sessionManager = appContainer.sessionManager,
                apiConfig = appContainer.apiConfig,
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    val session = appContainer.sessionManager.sessionState.value
                    val destination = getDestinationForRole(session?.role)
                    navController.navigate(destination) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            val loginViewModel = LoginViewModel(
                authRepository = appContainer.authRepository,
                sessionManager = appContainer.sessionManager,
                apiConfig = appContainer.apiConfig
            )
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    val session = appContainer.sessionManager.sessionState.value
                    val destination = getDestinationForRole(session?.role)
                    navController.navigate(destination) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            val dashboardViewModel = DashboardViewModel(
                sessionManager = appContainer.sessionManager,
                authRepository = appContainer.authRepository,
                apiConfig = appContainer.apiConfig
            )
            DashboardScreen(
                viewModel = dashboardViewModel,
                onNavigateToRoute = { route ->
                    navController.navigate(route)
                },
                onLogoutSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }

        // ==========================================
        // CUSTOMER TMS MOBILE MODULE SCREENS
        // ==========================================

        composable(Screen.CustomerDashboard.route) {
            val customerDashboardViewModel = CustomerDashboardViewModel(
                sessionManager = appContainer.sessionManager,
                authRepository = appContainer.authRepository,
                customerRepository = appContainer.customerRepository
            )
            CustomerDashboardScreen(
                viewModel = customerDashboardViewModel,
                onNavigateToRoute = { route ->
                    navController.navigate(route)
                },
                onLogoutSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.CustomerDashboard.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.CustomerScan.route) {
            val scanViewModel = TrolleyScanViewModel(
                customerRepository = appContainer.customerRepository
            )
            TrolleyScanScreen(
                viewModel = scanViewModel,
                onNavigateBack = { navController.popBackStack() },
                onViewTrolleyDetail = { trolleyId ->
                    navController.navigate(Screen.CustomerTrolleyDetail.createRoute(trolleyId))
                },
                onViewTrolley360 = { trolleyId ->
                    navController.navigate(Screen.CustomerTrolley360.createRoute(trolleyId))
                },
                onRaiseDamage = { trolleyId ->
                    navController.navigate(Screen.CustomerRaiseDamage.createRoute(trolleyId))
                }
            )
        }

        composable(Screen.CustomerSearch.route) {
            val searchViewModel = TrolleySearchViewModel(
                customerRepository = appContainer.customerRepository
            )
            TrolleySearchScreen(
                viewModel = searchViewModel,
                onNavigateBack = { navController.popBackStack() },
                onScanClick = { navController.navigate(Screen.CustomerScan.route) },
                onViewDetail = { trolleyId ->
                    navController.navigate(Screen.CustomerTrolleyDetail.createRoute(trolleyId))
                },
                onView360 = { trolleyId ->
                    navController.navigate(Screen.CustomerTrolley360.createRoute(trolleyId))
                },
                onRaiseDamage = { trolleyId ->
                    navController.navigate(Screen.CustomerRaiseDamage.createRoute(trolleyId))
                }
            )
        }

        composable(
            route = Screen.CustomerTrolleyDetail.route,
            arguments = listOf(navArgument("trolleyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val trolleyId = backStackEntry.arguments?.getString("trolleyId") ?: ""
            val detailViewModel = TrolleyDetailViewModel(
                customerRepository = appContainer.customerRepository
            )
            TrolleyDetailScreen(
                trolleyId = trolleyId,
                viewModel = detailViewModel,
                onNavigateBack = { navController.popBackStack() },
                onView360 = { id ->
                    navController.navigate(Screen.CustomerTrolley360.createRoute(id))
                },
                onRaiseDamage = { id ->
                    navController.navigate(Screen.CustomerRaiseDamage.createRoute(id))
                }
            )
        }

        composable(
            route = Screen.CustomerTrolley360.route,
            arguments = listOf(navArgument("trolleyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val trolleyId = backStackEntry.arguments?.getString("trolleyId") ?: ""
            val detailViewModel = TrolleyDetailViewModel(
                customerRepository = appContainer.customerRepository
            )
            Trolley360Screen(
                trolleyId = trolleyId,
                viewModel = detailViewModel,
                onNavigateBack = { navController.popBackStack() },
                onRaiseDamage = { id ->
                    navController.navigate(Screen.CustomerRaiseDamage.createRoute(id))
                }
            )
        }

        composable(
            route = Screen.CustomerRaiseDamage.route,
            arguments = listOf(
                navArgument("trolleyId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val trolleyId = backStackEntry.arguments?.getString("trolleyId")
            val raiseDamageViewModel = RaiseDamageViewModel(
                customerRepository = appContainer.customerRepository
            )
            RaiseDamageScreen(
                trolleyId = trolleyId,
                viewModel = raiseDamageViewModel,
                onNavigateBack = { navController.popBackStack() },
                onScanClick = { navController.navigate(Screen.CustomerScan.route) },
                onViewSubmittedRequest = { _ ->
                    navController.navigate(Screen.CustomerRequests.createRoute("ALL")) {
                        popUpTo(Screen.CustomerDashboard.route)
                    }
                }
            )
        }

        composable(
            route = Screen.CustomerRequests.route,
            arguments = listOf(
                navArgument("filter") {
                    type = NavType.StringType
                    defaultValue = "ALL"
                }
            )
        ) { backStackEntry ->
            val filter = backStackEntry.arguments?.getString("filter") ?: "ALL"
            val requestsViewModel = CustomerRequestsViewModel(
                customerRepository = appContainer.customerRepository
            )
            CustomerRequestsScreen(
                initialFilter = filter,
                viewModel = requestsViewModel,
                onNavigateBack = { navController.popBackStack() },
                onRaiseDamageClick = { navController.navigate(Screen.CustomerRaiseDamage.createRoute()) }
            )
        }

        composable(Screen.CustomerNotifications.route) {
            val notificationsViewModel = CustomerNotificationsViewModel(
                customerRepository = appContainer.customerRepository
            )
            CustomerNotificationsScreen(
                viewModel = notificationsViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToRequest = { _ ->
                    navController.navigate(Screen.CustomerRequests.createRoute("ALL"))
                }
            )
        }

        composable(Screen.CustomerReports.route) {
            val reportsViewModel = CustomerReportsViewModel(
                customerRepository = appContainer.customerRepository
            )
            CustomerReportsScreen(
                viewModel = reportsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ==========================================
        // OTHER TMS MODULES
        // ==========================================

        composable(Screen.Trolley.route) {
            TrolleyScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Maintenance.route) {
            MaintenanceScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Damage.route) {
            DamageScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Logistics.route) {
            LogisticsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Reports.route) {
            ReportsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Notifications.route) {
            NotificationsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                sessionManager = appContainer.sessionManager,
                authRepository = appContainer.authRepository,
                apiConfig = appContainer.apiConfig,
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }

        // ==========================================
        // SUPER ADMIN DASHBOARD
        // ==========================================
        composable(Screen.SuperAdminDashboard.route) {
            val adminViewModel = SuperAdminDashboardViewModel(
                sessionManager = appContainer.sessionManager,
                authRepository = appContainer.authRepository,
                adminRepository = appContainer.adminRepository
            )
            SuperAdminDashboardScreen(
                viewModel = adminViewModel,
                onLogoutSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.SuperAdminDashboard.route) { inclusive = true }
                    }
                }
            )
        }

        // ==========================================
        // MAINTENANCE MANAGER DASHBOARD
        // ==========================================
        composable(Screen.MaintenanceManagerDashboard.route) {
            val managerViewModel = MaintenanceManagerDashboardViewModel(
                sessionManager = appContainer.sessionManager,
                authRepository = appContainer.authRepository,
                maintenanceRepository = appContainer.maintenanceRepository
            )
            MaintenanceManagerDashboardScreen(
                viewModel = managerViewModel,
                onNavigateToRoute = { route -> navController.navigate(route) },
                onLogoutSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.MaintenanceManagerDashboard.route) { inclusive = true }
                    }
                }
            )
        }

        // ==========================================
        // MAINTENANCE TECHNICIAN WORKBENCH & EXECUTION
        // ==========================================
        composable(Screen.TechnicianDashboard.route) {
            val techViewModel = TechnicianDashboardViewModel(
                sessionManager = appContainer.sessionManager,
                authRepository = appContainer.authRepository,
                maintenanceRepository = appContainer.maintenanceRepository
            )
            TechnicianDashboardScreen(
                viewModel = techViewModel,
                onNavigateToRoute = { route -> navController.navigate(route) },
                onOpenWorkOrder = { woId ->
                    navController.navigate(Screen.TechnicianWorkOrderExecution.createRoute(woId))
                },
                onLogoutSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.TechnicianDashboard.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.TechnicianWorkOrderExecution.route,
            arguments = listOf(
                navArgument("workOrderId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val workOrderId = backStackEntry.arguments?.getString("workOrderId") ?: ""
            val techViewModel = TechnicianDashboardViewModel(
                sessionManager = appContainer.sessionManager,
                authRepository = appContainer.authRepository,
                maintenanceRepository = appContainer.maintenanceRepository
            )
            TechnicianWorkOrderExecutionScreen(
                workOrderId = workOrderId,
                viewModel = techViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ==========================================
        // LOGISTICS DASHBOARD & OPERATIONS
        // ==========================================
        composable(Screen.LogisticsDashboard.route) {
            val logisticsViewModel = LogisticsDashboardViewModel(
                sessionManager = appContainer.sessionManager,
                authRepository = appContainer.authRepository,
                logisticsRepository = appContainer.logisticsRepository
            )
            LogisticsDashboardScreen(
                viewModel = logisticsViewModel,
                onNavigateToRoute = { route -> navController.navigate(route) },
                onLogoutSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.LogisticsDashboard.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.LogisticsMovement.route,
            arguments = listOf(
                navArgument("type") {
                    type = NavType.StringType
                    defaultValue = "IN"
                },
                navArgument("trolleyId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: "IN"
            val trolleyId = backStackEntry.arguments?.getString("trolleyId")
            val logisticsViewModel = LogisticsDashboardViewModel(
                sessionManager = appContainer.sessionManager,
                authRepository = appContainer.authRepository,
                logisticsRepository = appContainer.logisticsRepository
            )
            TrolleyMovementScreen(
                initialType = type,
                initialTrolleyId = trolleyId,
                viewModel = logisticsViewModel,
                onNavigateBack = { navController.popBackStack() },
                onOpenScan = { navController.navigate(Screen.CustomerScan.route) }
            )
        }

        composable(Screen.LogisticsHistory.route) {
            val logisticsViewModel = LogisticsDashboardViewModel(
                sessionManager = appContainer.sessionManager,
                authRepository = appContainer.authRepository,
                logisticsRepository = appContainer.logisticsRepository
            )
            MovementHistoryScreen(
                viewModel = logisticsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
