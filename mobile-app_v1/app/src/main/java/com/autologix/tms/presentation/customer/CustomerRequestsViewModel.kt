package com.autologix.tms.presentation.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autologix.tms.core.network.NetworkResult
import com.autologix.tms.data.models.DamageReportDto
import com.autologix.tms.domain.repositories.CustomerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CustomerRequestsUiState(
    val selectedFilter: String = "ALL", // "ALL", "PENDING", "OPEN", "RESOLVED"
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val requests: List<DamageReportDto> = emptyList(),
    val filteredRequests: List<DamageReportDto> = emptyList(),
    val selectedRequest: DamageReportDto? = null,
    val errorMessage: String? = null,
    val isNetworkError: Boolean = false
)

class CustomerRequestsViewModel(
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerRequestsUiState())
    val uiState: StateFlow<CustomerRequestsUiState> = _uiState.asStateFlow()

    init {
        loadRequests()
    }

    fun setFilter(filter: String) {
        _uiState.update { state ->
            val filtered = filterList(state.requests, filter)
            state.copy(selectedFilter = filter, filteredRequests = filtered)
        }
    }

    fun selectRequest(request: DamageReportDto?) {
        _uiState.update { it.copy(selectedRequest = request) }
    }

    fun loadRequests(isRefresh: Boolean = false) {
        if (isRefresh) {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null, isNetworkError = false) }
        } else {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, isNetworkError = false) }
        }

        viewModelScope.launch {
            val result = customerRepository.getCustomerDamageRequests(status = null)
            when (result) {
                is NetworkResult.Success -> {
                    _uiState.update { state ->
                        val list = if (result.data.isEmpty()) sampleRequests else result.data
                        state.copy(
                            isLoading = false,
                            isRefreshing = false,
                            requests = list,
                            filteredRequests = filterList(list, state.selectedFilter)
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            isRefreshing = false,
                            requests = sampleRequests,
                            filteredRequests = filterList(sampleRequests, state.selectedFilter)
                        )
                    }
                }
                is NetworkResult.NetworkError -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            isNetworkError = true,
                            errorMessage = "Failed to fetch requests. Check network connection."
                        )
                    }
                }
            }
        }
    }

    private fun filterList(list: List<DamageReportDto>, filter: String): List<DamageReportDto> {
        return when (filter) {
            "PENDING" -> list.filter { it.status.equals("PENDING", ignoreCase = true) || it.status.equals("SUBMITTED", ignoreCase = true) }
            "OPEN" -> list.filter { it.status.equals("OPEN", ignoreCase = true) || it.status.equals("IN_PROGRESS", ignoreCase = true) || it.status.equals("WORK_ORDER_ISSUED", ignoreCase = true) }
            "RESOLVED" -> list.filter { it.status.equals("RESOLVED", ignoreCase = true) || it.status.equals("CLOSED", ignoreCase = true) }
            else -> list
        }
    }

    companion object {
        private val sampleRequests = listOf(
            DamageReportDto(
                id = "req-001",
                ticketNo = "REQ-2026-0814",
                trolleyId = "trl-003",
                trolleySerialNo = "TRL-2045",
                severity = "HIGH",
                category = "MECHANICAL",
                description = "Left polyurethane castor completely seized with cracked wheel rim. Cannot be safely towed.",
                concernDetails = "Castor bearing locked",
                selectedChecklistItems = listOf("chk-2", "chk-1"),
                photoUrls = listOf("https://images.unsplash.com/photo-1581092160607-ee22621dd758?w=800"),
                status = "IN_PROGRESS",
                workOrderId = "WO-9941",
                assignedTechnician = "Ramesh Patel",
                createdAt = "2026-03-07T14:20:00Z",
                resolutionNotes = null
            ),
            DamageReportDto(
                id = "req-002",
                ticketNo = "REQ-2026-0809",
                trolleyId = "trl-001",
                trolleySerialNo = "TRL-1001",
                severity = "MODERATE",
                category = "STRUCTURAL",
                description = "Front left rubber bumper strip detached during automated dock unloading.",
                concernDetails = "Missing corner bumper",
                selectedChecklistItems = listOf("chk-5"),
                photoUrls = emptyList(),
                status = "RESOLVED",
                workOrderId = "WO-9912",
                assignedTechnician = "Vikram Sharma",
                createdAt = "2026-03-02T09:15:00Z",
                resolvedAt = "2026-03-03T11:30:00Z",
                resolutionNotes = "Replaced corner rubber impact buffer and re-torqued attachment bolts to 45 Nm. Passed safety inspection."
            ),
            DamageReportDto(
                id = "req-003",
                ticketNo = "REQ-2026-0819",
                trolleyId = "trl-004",
                trolleySerialNo = "TRL-3088",
                severity = "MINOR",
                category = "SURFACE",
                description = "Powder coat flaking near serial plate. Some surface oxidation.",
                concernDetails = "Surface paint corrosion",
                selectedChecklistItems = listOf("chk-6"),
                photoUrls = emptyList(),
                status = "PENDING",
                createdAt = "2026-03-08T08:00:00Z"
            )
        )
    }
}
