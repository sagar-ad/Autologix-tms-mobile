package com.autologix.tms.data.models

import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Objects for the AutoLogix TMS Customer Mobile Module.
 * Directly mapped to existing NestJS backend contracts identified in docs/mobile-api-inventory.md.
 */

data class CustomerDashboardKpiDto(
    @SerializedName("totalTrolleys")
    val totalTrolleys: Int = 0,
    @SerializedName("activeTrolleys")
    val activeTrolleys: Int = 0,
    @SerializedName("customerRaisedRequests")
    val customerRaisedRequests: Int = 0,
    @SerializedName("openRequests")
    val openRequests: Int = 0,
    @SerializedName("resolvedRequests")
    val resolvedRequests: Int = 0,
    @SerializedName("pendingRequests")
    val pendingRequests: Int = 0
)

data class TrolleyQuickScanDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("serialNo")
    val serialNo: String,
    @SerializedName("qrCode")
    val qrCode: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("currentLocation")
    val currentLocation: String? = null,
    @SerializedName("isDamaged")
    val isDamaged: Boolean = false,
    @SerializedName("customerName")
    val customerName: String? = null
)

data class TrolleySummaryDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("serialNo")
    val serialNo: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("currentLocation")
    val currentLocation: String? = null,
    @SerializedName("isDamaged")
    val isDamaged: Boolean = false,
    @SerializedName("lastPmDate")
    val lastPmDate: String? = null,
    @SerializedName("nextPmDueDate")
    val nextPmDueDate: String? = null,
    @SerializedName("thumbnailUrl")
    val thumbnailUrl: String? = null
)

data class TrolleyDocumentDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("type")
    val type: String, // e.g., "CERTIFICATE", "MANUAL", "DRAWING", "INSPECTION_REPORT"
    @SerializedName("fileSize")
    val fileSize: String,
    @SerializedName("downloadUrl")
    val downloadUrl: String,
    @SerializedName("isRestricted")
    val isRestricted: Boolean = false,
    @SerializedName("uploadedAt")
    val uploadedAt: String? = null
)

data class TrolleyFullDetailDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("serialNo")
    val serialNo: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("currentLocation")
    val currentLocation: String? = null,
    @SerializedName("isDamaged")
    val isDamaged: Boolean = false,
    @SerializedName("manufacturer")
    val manufacturer: String? = null,
    @SerializedName("commissionDate")
    val commissionDate: String? = null,
    @SerializedName("tareWeightKg")
    val tareWeightKg: Double? = null,
    @SerializedName("maxLoadKg")
    val maxLoadKg: Double? = null,
    @SerializedName("dimensions")
    val dimensions: String? = null, // e.g. "1200 x 800 x 1450 mm"
    @SerializedName("imageUrls")
    val imageUrls: List<String> = emptyList(),
    @SerializedName("documents")
    val documents: List<TrolleyDocumentDto> = emptyList(),
    @SerializedName("specs")
    val specs: Map<String, String> = emptyMap(),
    @SerializedName("pmCycleDays")
    val pmCycleDays: Int = 90,
    @SerializedName("nextPmDueDate")
    val nextPmDueDate: String? = null,
    @SerializedName("healthScore")
    val healthScore: Int = 100
)

data class TrolleyHotspotDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("component")
    val component: String, // WHEELS, LOCK_PIN, TOW_BAR, BASE_PLATE, CORNER_BUMPER
    @SerializedName("description")
    val description: String,
    @SerializedName("status")
    val status: String // GOOD, ATTENTION_REQUIRED, DAMAGED
)

data class PmHistoryItemDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("executionDate")
    val executionDate: String,
    @SerializedName("technicianName")
    val technicianName: String,
    @SerializedName("status")
    val status: String, // COMPLETED, PASSED, DEFECTS_NOTED
    @SerializedName("checklistPassed")
    val checklistPassed: Int,
    @SerializedName("checklistTotal")
    val checklistTotal: Int,
    @SerializedName("remarks")
    val remarks: String? = null
)

data class MovementHistoryItemDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("timestamp")
    val timestamp: String,
    @SerializedName("movementType")
    val movementType: String, // GATE_IN, GATE_OUT, BAY_TRANSFER
    @SerializedName("sourceLocation")
    val sourceLocation: String? = null,
    @SerializedName("destinationLocation")
    val destinationLocation: String? = null,
    @SerializedName("gatePassNumber")
    val gatePassNumber: String? = null
)

