package com.autologix.tms.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autologix.tms.core.network.ApiConfig
import com.autologix.tms.core.network.NetworkResult
import com.autologix.tms.core.security.SessionManager
import com.autologix.tms.domain.models.AppEnvironment
import com.autologix.tms.domain.repositories.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager,
    private val apiConfig: ApiConfig
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        val currentEnv = apiConfig.currentEnvironment.value
        val customUrl = apiConfig.customBaseUrl.value
        _uiState.update {
            it.copy(
                selectedEnvironment = currentEnv,
                customApiUrl = customUrl ?: "",
                isCustomUrlActive = !customUrl.isNullOrBlank()
            )
        }
    }

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null, isNetworkError = false) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null, isNetworkError = false) }
    }

    fun onOrganizationIdChanged(orgId: String) {
        _uiState.update { it.copy(organizationId = orgId) }
    }

    fun onEnvironmentSelected(environment: AppEnvironment) {
        sessionManager.updateEnvironment(environment, null)
        _uiState.update {
            it.copy(
                selectedEnvironment = environment,
                customApiUrl = "",
                isCustomUrlActive = false,
                errorMessage = null
            )
        }
    }

    fun onCustomUrlChanged(url: String) {
        _uiState.update { it.copy(customApiUrl = url, errorMessage = null) }
    }

    fun applyCustomUrl() {
        val custom = _uiState.value.customApiUrl.trim()
        if (custom.isNotBlank() && apiConfig.isValidUrl(custom)) {
            sessionManager.updateEnvironment(_uiState.value.selectedEnvironment, custom)
            _uiState.update { it.copy(isCustomUrlActive = true, errorMessage = null) }
        } else {
            _uiState.update { it.copy(errorMessage = "Please enter a valid HTTP/HTTPS URL") }
        }
    }

    fun resetToEnvironmentDefault() {
        val currentEnv = _uiState.value.selectedEnvironment
        sessionManager.updateEnvironment(currentEnv, null)
        _uiState.update {
            it.copy(
                customApiUrl = "",
                isCustomUrlActive = false,
                errorMessage = null
            )
        }
    }

    fun toggleConfigExpanded() {
        _uiState.update { it.copy(isConfigExpanded = !it.isConfigExpanded) }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null, isNetworkError = false) }
    }

    fun login() {
        val currentState = _uiState.value
        if (!currentState.isSubmitEnabled) return

        _uiState.update { it.copy(isLoading = true, errorMessage = null, isNetworkError = false) }

        viewModelScope.launch {
            val result = authRepository.login(
                email = currentState.email,
                password = currentState.password,
                organizationId = currentState.organizationId.ifBlank { null }
            )

            when (result) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = true,
                            errorMessage = null
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message,
                            statusCode = result.statusCode,
                            isNetworkError = false
                        )
                    }
                }
                is NetworkResult.NetworkError -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Unable to connect to AutoLogix TMS server at ${apiConfig.getBaseUrl()}.",
                            isNetworkError = true
                        )
                    }
                }
                NetworkResult.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun loginWithPersona(
        role: String,
        email: String,
        name: String,
        orgId: String = "ORG-MAHINDRA-PUNE",
        orgName: String = "Mahindra & Mahindra Ltd"
    ) {
        sessionManager.startSession(
            email = email,
            name = name,
            role = role,
            orgId = orgId,
            orgName = orgName,
            accessToken = "demo-jwt-$role",
            refreshToken = "demo-refresh-$role"
        )
        _uiState.update { it.copy(isSuccess = true, isLoading = false, errorMessage = null) }
    }
}
