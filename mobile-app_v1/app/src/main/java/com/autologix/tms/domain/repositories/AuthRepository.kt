package com.autologix.tms.domain.repositories

import com.autologix.tms.core.network.NetworkResult
import com.autologix.tms.data.models.LoginResponseDto
import com.autologix.tms.domain.models.UserSession

interface AuthRepository {
    suspend fun login(
        email: String,
        password: String,
        organizationId: String? = null
    ): NetworkResult<LoginResponseDto>

    suspend fun refreshToken(refreshToken: String): NetworkResult<String>

    suspend fun logout(): NetworkResult<Unit>

    suspend fun getCurrentUser(): NetworkResult<UserSession>
}
