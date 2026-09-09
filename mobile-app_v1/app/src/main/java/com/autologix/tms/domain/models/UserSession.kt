package com.autologix.tms.domain.models

data class UserSession(
    val userId: String,
    val email: String,
    val name: String,
    val role: String,
    val organizationId: String? = null,
    val organizationName: String? = null,
    val activeOrganizationName: String? = organizationName,
    val accessToken: String,
    val refreshToken: String? = null,
    val environment: AppEnvironment = AppEnvironment.DEVELOPMENT,
    val customBaseUrl: String? = null
)
