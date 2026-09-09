package com.autologix.tms.core.network

import com.autologix.tms.domain.models.AppEnvironment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

class ApiConfig private constructor() {

    private val _currentEnvironment = MutableStateFlow(AppEnvironment.DEVELOPMENT)
    val currentEnvironment: StateFlow<AppEnvironment> = _currentEnvironment.asStateFlow()

    private val _customBaseUrl = MutableStateFlow<String?>(null)
    val customBaseUrl: StateFlow<String?> = _customBaseUrl.asStateFlow()

    fun getBaseUrl(): String {
        val custom = _customBaseUrl.value
        if (!custom.isNullOrBlank() && isValidUrl(custom)) {
            return if (custom.endsWith("/")) custom else "$custom/"
        }
        val env = _currentEnvironment.value
        return if (env.defaultBaseUrl.endsWith("/")) env.defaultBaseUrl else "${env.defaultBaseUrl}/"
    }

    fun isValidUrl(url: String): Boolean {
        val httpUrl: HttpUrl? = url.toHttpUrlOrNull()
        return httpUrl != null && (httpUrl.scheme == "http" || httpUrl.scheme == "https")
    }

    fun updateEnvironment(environment: AppEnvironment, customUrl: String? = null) {
        _currentEnvironment.value = environment
        _customBaseUrl.value = if (!customUrl.isNullOrBlank() && isValidUrl(customUrl)) customUrl else null
    }

    companion object {
        @Volatile
        private var INSTANCE: ApiConfig? = null

        fun getInstance(): ApiConfig {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ApiConfig().also { INSTANCE = it }
            }
        }
    }
}
