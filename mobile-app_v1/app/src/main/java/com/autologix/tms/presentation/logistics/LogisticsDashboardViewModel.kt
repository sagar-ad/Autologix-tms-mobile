package com.autologix.tms.presentation.logistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autologix.tms.core.network.NetworkResult
import com.autologix.tms.core.security.SessionManager
import com.autologix.tms.data.models.GateInRequestDto
import com.autologix.tms.data.models.GateOutRequestDto
import com.autologix.tms.data.models.LogisticsKpisDto
import com.autologix.tms.data.models.MovementRecordDto
import com.autologix.tms.domain.repositories.AuthRepository
import com.autologix.tms.domain.repositories.LogisticsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LogisticsUiState(
    val isLoading: Boolean = false,
    val kpis: LogisticsKpisDto = LogisticsKpisDto(),
    val movements: List<MovementRecordDto> = defaultMovements,
    val selectedMovementTypeFilter: String = "ALL", // ALL, IN, OUT
    // Form fields for Movement Screen
    val movementFormType: String = "IN", // IN, OUT
    val trolleyId: String = "TRL-2024-001",
    val trolleyNumber: String = "TRL-2024-001",
    val location: String = "Plant Alpha Dock 3",
    val referenceNumber: String = "GP-2026-9901",
    val truckNumber: String = "MH-12-AB-9876",
    val driverName: String = "Suresh Verma",
    val sealNumber: String = "SL-8849",
    val project: String = "Mahindra XUV700 Assembly",
    val customer: String = "Mahindra & Mahindra Ltd",
    val conditionStatus: String = "GOOD",
    val remarks: String = "",
    val photoUrls: List<String> = emptyList(),
    val isSubmitting: Boolean = false,
    val actionMessage: String? = null,
    val errorMessage: String? = null
) {
    val filteredMovements: List<MovementRecordDto>
        get() = when (selectedMovementTypeFilter) {
            "IN" -> movements.filter { it.movementType == "IN" }
            "OUT" -> movements.filter { it.movementType == "OUT" }
            else -> movements
        }

    companion object {
        val defaultMovements = listOf(
            MovementRecordDto(
                id = "MOV-1",
                trolleyId = "TRL-2024-001",
                trolleyNumber = "TRL-2024-001",
                movementType = "IN",
                sourceLocation = "Supplier Plant Pune",
                destinationLocation = "Main Staging Bay 4",
                referenceDocNumber = "GP-2026-8801",
                carrierTruck = "MH-12-QW-1234",
                driverName = "Rajesh Kumar",
                conditionStatus = "GOOD",
                operatorName = "Logistics Operator",
                timestamp = "Today 10:15 AM"
            ),
            MovementRecordDto(
                id = "MOV-2",
                trolleyId = "TRL-2024-042",
                trolleyNumber = "TRL-2024-042",
                movementType = "OUT",
                sourceLocation = "Finished Goods Dock",
                destinationLocation = "Customer Assembly Plant",
                referenceDocNumber = "DC-2026-4402",
                carrierTruck = "MH-14-GH-5566",
                driverName = "Manoj Singh",
                sealNumber = "SEAL-9081",
                project = "Mahindra XUV700",
                customer = "Mahindra Ltd",
                operatorName = "Logistics Operator",
                timestamp = "Today 08:45 AM"
            )
        )
    }
}

