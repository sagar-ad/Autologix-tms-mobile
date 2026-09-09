package com.autologix.tms.data.repositories

import com.autologix.tms.core.network.NetworkResult
import com.autologix.tms.core.security.SessionManager
import com.autologix.tms.data.models.ErrorResponseDto
import com.autologix.tms.data.models.LoginRequestDto
import com.autologix.tms.data.models.LoginResponseDto
import com.autologix.tms.data.models.RefreshTokenRequestDto
import com.autologix.tms.data.remote.AuthApiService
import com.autologix.tms.domain.models.UserSession
import com.autologix.tms.domain.repositories.AuthRepository
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class AuthRepositoryImpl(
    private val authApiService: AuthApiService,
    private val sessionManager: SessionManager
) : AuthRepository {

    private val gson = Gson()

    override suspend fun login(
        email: String,
        password: String,
        organizationId: String?
    ): NetworkResult<LoginResponseDto> = withContext(Dispatchers.IO) {
        try {
            val request = LoginRequestDto(
                email = email.trim(),
                password = password,
                organizationId = organizationId?.trim()?.ifBlank { null }
            )
            val response = authApiService.login(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                sessionManager.startSession(
                    email = body.user.email,
                    name = body.user.name,
                    role = body.user.role,
                    orgId = body.user.organizationId,
                    orgName = body.user.organizationName,
                    accessToken = body.accessToken,
                    refreshToken = body.refreshToken
                )
                NetworkResult.Success(body)
            } else {
                parseHttpError(response)
            }
        } catch (e: Exception) {
            NetworkResult.NetworkError(e)
        }
    }

    override suspend fun refreshToken(refreshToken: String): NetworkResult<String> =
        withContext(Dispatchers.IO) {
            try {
                val response = authApiService.refreshToken(RefreshTokenRequestDto(refreshToken))
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    sessionManager.sessionState.value?.let { current ->
                        sessionManager.saveSession(
                            current.copy(
                                accessToken = body.accessToken,
                                refreshToken = body.refreshToken ?: current.refreshToken
                            )
                        )
                    }
                    NetworkResult.Success(body.accessToken)
                } else {
                    parseHttpError(response)
                }
            } catch (e: Exception) {
                NetworkResult.NetworkError(e)
            }
        }

    override suspend fun logout(): NetworkResult<Unit> = withContext(Dispatchers.IO) {
        try {
            authApiService.logout()
        } catch (_: Exception) {
            // Best effort logout on remote
        }
        sessionManager.clearSession()
        NetworkResult.Success(Unit)
    }

    override suspend fun getCurrentUser(): NetworkResult<UserSession> = withContext(Dispatchers.IO) {
        val current = sessionManager.sessionState.value
        if (current != null) {
            NetworkResult.Success(current)
        } else {
            NetworkResult.Error("No active user session", statusCode = 401)
        }
    }

    private fun <T> parseHttpError(response: Response<T>): NetworkResult.Error {
        val errorBody = response.errorBody()?.string()
        val statusCode = response.code()
        val parsedMessage = if (!errorBody.isNullOrBlank()) {
            try {
                val errorDto = gson.fromJson(errorBody, ErrorResponseDto::class.java)
                errorDto.singleMessage
            } catch (_: Exception) {
                "Authentication failed ($statusCode)"
            }
        } else {
            "HTTP $statusCode: ${response.message()}"
        }

        return NetworkResult.Error(
            message = parsedMessage,
            statusCode = statusCode,
            errorBody = errorBody
        )
    }
}
