package com.autologix.tms.core.constants

object ApiEndpoints {
    // Auth & Identity
    const val AUTH_LOGIN = "api/v1/auth/login"
    const val AUTH_REFRESH = "api/v1/auth/refresh"
    const val AUTH_LOGOUT = "api/v1/auth/logout"
    const val AUTH_ME = "api/v1/users/me"

    // Organizations
    const val ORGANIZATIONS = "api/v1/organizations"
    const val ORGANIZATIONS_SWITCH = "api/v1/organizations/switch"

    // Fleet & Trolleys
    const val TROLLEYS = "api/v1/trolleys"
    const val TROLLEY_DETAIL = "api/v1/trolleys/{id}"
    const val TROLLEY_360 = "api/v1/trolleys/{id}/360"
    const val TROLLEY_SCAN = "api/v1/trolleys/scan/{barcode}"

    // Maintenance & PM
    const val PM_SCHEDULES = "api/v1/pm/schedules"
    const val PM_EXECUTIONS = "api/v1/pm/executions/{id}"
    const val PM_CHECKLIST_TEMPLATES = "api/v1/pm/checklists/templates/{typeId}"
    const val WORK_ORDERS = "api/v1/maintenance/work-orders"
    const val WORK_ORDERS_MY = "api/v1/maintenance/work-orders/my"
    const val WORK_ORDER_ACTION = "api/v1/maintenance/work-orders/{id}/action"
    const val TECHNICIANS = "api/v1/users/technicians"

    // Logistics & Gate
    const val GATE_IN = "api/v1/logistics/movement/gate-in"
    const val GATE_OUT = "api/v1/logistics/movement/gate-out"
    const val MOVEMENT_HISTORY = "api/v1/logistics/movement/history"
    const val MOVEMENT_BATCH = "api/v1/logistics/movement/batch"

    // Damages & Incident Reports
    const val DAMAGES = "api/v1/damages"
    const val DAMAGE_DETAIL = "api/v1/damages/{id}"
    const val DAMAGE_STATUS_UPDATE = "api/v1/damages/{id}/status"

    // Media Upload
    const val MEDIA_UPLOAD = "api/v1/media/upload"

    // Dashboard & Reports
    const val DASHBOARD_KPIS = "api/v1/dashboard/kpis"
    const val DASHBOARD_FLEET_UTILIZATION = "api/v1/dashboard/fleet-utilization"

    // Notifications
    const val NOTIFICATIONS = "api/v1/notifications"
    const val NOTIFICATION_MARK_READ = "api/v1/notifications/{id}/read"
}
