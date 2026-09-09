package com.autologix.tms.presentation.auth

import com.autologix.tms.domain.models.AppEnvironment

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val organizationId: String = "",
    val selectedEnvironment: AppEnvironment = AppEnvironment.DEVELOPMENT,
    val customApiUrl: String = "",
    val isCustomUrlActive: Boolean = false,
    val isConfigExpanded: Boolean = false,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val statusCode: Int? = null,
    val isNetworkError: Boolean = false
) {
    val isSubmitEnabled: Boolean
        get() = email.isNotBlank() && password.isNotBlank() && !isLoading
}
