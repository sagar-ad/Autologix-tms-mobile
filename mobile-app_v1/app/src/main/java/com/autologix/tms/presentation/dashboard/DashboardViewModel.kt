package com.autologix.tms.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autologix.tms.core.network.ApiConfig
import com.autologix.tms.core.security.SessionManager
import com.autologix.tms.domain.models.UserSession
import com.autologix.tms.domain.repositories.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardUiState(
    val session: UserSession? = null,
    val activeBaseUrl: String = "",
    val activeEnvironmentName: String = "",
    val isLoggingOut: Boolean = false,
    val selectedFilter: String = "ALL",
    val isRefreshing: Boolean = false
)

class DashboardViewModel(
    private val sessionManager: SessionManager,
    private val authRepository: AuthRepository,
    private val apiConfig: ApiConfig
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        DashboardUiState(
            session = sessionManager.sessionState.value,
            activeBaseUrl = apiConfig.getBaseUrl(),
            activeEnvironmentName = apiConfig.currentEnvironment.value.displayName
        )
    )
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionManager.sessionState.collect { session ->
                _uiState.update {
                    it.copy(
                        session = session,
                        activeBaseUrl = apiConfig.getBaseUrl(),
                        activeEnvironmentName = apiConfig.currentEnvironment.value.displayName
                    )
                }
            }
        }
    }

    fun onFilterSelected(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun logout(onLoggedOut: () -> Unit) {
        _uiState.update { it.copy(isLoggingOut = true) }
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update { it.copy(isLoggingOut = false) }
            onLoggedOut()
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        viewModelScope.launch {
            kotlinx.coroutines.delay(600)
            _uiState.update {
                it.copy(
                    isRefreshing = false,
                    activeBaseUrl = apiConfig.getBaseUrl(),
                    activeEnvironmentName = apiConfig.currentEnvironment.value.displayName
                )
            }
        }
    }
}
