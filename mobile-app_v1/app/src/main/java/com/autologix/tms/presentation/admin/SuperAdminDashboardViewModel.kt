package com.autologix.tms.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autologix.tms.core.network.NetworkResult
import com.autologix.tms.core.security.SessionManager
import com.autologix.tms.data.models.OrganizationDetailDto
import com.autologix.tms.data.models.SuperAdminKpisDto
import com.autologix.tms.data.models.SystemHealthDto
import com.autologix.tms.domain.repositories.AdminRepository
import com.autologix.tms.domain.repositories.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SuperAdminUiState(
    val isLoading: Boolean = false,
    val kpis: SuperAdminKpisDto = SuperAdminKpisDto(),
    val organizations: List<OrganizationDetailDto> = defaultOrganizations,
    val systemHealth: SystemHealthDto = SystemHealthDto(),
    val actionMessage: String? = null,
    val errorMessage: String? = null
) {
    companion object {
        val defaultOrganizations = listOf(
            OrganizationDetailDto("ORG-01", "Mahindra & Mahindra Ltd", "M&M-PUNE", "ACTIVE", 420, 36, "Chakan Plant, Pune", "2024-01-15"),
            OrganizationDetailDto("ORG-02", "Tata Motors Commercial", "TATA-PMPR", "ACTIVE", 280, 24, "Pimpri Works, Pune", "2024-03-10"),
            OrganizationDetailDto("ORG-03", "Bajaj Auto Manufacturing", "BAJAJ-WALU", "ACTIVE", 145, 18, "Waluj Plant, Aurangabad", "2024-06-01"),
            OrganizationDetailDto("ORG-04", "Ashok Leyland Logistics", "AL-HOSUR", "ACTIVE", 45, 6, "Hosur Unit 2", "2025-01-20")
        )
    }
}

class SuperAdminDashboardViewModel(
    private val sessionManager: SessionManager,
    private val authRepository: AuthRepository,
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SuperAdminUiState())
    val uiState: StateFlow<SuperAdminUiState> = _uiState.asStateFlow()

    init {
        loadAdminData()
    }

    fun loadAdminData() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val kpisRes = adminRepository.getSuperAdminKpis()) {
                is NetworkResult.Success -> _uiState.update { it.copy(kpis = kpisRes.data) }
                else -> {}
            }
            when (val orgsRes = adminRepository.getOrganizations()) {
                is NetworkResult.Success -> {
                    if (orgsRes.data.isNotEmpty()) {
                        _uiState.update { it.copy(organizations = orgsRes.data) }
                    }
                }
                else -> {}
            }
            when (val healthRes = adminRepository.getSystemHealth()) {
                is NetworkResult.Success -> _uiState.update { it.copy(systemHealth = healthRes.data) }
                else -> {}
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun logout(onLogoutSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            sessionManager.clearSession()
            onLogoutSuccess()
        }
    }
}
