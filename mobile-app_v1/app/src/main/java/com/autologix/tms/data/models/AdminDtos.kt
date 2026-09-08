package com.autologix.tms.data.models

data class SystemHealthDto(
    val status: String = "HEALTHY",
    val databaseStatus: String = "CONNECTED",
    val apiLatencyMs: Long = 42,
    val activeWebSocketConnections: Int = 18,
    val nestJsVersion: String = "v10.3.2",
    val serverUptimeHours: Long = 348
)

data class OrganizationDetailDto(
    val id: String,
    val name: String,
    val code: String,
    val status: String, // ACTIVE, SUSPENDED
    val trolleyCount: Int,
    val userCount: Int,
    val plantLocation: String,
    val createdAt: String
)

data class SuperAdminKpisDto(
    val totalOrganizations: Int = 4,
    val totalFleetAcrossOrgs: Int = 890,
    val totalRegisteredUsers: Int = 84,
    val pendingSupportRequests: Int = 2,
    val systemHealth: SystemHealthDto = SystemHealthDto()
)
