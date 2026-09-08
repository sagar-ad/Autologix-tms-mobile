package com.autologix.tms.data.repositories

import com.autologix.tms.core.network.NetworkResult
import com.autologix.tms.data.models.CreateWorkOrderRequestDto
import com.autologix.tms.data.models.DamageReportDto
import com.autologix.tms.data.models.ErrorResponseDto
import com.autologix.tms.data.models.MaintenanceManagerKpisDto
import com.autologix.tms.data.models.ManagerReviewRequestDto
import com.autologix.tms.data.models.MediaUploadResponseDto
import com.autologix.tms.data.models.PmScheduleDto
import com.autologix.tms.data.models.TechnicianDto
import com.autologix.tms.data.models.WorkOrderActionRequestDto
import com.autologix.tms.data.models.WorkOrderDto
import com.autologix.tms.data.remote.MaintenanceApiService
import com.autologix.tms.domain.repositories.MaintenanceRepository
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

class MaintenanceRepositoryImpl(
    private val apiService: MaintenanceApiService
) : MaintenanceRepository {

    private val gson = Gson()

    override suspend fun getMaintenanceKpis(): NetworkResult<MaintenanceManagerKpisDto> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getMaintenanceKpis()
                if (response.isSuccessful && response.body() != null) {
                    NetworkResult.Success(response.body()!!)
                } else {
                    parseHttpError(response)
                }
            } catch (e: Exception) {
                NetworkResult.NetworkError(e)
            }
        }

    override suspend fun getPmSchedules(
        status: String?,
        technicianId: String?
    ): NetworkResult<List<PmScheduleDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getPmSchedules(status = status, technicianId = technicianId)
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!.items)
            } else {
                parseHttpError(response)
            }
        } catch (e: Exception) {
            NetworkResult.NetworkError(e)
        }
    }

    override suspend fun getAllWorkOrders(
        status: String?,
        priority: String?
    ): NetworkResult<List<WorkOrderDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAllWorkOrders(status = status, priority = priority)
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!.items)
            } else {
                parseHttpError(response)
            }
        } catch (e: Exception) {
            NetworkResult.NetworkError(e)
        }
    }

    override suspend fun getMyWorkOrders(status: String?): NetworkResult<List<WorkOrderDto>> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getMyWorkOrders(status = status)
                if (response.isSuccessful && response.body() != null) {
                    NetworkResult.Success(response.body()!!.items)
                } else {
                    parseHttpError(response)
                }
            } catch (e: Exception) {
                NetworkResult.NetworkError(e)
            }
        }

    override suspend fun createOrAssignWorkOrder(
        request: CreateWorkOrderRequestDto
    ): NetworkResult<WorkOrderDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createOrAssignWorkOrder(request)
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                parseHttpError(response)
            }
        } catch (e: Exception) {
            NetworkResult.NetworkError(e)
        }
    }

    override suspend fun performWorkOrderAction(
        id: String,
        request: WorkOrderActionRequestDto
    ): NetworkResult<WorkOrderDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.performWorkOrderAction(id, request)
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                parseHttpError(response)
            }
        } catch (e: Exception) {
            NetworkResult.NetworkError(e)
        }
    }

    override suspend fun reviewWorkOrder(
        id: String,
        request: ManagerReviewRequestDto
    ): NetworkResult<WorkOrderDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.reviewWorkOrder(id, request)
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                parseHttpError(response)
            }
        } catch (e: Exception) {
            NetworkResult.NetworkError(e)
        }
    }

    override suspend fun getTechnicians(
        availableOnly: Boolean?
    ): NetworkResult<List<TechnicianDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getTechnicians(availableOnly = availableOnly)
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                parseHttpError(response)
            }
        } catch (e: Exception) {
            NetworkResult.NetworkError(e)
        }
    }

    override suspend fun getCustomerDamageRequests(
        status: String?
    ): NetworkResult<List<DamageReportDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getCustomerDamageRequests(status = status)
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
            val response = apiService.uploadMaintenancePhoto(part)
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
