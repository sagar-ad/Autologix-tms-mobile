package com.autologix.tms.core.navigation

sealed class Screen(val route: String, val title: String) {
    object Splash : Screen("splash", "AutoLogix TMS")
    object Login : Screen("login", "Sign In")
    object Dashboard : Screen("dashboard", "Dashboard")
    object Trolley : Screen("trolley", "Trolley Catalog")
    object Maintenance : Screen("maintenance", "Preventive Maintenance")
    object Damage : Screen("damage", "Damage & Incidents")
    object Logistics : Screen("logistics", "Logistics & Movements")
    object Reports : Screen("reports", "Reports & KPIs")
    object Notifications : Screen("notifications", "Notifications")
    object Profile : Screen("profile", "My Profile")

    // Customer Specific Routes
    object CustomerDashboard : Screen("customer_dashboard", "Customer Portal")
    object CustomerScan : Screen("customer_scan", "Scan Trolley QR/Barcode")
    object CustomerSearch : Screen("customer_search", "Search Trolleys")
    object CustomerTrolleyDetail : Screen("customer_trolley_detail/{trolleyId}", "Trolley Details") {
        fun createRoute(trolleyId: String): String = "customer_trolley_detail/$trolleyId"
    }
    object CustomerTrolley360 : Screen("customer_trolley_360/{trolleyId}", "Trolley 360° Inspection") {
        fun createRoute(trolleyId: String): String = "customer_trolley_360/$trolleyId"
    }
    object CustomerRaiseDamage : Screen("customer_raise_damage?trolleyId={trolleyId}", "Raise Damage Request") {
        fun createRoute(trolleyId: String? = null): String =
            if (!trolleyId.isNullOrBlank()) "customer_raise_damage?trolleyId=$trolleyId" else "customer_raise_damage"
    }
    object CustomerRequests : Screen("customer_requests?filter={filter}", "Damage Requests") {
        fun createRoute(filter: String = "ALL"): String = "customer_requests?filter=$filter"
    }
    object CustomerRequestDetail : Screen("customer_request_detail/{requestId}", "Request Details") {
        fun createRoute(requestId: String): String = "customer_request_detail/$requestId"
    }
    object CustomerReports : Screen("customer_reports", "Customer Fleet Reports")
    object CustomerNotifications : Screen("customer_notifications", "Customer Notifications")

    // Role-Based Dashboards
    object SuperAdminDashboard : Screen("super_admin_dashboard", "Super Admin Console")
    object MaintenanceManagerDashboard : Screen("maintenance_manager_dashboard", "Maintenance Manager Dashboard")
    object TechnicianDashboard : Screen("technician_dashboard", "Technician Workbench")
    object TechnicianWorkOrderExecution : Screen("technician_work_order/{workOrderId}", "Work Order Execution") {
        fun createRoute(workOrderId: String): String = "technician_work_order/$workOrderId"
    }
    object LogisticsDashboard : Screen("logistics_dashboard", "Logistics Operations")
    object LogisticsMovement : Screen("logistics_movement?type={type}&trolleyId={trolleyId}", "Trolley Movement") {
        fun createRoute(type: String = "IN", trolleyId: String? = null): String =
            if (!trolleyId.isNullOrBlank()) "logistics_movement?type=$type&trolleyId=$trolleyId"
            else "logistics_movement?type=$type"
    }
    object LogisticsHistory : Screen("logistics_history", "Movement History")
}
