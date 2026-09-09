package com.autologix.tms.data.remote

import com.autologix.tms.core.constants.ApiEndpoints
import com.autologix.tms.data.models.AuthUserDto
import com.autologix.tms.data.models.LoginRequestDto
import com.autologix.tms.data.models.LoginResponseDto
import com.autologix.tms.data.models.RefreshTokenRequestDto
import com.autologix.tms.data.models.RefreshTokenResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApiService {

    @POST(ApiEndpoints.AUTH_LOGIN)
    suspend fun login(
        @Body request: LoginRequestDto
    ): Response<LoginResponseDto>

    @POST(ApiEndpoints.AUTH_REFRESH)
    suspend fun refreshToken(
        @Body request: RefreshTokenRequestDto
    ): Response<RefreshTokenResponseDto>

    @POST(ApiEndpoints.AUTH_LOGOUT)
    suspend fun logout(): Response<Unit>

    @GET(ApiEndpoints.AUTH_ME)
    suspend fun getCurrentUser(): Response<AuthUserDto>
}
