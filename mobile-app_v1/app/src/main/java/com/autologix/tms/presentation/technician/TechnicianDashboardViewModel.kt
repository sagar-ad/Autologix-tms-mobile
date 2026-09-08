package com.autologix.tms.presentation.technician

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autologix.tms.core.network.NetworkResult
import com.autologix.tms.core.security.SessionManager
import com.autologix.tms.data.models.ChecklistItemDto
import com.autologix.tms.data.models.SparePartItemDto
import com.autologix.tms.data.models.WorkOrderActionRequestDto
import com.autologix.tms.data.models.WorkOrderDto
import com.autologix.tms.domain.repositories.AuthRepository
import com.autologix.tms.domain.repositories.MaintenanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TechnicianDashboardUiState(
    val isLoading: Boolean = false,
    val selectedFilter: String = "ALL", // ALL, ASSIGNED, IN_PROGRESS, REWORK
    val workOrders: List<WorkOrderDto> = defaultWorkOrders,
    val activeExecutionOrder: WorkOrderDto? = null,
    val checklist: List<ChecklistItemDto> = defaultChecklist,
    val partsReplaced: List<SparePartItemDto> = emptyList(),
    val remarks: String = "",
    val beforePhotoUrls: List<String> = emptyList(),
    val afterPhotoUrls: List<String> = emptyList(),
    val isUploadingPhoto: Boolean = false,
    val actionMessage: String? = null,
    val errorMessage: String? = null
) {
    val filteredOrders: List<WorkOrderDto>
        get() = when (selectedFilter) {
            "ASSIGNED" -> workOrders.filter { it.status == "ASSIGNED" }
            "IN_PROGRESS" -> workOrders.filter { it.status == "IN_PROGRESS" }
            "REWORK" -> workOrders.filter { it.status == "REWORK_REQUESTED" }
            else -> workOrders
        }

    val activeCount: Int get() = workOrders.count { it.status == "IN_PROGRESS" }
    val assignedCount: Int get() = workOrders.count { it.status == "ASSIGNED" }
    val reworkCount: Int get() = workOrders.count { it.status == "REWORK_REQUESTED" }

    companion object {
        val defaultChecklist = listOf(
            ChecklistItemDto("CHK-1", "Wheel Assembly", "Check castor swivel bearing and wheel tread wear", criteria = "Tread depth > 3mm, smooth 360 rotation"),
            ChecklistItemDto("CHK-2", "Wheel Assembly", "Inspect brake locking mechanism and pedal engagement", criteria = "Positive lock under full load"),
            ChecklistItemDto("CHK-3", "Towing System", "Inspect tow bar pin, coupler eye, and latch safety spring", criteria = "No deformation, pin securely latched"),
            ChecklistItemDto("CHK-4", "Structural Frame", "Inspect chassis welds, crossbars, and shelf alignment", criteria = "No crack or bend in structural steel"),
            ChecklistItemDto("CHK-5", "Identification & Safety", "Verify Barcode/QR tag readability and reflective warning strips", criteria = "Clean, scannable, reflective intact")
        )

        val defaultWorkOrders = listOf(
            WorkOrderDto(
                id = "WO-101",
                workOrderNumber = "WO-2026-0045",
                trolleyId = "TRL-2024-001",
                trolleyNumber = "TRL-2024-001",
                trolleyType = "Standard Shelf",
                type = "PM",
                status = "IN_PROGRESS",
                priority = "HIGH",
                targetDate = "Today 16:00",
                assignedTechnicianId = "ME",
                assignedTechnicianName = "Technician",
                managerNotes = "Scheduled 30-day preventive maintenance routine.",
                checklistItems = defaultChecklist,
                createdAt = "2026-03-08 08:30"
            ),
            WorkOrderDto(
                id = "WO-102",
                workOrderNumber = "WO-2026-0046",
                trolleyId = "TRL-2024-042",
                trolleyNumber = "TRL-2024-042",
                trolleyType = "Heavy Duty Tow",
                type = "DAMAGE_REPAIR",
                status = "REWORK_REQUESTED",
                priority = "CRITICAL",
                targetDate = "Today 18:00",
                assignedTechnicianId = "ME",
                assignedTechnicianName = "Technician",
                damageDescription = "Left rear castor locked, severe vibration during towing",
                reworkReason = "Manager Inspection: Tow pin grease insufficient and after photo unclear. Please re-lubricate and re-upload clear photo.",
                createdAt = "2026-03-07 14:00"
            ),
            WorkOrderDto(
                id = "WO-103",
                workOrderNumber = "WO-2026-0047",
                trolleyId = "TRL-2024-089",
                trolleyNumber = "TRL-2024-089",
                trolleyType = "Custom Bin",
                type = "PM",
                status = "ASSIGNED",
                priority = "NORMAL",
                targetDate = "Tomorrow 12:00",
                assignedTechnicianId = "ME",
                assignedTechnicianName = "Technician",
                managerNotes = "Perform standard PM cycle.",
                createdAt = "2026-03-08 10:00"
            )
        )
    }
}

