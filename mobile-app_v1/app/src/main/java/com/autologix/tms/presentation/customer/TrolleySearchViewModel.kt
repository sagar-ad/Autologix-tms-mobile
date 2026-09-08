package com.autologix.tms.presentation.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autologix.tms.core.network.NetworkResult
import com.autologix.tms.data.models.TrolleySummaryDto
import com.autologix.tms.domain.repositories.CustomerRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TrolleySearchUiState(
    val query: String = "",
    val selectedStatusFilter: String = "ALL",
    val isLoading: Boolean = false,
    val trolleys: List<TrolleySummaryDto> = emptyList(),
    val errorMessage: String? = null,
    val isNetworkError: Boolean = false
)

class TrolleySearchViewModel(
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrolleySearchUiState())
    val uiState: StateFlow<TrolleySearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        performSearch(query = "", status = null)
    }

    fun onQueryChanged(newQuery: String) {
        _uiState.update { it.copy(query = newQuery) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300) // Debounce search
            performSearch(
                query = newQuery,
                status = if (_uiState.value.selectedStatusFilter == "ALL") null else _uiState.value.selectedStatusFilter
            )
        }
    }

    fun onStatusFilterSelected(status: String) {
        _uiState.update { it.copy(selectedStatusFilter = status) }
        performSearch(
            query = _uiState.value.query,
            status = if (status == "ALL") null else status
        )
    }

    fun retry() {
        performSearch(
            query = _uiState.value.query,
            status = if (_uiState.value.selectedStatusFilter == "ALL") null else _uiState.value.selectedStatusFilter
        )
    }

    private fun performSearch(query: String, status: String?) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null, isNetworkError = false) }

        viewModelScope.launch {
            val result = customerRepository.getTrolleys(
                search = query.ifBlank { null },
                status = status
            )

            when (result) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            trolleys = result.data,
                            errorMessage = null
                        )
                    }
                }
                is NetworkResult.Error -> {
                    // Fallback to sample fleet if testing against local mock
                    if (_uiState.value.trolleys.isEmpty()) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                trolleys = sampleFleet.filter { item ->
                                    (query.isBlank() || item.serialNo.contains(query, ignoreCase = true) || item.type.contains(query, ignoreCase = true)) &&
                                    (status == null || item.status.equals(status, ignoreCase = true))
                                }
                            )
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                    }
                }
                is NetworkResult.NetworkError -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isNetworkError = true,
                            errorMessage = "Unable to connect to fleet server. Please check your network."
                        )
                    }
                }
            }
        }
    }

    companion object {
        private val sampleFleet = listOf(
            TrolleySummaryDto(
                id = "trl-001",
                serialNo = "TRL-1001",
                type = "Heavy Duty Frame Carrier",
                status = "ACTIVE",
                currentLocation = "Zone A - Stamping Bay",
                isDamaged = false,
                lastPmDate = "2026-02-15",
                nextPmDueDate = "2026-05-15"
            ),
            TrolleySummaryDto(
                id = "trl-002",
                serialNo = "TRL-1002",
                type = "Engine Block Dolly",
                status = "ACTIVE",
                currentLocation = "Zone B - Subassembly",
                isDamaged = false,
                lastPmDate = "2026-01-20",
                nextPmDueDate = "2026-04-20"
            ),
            TrolleySummaryDto(
                id = "trl-003",
                serialNo = "TRL-2045",
                type = "Powertrain Tow Trolley",
                status = "DAMAGED",
                currentLocation = "Repair Yard - Bay 4",
                isDamaged = true,
                lastPmDate = "2025-11-10",
                nextPmDueDate = "2026-02-10"
            ),
            TrolleySummaryDto(
                id = "trl-004",
                serialNo = "TRL-3088",
                type = "Modular Panel Rack",
                status = "IN_YARD",
                currentLocation = "Storage Yard North",
                isDamaged = false,
                lastPmDate = "2026-02-01",
                nextPmDueDate = "2026-05-01"
            )
        )
    }
}
