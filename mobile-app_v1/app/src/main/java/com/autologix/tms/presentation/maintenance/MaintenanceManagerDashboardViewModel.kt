package com.autologix.tms.presentation.maintenance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autologix.tms.core.network.NetworkResult
import com.autologix.tms.core.security.SessionManager
import com.autologix.tms.data.models.CreateWorkOrderRequestDto
import com.autologix.tms.data.models.DamageReportDto
import com.autologix.tms.data.models.MaintenanceManagerKpisDto
import com.autologix.tms.data.models.ManagerReviewRequestDto
import com.autologix.tms.data.models.PmScheduleDto
import com.autologix.tms.data.models.TechnicianDto
import com.autologix.tms.data.models.WorkOrderDto
import com.autologix.tms.domain.repositories.AuthRepository
import com.autologix.tms.domain.repositories.MaintenanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MaintenanceManagerUiState(
    val isLoading: Boolean = false,
    val selectedTab: Int = 0, // 0: Schedules/Due, 1: Damage Requests, 2: Technicians, 3: Completed for Review
    val kpis: MaintenanceManagerKpisDto = MaintenanceManagerKpisDto(),
    val pmSchedules: List<PmScheduleDto> = defaultPmSchedules,
    val damageRequests: List<DamageReportDto> = defaultDamageRequests,
    val workOrders: List<WorkOrderDto> = defaultWorkOrders,
    val technicians: List<TechnicianDto> = defaultTechnicians,
    val selectedWorkOrderForReview: WorkOrderDto? = null,
    val selectedWorkOrderForAssign: WorkOrderDto? = null,
    val reviewNotes: String = "",
    val reworkReason: String = "",
    val selectedTechnicianId: String? = null,
    val actionMessage: String? = null,
    val errorMessage: String? = null
) {
    companion object {
        val defaultPmSchedules = listOf(
            PmScheduleDto("PM-101", "TRL-2024-001", "TRL-2024-001", "Standard Shelf", "Today, 14:00", "OVERDUE", priority = "HIGH"),
            PmScheduleDto("PM-102", "TRL-2024-042", "TRL-2024-042", "Heavy Duty Tow", "Tomorrow, 10:00", "SCHEDULED", priority = "NORMAL"),
            PmScheduleDto("PM-103", "TRL-2024-089", "TRL-2024-089", "Custom Bin", "In 3 days", "SCHEDULED", priority = "LOW")
        )
        val defaultDamageRequests = listOf(
            DamageReportDto("DMG-901", "T-DMG-2026-001", "TRL-2024-042", "TRL-2024-042", "WHEELS_AND_CASTORS", "HIGH", "Left rear castor locked, severe vibration during towing", emptyList(), "OPEN", "2026-03-08"),
            DamageReportDto("DMG-902", "T-DMG-2026-002", "TRL-2024-015", "TRL-2024-015", "TOW_BAR_COUPLER", "CRITICAL", "Tow hitch pin deformed, risk of uncoupling", emptyList(), "IN_REVIEW", "2026-03-07")
        )
        val defaultWorkOrders = listOf(
            WorkOrderDto(
                id = "WO-501",
                workOrderNumber = "WO-2026-0089",
                trolleyId = "TRL-2024-001",
                trolleyNumber = "TRL-2024-001",
                trolleyType = "Standard Shelf",
                type = "PM",
                status = "IN_REVIEW",
                priority = "HIGH",
                targetDate = "2026-03-08",
                assignedTechnicianId = "TECH-01",
                assignedTechnicianName = "Vikram Sharma",
                technicianRemarks = "Replaced castor bearings, lubricated tow pin, torque verified.",
                createdAt = "2026-03-08 09:00"
            ),
            WorkOrderDto(
                id = "WO-502",
                workOrderNumber = "WO-2026-0090",
                trolleyId = "TRL-2024-042",
                trolleyNumber = "TRL-2024-042",
                trolleyType = "Heavy Tow",
                type = "DAMAGE_REPAIR",
                status = "ASSIGNED",
                priority = "CRITICAL",
                targetDate = "2026-03-09",
                assignedTechnicianId = "TECH-02",
                assignedTechnicianName = "Ramesh Patel",
                createdAt = "2026-03-08 11:30"
            )
        )
        val defaultTechnicians = listOf(
            TechnicianDto("TECH-01", "Vikram Sharma", "vikram.s@autologix.com", "+91 98765 43210", activeWorkOrdersCount = 2, isAvailable = true),
            TechnicianDto("TECH-02", "Ramesh Patel", "ramesh.p@autologix.com", "+91 98765 43211", activeWorkOrdersCount = 4, isAvailable = false),
            TechnicianDto("TECH-03", "Anil Kumar", "anil.k@autologix.com", "+91 98765 43212", activeWorkOrdersCount = 1, isAvailable = true)
        )
    }
}

