package com.autologix.tms.data.models

data class PmScheduleDto(
    val id: String,
    val trolleyId: String,
    val trolleyNumber: String,
    val trolleyType: String,
    val dueDate: String,
    val status: String, // SCHEDULED, OVERDUE, COMPLETED, IN_PROGRESS
    val frequencyDays: Int = 30,
    val assignedTechnicianId: String? = null,
    val assignedTechnicianName: String? = null,
    val checklistTemplateId: String? = null,
    val priority: String = "NORMAL"
)

data class TechnicianDto(
    val id: String,
    val name: String,
    val email: String,
    val phone: String? = null,
    val activeWorkOrdersCount: Int = 0,
    val isAvailable: Boolean = true,
    val skillLevel: String = "LEVEL_2"
)

data class ChecklistItemDto(
    val id: String,
    val section: String,
    val title: String,
    val criteria: String,
    val isMandatory: Boolean = true,
    val requiresPhoto: Boolean = false,
    var status: String = "PENDING", // PASS, FAIL, FLAGGED, PENDING
    var remarks: String = "",
    var photoUrl: String? = null
)

data class SparePartItemDto(
    val partId: String,
    val partNumber: String,
    val partName: String,
    val quantity: Int = 1,
    val unit: String = "PCS"
)

data class WorkOrderDto(
    val id: String,
    val workOrderNumber: String,
    val trolleyId: String,
    val trolleyNumber: String,
    val trolleyType: String,
    val type: String, // PM, DAMAGE_REPAIR, REWORK
    val status: String, // ASSIGNED, IN_PROGRESS, IN_REVIEW, COMPLETED, REWORK_REQUESTED
    val priority: String, // CRITICAL, HIGH, MEDIUM, LOW
    val targetDate: String,
    val assignedTechnicianId: String?,
    val assignedTechnicianName: String?,
    val damageReportId: String? = null,
    val damageDescription: String? = null,
    val managerNotes: String? = null,
    val technicianRemarks: String? = null,
    val reworkReason: String? = null,
    val beforePhotoUrls: List<String> = emptyList(),
    val afterPhotoUrls: List<String> = emptyList(),
    val partsReplaced: List<SparePartItemDto> = emptyList(),
    val checklistItems: List<ChecklistItemDto> = emptyList(),
    val createdAt: String,
    val completedAt: String? = null
)

data class CreateWorkOrderRequestDto(
    val trolleyId: String,
    val type: String, // PM or DAMAGE_REPAIR
    val priority: String,
    val assignedTechnicianId: String,
    val targetDate: String,
    val damageReportId: String? = null,
    val managerNotes: String? = null
)

data class WorkOrderActionRequestDto(
    val action: String, // START, PAUSE, SUBMIT_REVIEW, COMPLETE
    val technicianRemarks: String? = null,
    val beforePhotoUrls: List<String> = emptyList(),
    val afterPhotoUrls: List<String> = emptyList(),
    val partsReplaced: List<SparePartItemDto> = emptyList(),
    val checklistResults: List<ChecklistItemDto> = emptyList()
)

data class ManagerReviewRequestDto(
    val approved: Boolean,
    val reviewNotes: String? = null,
    val reworkReason: String? = null,
    val reassignTechnicianId: String? = null
)

data class MaintenanceManagerKpisDto(
    val weeklyPmTotal: Int = 42,
    val pmDueCount: Int = 14,
    val pmOverdueCount: Int = 3,
    val customerDamageRequestsCount: Int = 8,
    val pendingReviewCount: Int = 5,
    val activeTechniciansCount: Int = 6,
    val pmCompliancePercent: Double = 96.4
)
