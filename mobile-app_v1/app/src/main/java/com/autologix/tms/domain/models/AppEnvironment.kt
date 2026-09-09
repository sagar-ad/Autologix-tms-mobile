package com.autologix.tms.domain.models

enum class AppEnvironment(
    val displayName: String,
    val defaultBaseUrl: String,
    val description: String
) {
    DEVELOPMENT("Development", "http://10.0.2.2:3000/api/v1/", "Local dev server / Android emulator"),
    UAT("UAT Staging", "https://uat-api.autologix-tms.com/api/v1/", "User acceptance testing environment"),
    PRODUCTION("Production", "https://api.autologix-tms.com/api/v1/", "Secure enterprise cloud endpoint")
}