class TechnicianDashboardViewModel(
    private val sessionManager: SessionManager,
    private val authRepository: AuthRepository,
    private val maintenanceRepository: MaintenanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TechnicianDashboardUiState())
    val uiState: StateFlow<TechnicianDashboardUiState> = _uiState.asStateFlow()

    init {
        loadAssignedWorkOrders()
    }

    fun setFilter(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun loadAssignedWorkOrders() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = maintenanceRepository.getMyWorkOrders()) {
                is NetworkResult.Success -> {
                    if (result.data.isNotEmpty()) {
                        _uiState.update { it.copy(workOrders = result.data, isLoading = false) }
                    } else {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
                else -> {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun openWorkOrderExecution(orderId: String) {
        val order = _uiState.value.workOrders.find { it.id == orderId }
        _uiState.update {
            it.copy(
                activeExecutionOrder = order,
                checklist = order?.checklistItems?.ifEmpty { TechnicianDashboardUiState.defaultChecklist } ?: TechnicianDashboardUiState.defaultChecklist,
                partsReplaced = order?.partsReplaced ?: emptyList(),
                remarks = order?.technicianRemarks ?: "",
                beforePhotoUrls = order?.beforePhotoUrls ?: emptyList(),
                afterPhotoUrls = order?.afterPhotoUrls ?: emptyList()
            )
        }
    }

    fun updateChecklistItemStatus(itemId: String, status: String) {
        _uiState.update { state ->
            val updated = state.checklist.map { item ->
                if (item.id == itemId) item.copy(status = status) else item
            }
            state.copy(checklist = updated)
        }
    }

    fun updateRemarks(remarks: String) {
        _uiState.update { it.copy(remarks = remarks) }
    }

    fun addSparePart(name: String, quantity: Int) {
        if (name.isBlank()) return
        val newPart = SparePartItemDto(
            partId = "SP-${System.currentTimeMillis() % 1000}",
            partNumber = "PN-${(1000..9999).random()}",
            partName = name,
            quantity = quantity
        )
        _uiState.update { it.copy(partsReplaced = it.partsReplaced + newPart) }
    }

    fun removeSparePart(partId: String) {
        _uiState.update { it.copy(partsReplaced = it.partsReplaced.filterNot { p -> p.partId == partId }) }
    }

    fun uploadBeforePhotoMock() {
        val sampleUrl = "https://images.unsplash.com/photo-1581092160607-ee22621dd758?w=800&auto=format&fit=crop&q=60"
        _uiState.update { it.copy(beforePhotoUrls = it.beforePhotoUrls + sampleUrl) }
    }

    fun uploadAfterPhotoMock() {
        val sampleUrl = "https://images.unsplash.com/photo-1581092335397-9583fe92d232?w=800&auto=format&fit=crop&q=60"
        _uiState.update { it.copy(afterPhotoUrls = it.afterPhotoUrls + sampleUrl) }
    }

    fun startWorkOrder() {
        val order = _uiState.value.activeExecutionOrder ?: return
        viewModelScope.launch {
            val request = WorkOrderActionRequestDto(action = "START")
            maintenanceRepository.performWorkOrderAction(order.id, request)
            _uiState.update {
                it.copy(
                    activeExecutionOrder = order.copy(status = "IN_PROGRESS"),
                    actionMessage = "Work Order ${order.workOrderNumber} started."
                )
            }
            loadAssignedWorkOrders()
        }
    }

    fun submitForManagerReview() {
        val order = _uiState.value.activeExecutionOrder ?: return
        if (_uiState.value.remarks.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter technician completion remarks before submitting.") }
            return
        }

        viewModelScope.launch {
            val request = WorkOrderActionRequestDto(
                action = "SUBMIT_REVIEW",
                technicianRemarks = _uiState.value.remarks,
                beforePhotoUrls = _uiState.value.beforePhotoUrls,
                afterPhotoUrls = _uiState.value.afterPhotoUrls,
                partsReplaced = _uiState.value.partsReplaced,
                checklistResults = _uiState.value.checklist
            )
            maintenanceRepository.performWorkOrderAction(order.id, request)
            _uiState.update {
                it.copy(
                    actionMessage = "Work Order ${order.workOrderNumber} submitted for manager review!"
                )
            }
            loadAssignedWorkOrders()
        }
    }

    fun dismissActionMessage() {
        _uiState.update { it.copy(actionMessage = null, errorMessage = null) }
    }

    fun logout(onLogoutSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            sessionManager.clearSession()
            onLogoutSuccess()
        }
    }
}
