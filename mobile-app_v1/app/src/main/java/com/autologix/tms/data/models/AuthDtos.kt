package com.autologix.tms.data.models

import com.google.gson.annotations.SerializedName

data class LoginRequestDto(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("organizationId")
    val organizationId: String? = null
)

data class AuthUserDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("role")
    val role: String,
    @SerializedName("organizationId")
    val organizationId: String? = null,
    @SerializedName("organizationName")
    val organizationName: String? = null
)

data class LoginResponseDto(
    @SerializedName("accessToken")
    val accessToken: String,
    @SerializedName("refreshToken")
    val refreshToken: String? = null,
    @SerializedName("user")
    val user: AuthUserDto
)

data class RefreshTokenRequestDto(
    @SerializedName("refreshToken")
    val refreshToken: String
)

data class RefreshTokenResponseDto(
    @SerializedName("accessToken")
    val accessToken: String,
    @SerializedName("refreshToken")
    val refreshToken: String? = null
)
