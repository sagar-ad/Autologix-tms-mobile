package com.autologix.tms.data.repositories

import com.autologix.tms.core.network.NetworkResult
import com.autologix.tms.data.models.CustomerDashboardKpiDto
import com.autologix.tms.data.models.CustomerFleetReportDto
import com.autologix.tms.data.models.CustomerNotificationDto
import com.autologix.tms.data.models.CustomerReportsDto
import com.autologix.tms.data.models.DamageChecklistTemplateDto
import com.autologix.tms.data.models.DamageReportDto
import com.autologix.tms.data.models.ErrorResponseDto
import com.autologix.tms.data.models.MediaUploadResponseDto
import com.autologix.tms.data.models.ReportDamageRequestDto
import com.autologix.tms.data.models.Trolley360ViewDto
import com.autologix.tms.data.models.TrolleyFullDetailDto
import com.autologix.tms.data.models.TrolleyQuickScanDto
import com.autologix.tms.data.models.TrolleySummaryDto
import com.autologix.tms.data.remote.CustomerApiService
import com.autologix.tms.domain.repositories.CustomerRepository
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

class CustomerRepositoryImpl(
    private val customerApiService: CustomerApiService
) : CustomerRepository {

    private val gson = Gson()

    override suspend fun getCustomerDashboardKpis(): NetworkResult<CustomerDashboardKpiDto> =
        withContext(Dispatchers.IO) {
            try {
                val response = customerApiService.getCustomerDashboardKpis()
                if (response.isSuccessful && response.body() != null) {
                    NetworkResult.Success(response.body()!!)
                } else {
                    parseHttpError(response)
                }
            } catch (e: Exception) {
                NetworkResult.NetworkError(e)
            }
        }

    override suspend fun getTrolleys(
        search: String?,
        status: String?,
        limit: Int?
    ): NetworkResult<List<TrolleySummaryDto>> = withContext(Dispatchers.IO) {
        try {
            val response = customerApiService.getTrolleys(search = search, status = status, limit = limit ?: 30)
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!.items)
            } else {
                parseHttpError(response)
            }
        } catch (e: Exception) {
            NetworkResult.NetworkError(e)
        }
    }

    override suspend fun scanTrolley(barcode: String): NetworkResult<TrolleyQuickScanDto> =
        withContext(Dispatchers.IO) {
            try {
                val response = customerApiService.scanTrolley(barcode.trim())
                if (response.isSuccessful && response.body() != null) {
                    NetworkResult.Success(response.body()!!)
                } else {
                    parseHttpError(response)
                }
            } catch (e: Exception) {
                NetworkResult.NetworkError(e)
            }
        }

    override suspend fun getTrolleyDetail(id: String): NetworkResult<TrolleyFullDetailDto> =
        withContext(Dispatchers.IO) {
            try {
                val response = customerApiService.getTrolleyDetail(id)
                if (response.isSuccessful && response.body() != null) {
                    NetworkResult.Success(response.body()!!)
                } else {
                    parseHttpError(response)
                }
            } catch (e: Exception) {
                NetworkResult.NetworkError(e)
            }
        }

    override suspend fun getTrolley360(id: String): NetworkResult<Trolley360ViewDto> =
        withContext(Dispatchers.IO) {
            try {
                val response = customerApiService.getTrolley360(id)
                if (response.isSuccessful && response.body() != null) {
                    NetworkResult.Success(response.body()!!)
                } else {
                    parseHttpError(response)
                }
            } catch (e: Exception) {
                NetworkResult.NetworkError(e)
            }
        }

    override suspend fun getDamageChecklist(typeId: String): NetworkResult<DamageChecklistTemplateDto> =
        withContext(Dispatchers.IO) {
            try {
                val response = customerApiService.getDamageChecklist(typeId)
                if (response.isSuccessful && response.body() != null) {
                    NetworkResult.Success(response.body()!!)
                } else {
                    parseHttpError(response)
                }
            } catch (e: Exception) {
                NetworkResult.NetworkError(e)
            }
        }

    override suspend fun uploadDamagePhoto(
        fileBytes: ByteArray,
        fileName: String,
        mimeType: String
    ): NetworkResult<MediaUploadResponseDto> = withContext(Dispatchers.IO) {
        try {
            val requestFile = fileBytes.toRequestBody(mimeType.toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", fileName, requestFile)
            val tagPart = "DAMAGE_REPORT".toRequestBody("text/plain".toMediaTypeOrNull())

            val response = customerApiService.uploadMedia(filePart, tagPart)
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                parseHttpError(response)
            }
        } catch (e: Exception) {
            NetworkResult.NetworkError(e)
        }
    }

    override suspend fun submitDamageRequest(request: ReportDamageRequestDto): NetworkResult<DamageReportDto> =
        withContext(Dispatchers.IO) {
            try {
                val response = customerApiService.submitDamageReport(request)
                if (response.isSuccessful && response.body() != null) {
                    NetworkResult.Success(response.body()!!)
                } else {
                    parseHttpError(response)
                }
            } catch (e: Exception) {
                NetworkResult.NetworkError(e)
            }
        }

    override suspend fun getCustomerDamageRequests(status: String?): NetworkResult<List<DamageReportDto>> =
        withContext(Dispatchers.IO) {
            try {
                val response = customerApiService.getDamageRequests(status = status)
                if (response.isSuccessful && response.body() != null) {
                    NetworkResult.Success(response.body()!!.items)
                } else {
                    parseHttpError(response)
                }
            } catch (e: Exception) {
                NetworkResult.NetworkError(e)
            }
        }

    override suspend fun getDamageRequestDetail(id: String): NetworkResult<DamageReportDto> =
        withContext(Dispatchers.IO) {
            try {
                val response = customerApiService.getDamageRequestDetail(id)
                if (response.isSuccessful && response.body() != null) {
                    NetworkResult.Success(response.body()!!)
                } else {
                    parseHttpError(response)
                }
            } catch (e: Exception) {
                NetworkResult.NetworkError(e)
            }
        }

    override suspend fun getNotifications(unreadOnly: Boolean): NetworkResult<List<CustomerNotificationDto>> =
        withContext(Dispatchers.IO) {
            try {
                val response = customerApiService.getNotifications(unreadOnly = unreadOnly)
                if (response.isSuccessful && response.body() != null) {
                    val dtos = response.body()!!.items.map { item ->
                        CustomerNotificationDto(
                            id = item.id,
                            title = item.title,
                            message = item.message,
                            createdAt = item.createdAt,
                            isRead = item.isRead,
                            type = item.type,
                            targetId = item.entityId,
                            relatedEntityId = item.entityId
                        )
                    }
                    NetworkResult.Success(dtos)
                } else {
                    parseHttpError(response)
                }
            } catch (e: Exception) {
                NetworkResult.NetworkError(e)
            }
        }

    override suspend fun markNotificationAsRead(id: String): NetworkResult<CustomerNotificationDto> =
        withContext(Dispatchers.IO) {
            try {
                val response = customerApiService.markNotificationAsRead(id)
                if (response.isSuccessful && response.body() != null) {
                    val item = response.body()!!
                    NetworkResult.Success(
                        CustomerNotificationDto(
                            id = item.id,
                            title = item.title,
                            message = item.message,
                            createdAt = item.createdAt,
                            isRead = item.isRead,
                            type = item.type,
                            targetId = item.entityId,
                            relatedEntityId = item.entityId
                        )
                    )
                } else {
                    parseHttpError(response)
                }
            } catch (e: Exception) {
                NetworkResult.NetworkError(e)
            }
        }

    override suspend fun getCustomerFleetReport(): NetworkResult<CustomerFleetReportDto> =
        withContext(Dispatchers.IO) {
            try {
                val response = customerApiService.getCustomerFleetReport()
                if (response.isSuccessful && response.body() != null) {
                    NetworkResult.Success(response.body()!!)
                } else {
                    parseHttpError(response)
                }
            } catch (e: Exception) {
                NetworkResult.NetworkError(e)
            }
        }

    override suspend fun getCustomerReports(period: String?): NetworkResult<CustomerReportsDto> =
        withContext(Dispatchers.IO) {
            try {
                val response = customerApiService.getCustomerFleetReport()
                if (response.isSuccessful && response.body() != null) {
                    val report = response.body()!!
                    NetworkResult.Success(
                        CustomerReportsDto(
                            fleetAvailabilityPercent = report.utilizationRate,
                            totalIncidentsReported = report.incidentsThisMonth,
                            pmCompliancePercent = report.pmComplianceRate
                        )
                    )
                } else {
                    NetworkResult.Success(CustomerReportsDto())
                }
            } catch (e: Exception) {
                NetworkResult.Success(CustomerReportsDto())
            }
        }

    private fun <T> parseHttpError(response: Response<T>): NetworkResult.Error {
        val errorBody = response.errorBody()?.string()
        val statusCode = response.code()

        val parsedMessage = if (!errorBody.isNullOrBlank()) {
            try {
                val errorDto = gson.fromJson(errorBody, ErrorResponseDto::class.java)
                errorDto.getFormattedMessage()
            } catch (_: Exception) {
                "Server returned status $statusCode"
            }
        } else {
            "HTTP error $statusCode: ${response.message()}"
        }

        return NetworkResult.Error(
            message = parsedMessage,
            statusCode = statusCode,
            errorBody = errorBody
        )
    }
}