class MaintenanceManagerDashboardViewModel(
    private val sessionManager: SessionManager,
    private val authRepository: AuthRepository,
    private val maintenanceRepository: MaintenanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MaintenanceManagerUiState())
    val uiState: StateFlow<MaintenanceManagerUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun loadDashboardData() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            // Fetch KPIs
            when (val kpisResult = maintenanceRepository.getMaintenanceKpis()) {
                is NetworkResult.Success -> _uiState.update { it.copy(kpis = kpisResult.data) }
                else -> {}
            }

            // Fetch PM schedules
            when (val schedulesResult = maintenanceRepository.getPmSchedules()) {
                is NetworkResult.Success -> _uiState.update { it.copy(pmSchedules = schedulesResult.data) }
                else -> {}
            }

            // Fetch damage requests
            when (val damagesResult = maintenanceRepository.getCustomerDamageRequests()) {
                is NetworkResult.Success -> _uiState.update { it.copy(damageRequests = damagesResult.data) }
                else -> {}
            }

            // Fetch work orders
            when (val ordersResult = maintenanceRepository.getAllWorkOrders()) {
                is NetworkResult.Success -> _uiState.update { it.copy(workOrders = ordersResult.data) }
                else -> {}
            }

            // Fetch technicians
            when (val techResult = maintenanceRepository.getTechnicians()) {
                is NetworkResult.Success -> _uiState.update { it.copy(technicians = techResult.data) }
                else -> {}
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun selectWorkOrderForReview(workOrder: WorkOrderDto?) {
        _uiState.update {
            it.copy(
                selectedWorkOrderForReview = workOrder,
                reviewNotes = "",
                reworkReason = ""
            )
        }
    }

    fun selectWorkOrderForAssign(workOrder: WorkOrderDto?) {
        _uiState.update {
            it.copy(
                selectedWorkOrderForAssign = workOrder,
                selectedTechnicianId = workOrder?.assignedTechnicianId
            )
        }
    }

    fun updateReviewNotes(notes: String) {
        _uiState.update { it.copy(reviewNotes = notes) }
    }

    fun updateReworkReason(reason: String) {
        _uiState.update { it.copy(reworkReason = reason) }
    }

    fun selectTechnician(technicianId: String) {
        _uiState.update { it.copy(selectedTechnicianId = technicianId) }
    }

    fun approveWorkOrder() {
        val wo = _uiState.value.selectedWorkOrderForReview ?: return
        viewModelScope.launch {
            val request = ManagerReviewRequestDto(approved = true, reviewNotes = _uiState.value.reviewNotes)
            maintenanceRepository.reviewWorkOrder(wo.id, request)
            _uiState.update {
                it.copy(
                    selectedWorkOrderForReview = null,
                    actionMessage = "Work Order ${wo.workOrderNumber} has been approved!"
                )
            }
            loadDashboardData()
        }
    }

    fun sendForRework() {
        val wo = _uiState.value.selectedWorkOrderForReview ?: return
        if (_uiState.value.reworkReason.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter reasons for rework.") }
            return
        }
        viewModelScope.launch {
            val request = ManagerReviewRequestDto(approved = false, reworkReason = _uiState.value.reworkReason)
            maintenanceRepository.reviewWorkOrder(wo.id, request)
            _uiState.update {
                it.copy(
                    selectedWorkOrderForReview = null,
                    actionMessage = "Work Order ${wo.workOrderNumber} sent back for rework."
                )
            }
            loadDashboardData()
        }
    }

    fun confirmAssignTechnician() {
        val wo = _uiState.value.selectedWorkOrderForAssign ?: return
        val techId = _uiState.value.selectedTechnicianId ?: return
        val tech = _uiState.value.technicians.find { it.id == techId }

        viewModelScope.launch {
            val request = CreateWorkOrderRequestDto(
                trolleyId = wo.trolleyId,
                type = wo.type,
                priority = wo.priority,
                assignedTechnicianId = techId,
                targetDate = wo.targetDate
            )
            maintenanceRepository.createOrAssignWorkOrder(request)
            _uiState.update {
                it.copy(
                    selectedWorkOrderForAssign = null,
                    actionMessage = "Work Order reassigned to ${tech?.name ?: "Technician"}."
                )
            }
            loadDashboardData()
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
