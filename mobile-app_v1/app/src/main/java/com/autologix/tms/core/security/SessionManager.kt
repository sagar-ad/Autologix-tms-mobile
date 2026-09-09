package com.autologix.tms.core.security

import com.autologix.tms.core.network.ApiConfig
import com.autologix.tms.domain.models.AppEnvironment
import com.autologix.tms.domain.models.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

class SessionManager(
    private val tokenStorage: SecureTokenStorage,
    private val apiConfig: ApiConfig
) {

    private val _sessionState = MutableStateFlow<UserSession?>(loadSavedSession())
    val sessionState: StateFlow<UserSession?> = _sessionState.asStateFlow()

    val selectedEnvironment: StateFlow<AppEnvironment> = apiConfig.currentEnvironment
    val customBaseUrl: StateFlow<String?> = apiConfig.customBaseUrl

    fun updateEnvironment(environment: AppEnvironment, customUrl: String? = null) {
        apiConfig.updateEnvironment(environment, customUrl)
        _sessionState.value?.let { currentSession ->
            val updated = currentSession.copy(
                environment = environment,
                customBaseUrl = customUrl
            )
            saveSession(updated)
        }
    }

    fun startSession(
        email: String,
        name: String,
        role: String,
        orgId: String?,
        orgName: String?,
        accessToken: String,
        refreshToken: String? = null
    ) {
        tokenStorage.saveTokens(accessToken, refreshToken)
        val session = UserSession(
            userId = "usr_${System.currentTimeMillis()}",
            email = email,
            name = name,
            role = role,
            organizationId = orgId,
            organizationName = orgName,
            accessToken = accessToken,
            refreshToken = refreshToken,
            environment = apiConfig.currentEnvironment.value,
            customBaseUrl = apiConfig.customBaseUrl.value
        )
        saveSession(session)
    }

    fun saveSession(session: UserSession) {
        _sessionState.value = session
        tokenStorage.saveTokens(session.accessToken, session.refreshToken)

        val json = JSONObject().apply {
            put("userId", session.userId)
            put("email", session.email)
            put("name", session.name)
            put("role", session.role)
            put("organizationId", session.organizationId ?: "")
            put("organizationName", session.organizationName ?: "")
            put("environment", session.environment.name)
            put("customBaseUrl", session.customBaseUrl ?: "")
        }
        tokenStorage.saveSessionData(KEY_SESSION_DATA, json.toString())
    }

    fun clearSession() {
        _sessionState.value = null
        tokenStorage.clear()
    }

    fun isLoggedIn(): Boolean = _sessionState.value != null && !tokenStorage.getAccessToken().isNullOrBlank()

    private fun loadSavedSession(): UserSession? {
        val token = tokenStorage.getAccessToken() ?: return null
        val sessionJson = tokenStorage.getSessionData(KEY_SESSION_DATA) ?: return null
        return try {
            val json = JSONObject(sessionJson)
            val envStr = json.optString("environment", AppEnvironment.DEVELOPMENT.name)
            val env = try { AppEnvironment.valueOf(envStr) } catch (_: Exception) { AppEnvironment.DEVELOPMENT }
            val customUrl = json.optString("customBaseUrl").ifBlank { null }

            UserSession(
                userId = json.optString("userId", "usr_saved"),
                email = json.optString("email", ""),
                name = json.optString("name", "AutoLogix User"),
                role = json.optString("role", "CUSTOMER"),
                organizationId = json.optString("organizationId").ifBlank { null },
                organizationName = json.optString("organizationName").ifBlank { null },
                accessToken = token,
                refreshToken = tokenStorage.getRefreshToken(),
                environment = env,
                customBaseUrl = customUrl
            )
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        private const val KEY_SESSION_DATA = "user_session_json"
    }
}
