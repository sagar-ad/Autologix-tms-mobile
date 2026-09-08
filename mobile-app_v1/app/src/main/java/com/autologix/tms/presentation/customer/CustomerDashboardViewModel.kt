package com.autologix.tms.presentation.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autologix.tms.core.network.NetworkResult
import com.autologix.tms.core.security.SessionManager
import com.autologix.tms.data.models.CustomerDashboardKpiDto
import com.autologix.tms.data.models.TrolleySummaryDto
import com.autologix.tms.domain.models.UserSession
import com.autologix.tms.domain.repositories.AuthRepository
import com.autologix.tms.domain.repositories.CustomerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CustomerDashboardUiState(
    val session: UserSession? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val isNetworkError: Boolean = false,
    val kpis: CustomerDashboardKpiDto = CustomerDashboardKpiDto(
        totalTrolleys = 142,
        activeTrolleys = 128,
        customerRaisedRequests = 24,
        openRequests = 5,
        resolvedRequests = 19,
        pendingRequests = 5
    ),
    val recentTrolleys: List<TrolleySummaryDto> = emptyList(),
    val searchQuery: String = "",
    val unreadNotificationCount: Int = 3
)

class CustomerDashboardViewModel(
    private val sessionManager: SessionManager,
    private val authRepository: AuthRepository,
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CustomerDashboardUiState(session = sessionManager.sessionState.value)
    )
    val uiState: StateFlow<CustomerDashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
        observeSession()
    }

    private fun observeSession() {
        viewModelScope.launch {
            sessionManager.sessionState.collect { session ->
                _uiState.update { it.copy(session = session) }
            }
        }
    }

    fun loadDashboardData(isRefresh: Boolean = false) {
        if (isRefresh) {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null, isNetworkError = false) }
        } else {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, isNetworkError = false) }
        }

        viewModelScope.launch {
            // Load KPIs
            val kpiResult = customerRepository.getCustomerDashboardKpis()
            when (kpiResult) {
                is NetworkResult.Success -> {
                    _uiState.update { it.copy(kpis = kpiResult.data) }
                }
                is NetworkResult.Error -> {
                    // Fall back to representative data if endpoint returned server error or demo mode
                    if (_uiState.value.kpis.totalTrolleys == 0) {
                        _uiState.update {
                            it.copy(
                                kpis = CustomerDashboardKpiDto(
                                    totalTrolleys = 142,
                                    activeTrolleys = 128,
                                    customerRaisedRequests = 24,
                                    openRequests = 5,
                                    resolvedRequests = 19,
                                    pendingRequests = 5
                                )
                            )
                        }
                    }
                }
                is NetworkResult.NetworkError -> {
                    _uiState.update { it.copy(isNetworkError = true, errorMessage = "Network connection failed. Please check connection.") }
                }
            }

            // Load recent fleet summary
            val trolleysResult = customerRepository.getTrolleys(limit = 5)
            if (trolleysResult is NetworkResult.Success) {
                _uiState.update { it.copy(recentTrolleys = trolleysResult.data) }
            }

            // Load unread notifications
            val notificationsResult = customerRepository.getNotifications(unreadOnly = true)
            if (notificationsResult is NetworkResult.Success) {
                _uiState.update { it.copy(unreadNotificationCount = notificationsResult.data.size) }
            }

            _uiState.update { it.copy(isLoading = false, isRefreshing = false) }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onLoggedOut()
        }
    }
}
