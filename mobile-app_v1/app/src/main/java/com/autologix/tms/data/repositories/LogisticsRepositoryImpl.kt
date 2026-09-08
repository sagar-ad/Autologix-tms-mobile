package com.autologix.tms.data.repositories

import com.autologix.tms.core.network.NetworkResult
import com.autologix.tms.data.models.ErrorResponseDto
import com.autologix.tms.data.models.GateInRequestDto
import com.autologix.tms.data.models.GateOutRequestDto
import com.autologix.tms.data.models.LogisticsKpisDto
import com.autologix.tms.data.models.MediaUploadResponseDto
import com.autologix.tms.data.models.MovementRecordDto
import com.autologix.tms.data.remote.LogisticsApiService
import com.autologix.tms.domain.repositories.LogisticsRepository
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

class LogisticsRepositoryImpl(
    private val apiService: LogisticsApiService
) : LogisticsRepository {

    private val gson = Gson()

    override suspend fun getLogisticsKpis(): NetworkResult<LogisticsKpisDto> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getLogisticsKpis()
                if (response.isSuccessful && response.body() != null) {
                    NetworkResult.Success(response.body()!!)
                } else {
                    parseHttpError(response)
                }
            } catch (e: Exception) {
                NetworkResult.NetworkError(e)
            }
        }

    override suspend fun recordGateIn(
        request: GateInRequestDto
    ): NetworkResult<MovementRecordDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.recordGateIn(request)
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                parseHttpError(response)
            }
        } catch (e: Exception) {
            NetworkResult.NetworkError(e)
        }
    }

    override suspend fun recordGateOut(
        request: GateOutRequestDto
    ): NetworkResult<MovementRecordDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.recordGateOut(request)
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                parseHttpError(response)
            }
        } catch (e: Exception) {
            NetworkResult.NetworkError(e)
        }
    }

    override suspend fun getMovementHistory(
        movementType: String?,
        trolleyId: String?
    ): NetworkResult<List<MovementRecordDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getMovementHistory(movementType = movementType, trolleyId = trolleyId)
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!.items)
            } else {
                parseHttpError(response)
            }
        } catch (e: Exception) {
            NetworkResult.NetworkError(e)
        }
    }

    override suspend fun uploadPhoto(
        bytes: ByteArray,
        fileName: String,
        mimeType: String
    ): NetworkResult<MediaUploadResponseDto> = withContext(Dispatchers.IO) {
        try {
            val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file", fileName, requestBody)
            val response = apiService.uploadGatePhoto(part)
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