data class Trolley360ViewDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("serialNo")
    val serialNo: String,
    @SerializedName("trolleyType")
    val trolleyType: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("angles360Images")
    val angles360Images: List<String> = emptyList(),
    @SerializedName("hotspots")
    val hotspots: List<TrolleyHotspotDto> = emptyList(),
    @SerializedName("specs")
    val specs: Map<String, String> = emptyMap(),
    @SerializedName("pmHistory")
    val pmHistory: List<PmHistoryItemDto> = emptyList(),
    @SerializedName("movementHistory")
    val movementHistory: List<MovementHistoryItemDto> = emptyList()
)

data class DamageChecklistItemDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("label")
    val label: String,
    @SerializedName("category")
    val category: String, // FRAME, WHEELS, COUPLER, LOCKS, SURFACE, SAFETY
    @SerializedName("isCritical")
    val isCritical: Boolean = false
)

data class DamageChecklistTemplateDto(
    @SerializedName("typeId")
    val typeId: String,
    @SerializedName("categories")
    val categories: List<String>,
    @SerializedName("items")
    val items: List<DamageChecklistItemDto>
)

data class ReportDamageRequestDto(
    @SerializedName("trolleyId")
    val trolleyId: String,
    @SerializedName("severity")
    val severity: String, // MINOR, MODERATE, MAJOR, CRITICAL
    @SerializedName("category")
    val category: String, // MECHANICAL, STRUCTURAL, ELECTRICAL, SURFACE
    @SerializedName("description")
    val description: String,
    @SerializedName("concernDetails")
    val concernDetails: String? = null,
    @SerializedName("selectedChecklistItems")
    val selectedChecklistItems: List<String> = emptyList(),
    @SerializedName("photoUrls")
    val photoUrls: List<String> = emptyList(),
    @SerializedName("reportedLocationId")
    val reportedLocationId: String? = null
)

data class DamageReportDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("ticketNo")
    val ticketNo: String,
    @SerializedName("trolleyId")
    val trolleyId: String,
    @SerializedName("trolleySerialNo")
    val trolleySerialNo: String,
    @SerializedName("severity")
    val severity: String,
    @SerializedName("category")
    val category: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("concernDetails")
    val concernDetails: String? = null,
    @SerializedName("selectedChecklistItems")
    val selectedChecklistItems: List<String> = emptyList(),
    @SerializedName("photoUrls")
    val photoUrls: List<String> = emptyList(),
    @SerializedName("status")
    val status: String, // OPEN, UNDER_REVIEW, WORK_ORDER_ASSIGNED, IN_REPAIR, RESOLVED, CLOSED
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String? = null,
    @SerializedName("resolutionNotes")
    val resolutionNotes: String? = null,
    @SerializedName("resolvedAt")
    val resolvedAt: String? = null,
    @SerializedName("assignedTechnician")
    val assignedTechnician: String? = null
)

data class MediaUploadResponseDto(
    @SerializedName("url")
    val url: String,
    @SerializedName("fileKey")
    val fileKey: String,
    @SerializedName("mimeType")
    val mimeType: String,
    @SerializedName("size")
    val size: Long
)

data class NotificationItemDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("type")
    val type: String, // REQUEST_STATUS_CHANGE, PM_DUE, FLEET_ALERT, SYSTEM
    @SerializedName("isRead")
    val isRead: Boolean = false,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("entityId")
    val entityId: String? = null,
    @SerializedName("entityType")
    val entityType: String? = null // DAMAGE_REQUEST, TROLLEY, PM_SCHEDULE
)

data class CustomerFleetReportDto(
    @SerializedName("utilizationRate")
    val utilizationRate: Double = 0.0,
    @SerializedName("totalActive")
    val totalActive: Int = 0,
    @SerializedName("totalInRepair")
    val totalInRepair: Int = 0,
    @SerializedName("avgTurnaroundDays")
    val avgTurnaroundDays: Double = 0.0,
    @SerializedName("incidentsThisMonth")
    val incidentsThisMonth: Int = 0,
    @SerializedName("pmComplianceRate")
    val pmComplianceRate: Double = 0.0,
    @SerializedName("statusBreakdown")
    val statusBreakdown: Map<String, Int> = emptyMap(),
    @SerializedName("monthlyTrend")
    val monthlyTrend: List<MonthlyMetricDto> = emptyList()
)

data class MonthlyMetricDto(
    @SerializedName("month")
    val month: String,
    @SerializedName("requestsCount")
    val requestsCount: Int,
    @SerializedName("resolvedCount")
    val resolvedCount: Int
)

data class PaginatedCustomerResponse<T>(
    @SerializedName("items")
    val items: List<T> = emptyList(),
    @SerializedName("total")
    val total: Int = 0,
    @SerializedName("page")
    val page: Int = 1,
    @SerializedName("totalPages")
    val totalPages: Int = 1
)