class LogisticsDashboardViewModel(
    private val sessionManager: SessionManager,
    private val authRepository: AuthRepository,
    private val logisticsRepository: LogisticsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogisticsUiState())
    val uiState: StateFlow<LogisticsUiState> = _uiState.asStateFlow()

    init {
        loadLogisticsData()
    }

    fun setFilter(filter: String) {
        _uiState.update { it.copy(selectedMovementTypeFilter = filter) }
    }

    fun setMovementFormType(type: String) {
        _uiState.update {
            it.copy(
                movementFormType = type,
                referenceNumber = if (type == "IN") "GP-${System.currentTimeMillis() % 10000}" else "DC-${System.currentTimeMillis() % 10000}"
            )
        }
    }

    fun setFormTrolley(trolleyId: String) {
        _uiState.update { it.copy(trolleyId = trolleyId, trolleyNumber = trolleyId) }
    }

    fun updateFormField(
        location: String? = null,
        referenceNumber: String? = null,
        truckNumber: String? = null,
        driverName: String? = null,
        sealNumber: String? = null,
        project: String? = null,
        customer: String? = null,
        conditionStatus: String? = null,
        remarks: String? = null
    ) {
        _uiState.update { state ->
            state.copy(
                location = location ?: state.location,
                referenceNumber = referenceNumber ?: state.referenceNumber,
                truckNumber = truckNumber ?: state.truckNumber,
                driverName = driverName ?: state.driverName,
                sealNumber = sealNumber ?: state.sealNumber,
                project = project ?: state.project,
                customer = customer ?: state.customer,
                conditionStatus = conditionStatus ?: state.conditionStatus,
                remarks = remarks ?: state.remarks
            )
        }
    }

    fun addMockPhoto() {
        val sampleUrl = "https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?w=800&auto=format&fit=crop&q=60"
        _uiState.update { it.copy(photoUrls = it.photoUrls + sampleUrl) }
    }

    fun loadLogisticsData() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val kpisResult = logisticsRepository.getLogisticsKpis()) {
                is NetworkResult.Success -> _uiState.update { it.copy(kpis = kpisResult.data) }
                else -> {}
            }

            when (val histResult = logisticsRepository.getMovementHistory()) {
                is NetworkResult.Success -> {
                    if (histResult.data.isNotEmpty()) {
                        _uiState.update { it.copy(movements = histResult.data) }
                    }
                }
                else -> {}
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun submitMovement(onSuccess: () -> Unit) {
        val s = _uiState.value
        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }

        viewModelScope.launch {
            if (s.movementFormType == "IN") {
                val req = GateInRequestDto(
                    trolleyId = s.trolleyId,
                    fromLocation = s.location,
                    gatePassNumber = s.referenceNumber,
                    truckNumber = s.truckNumber,
                    driverName = s.driverName,
                    conditionStatus = s.conditionStatus,
                    project = s.project,
                    customer = s.customer,
                    remarks = s.remarks,
                    photoUrls = s.photoUrls
                )
                logisticsRepository.recordGateIn(req)
            } else {
                val req = GateOutRequestDto(
                    trolleyId = s.trolleyId,
                    toLocation = s.location,
                    dispatchNumber = s.referenceNumber,
                    truckNumber = s.truckNumber,
                    driverName = s.driverName,
                    sealNumber = s.sealNumber,
                    project = s.project,
                    customer = s.customer,
                    remarks = s.remarks
                )
                logisticsRepository.recordGateOut(req)
            }

            val newRecord = MovementRecordDto(
                id = "MOV-${System.currentTimeMillis() % 1000}",
                trolleyId = s.trolleyId,
                trolleyNumber = s.trolleyNumber,
                movementType = s.movementFormType,
                sourceLocation = if (s.movementFormType == "IN") s.location else "Yard",
                destinationLocation = if (s.movementFormType == "OUT") s.location else "Yard",
                referenceDocNumber = s.referenceNumber,
                carrierTruck = s.truckNumber,
                driverName = s.driverName,
                sealNumber = s.sealNumber,
                project = s.project,
                customer = s.customer,
                conditionStatus = s.conditionStatus,
                remarks = s.remarks,
                operatorName = "Logistics Operator",
                timestamp = "Just now"
            )

            _uiState.update {
                it.copy(
                    isSubmitting = false,
                    movements = listOf(newRecord) + it.movements,
                    actionMessage = "Trolley ${s.trolleyNumber} Gate-${s.movementFormType} recorded successfully!"
                )
            }
            onSuccess()
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
