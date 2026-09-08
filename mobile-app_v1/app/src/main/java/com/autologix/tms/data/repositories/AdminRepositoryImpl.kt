package com.autologix.tms.data.repositories

import com.autologix.tms.core.network.NetworkResult
import com.autologix.tms.data.models.ErrorResponseDto
import com.autologix.tms.data.models.OrganizationDetailDto
import com.autologix.tms.data.models.SuperAdminKpisDto
import com.autologix.tms.data.models.SystemHealthDto
import com.autologix.tms.data.remote.AdminApiService
import com.autologix.tms.domain.repositories.AdminRepository
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class AdminRepositoryImpl(
    private val apiService: AdminApiService
) : AdminRepository {

    private val gson = Gson()

    override suspend fun getSuperAdminKpis(): NetworkResult<SuperAdminKpisDto> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getSuperAdminKpis()
                if (response.isSuccessful && response.body() != null) {
                    NetworkResult.Success(response.body()!!)
                } else {
                    parseHttpError(response)
                }
            } catch (e: Exception) {
                NetworkResult.NetworkError(e)
            }
        }

    override suspend fun getOrganizations(): NetworkResult<List<OrganizationDetailDto>> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getOrganizations()
                if (response.isSuccessful && response.body() != null) {
                    NetworkResult.Success(response.body()!!)
                } else {
                    parseHttpError(response)
                }
            } catch (e: Exception) {
                NetworkResult.NetworkError(e)
            }
        }

    override suspend fun getSystemHealth(): NetworkResult<SystemHealthDto> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getSystemHealth()
                if (response.isSuccessful && response.body() != null) {
                    NetworkResult.Success(response.body()!!)
                } else {
                    parseHttpError(response)
                }
            } catch (e: Exception) {
                NetworkResult.NetworkError(e)
            }
        }

    private fun <T> parseHttpError(response: Response<T>): NetworkResult.Error {
        val errorBody = response.errorBody()?.string()
        val errorMessage = try {
            if (!errorBody.isNullOrBlank()) {
                val errorDto = gson.fromJson(errorBody, ErrorResponseDto::class.java)
                errorDto.singleMessage
            } else {
                response.message().ifBlank { "HTTP Error ${response.code()}" }
            }
        } catch (_: Exception) {
            "HTTP ${response.code()}: ${response.message()}"
        }
        return NetworkResult.Error(message = errorMessage, statusCode = response.code())
    }
}
