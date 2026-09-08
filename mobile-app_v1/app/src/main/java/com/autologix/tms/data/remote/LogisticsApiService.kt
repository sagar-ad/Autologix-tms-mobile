package com.autologix.tms.data.remote

import com.autologix.tms.core.constants.ApiEndpoints
import com.autologix.tms.data.models.GateInRequestDto
import com.autologix.tms.data.models.GateOutRequestDto
import com.autologix.tms.data.models.LogisticsKpisDto
import com.autologix.tms.data.models.MediaUploadResponseDto
import com.autologix.tms.data.models.MovementRecordDto
import com.autologix.tms.data.models.PaginatedCustomerResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface LogisticsApiService {

    @GET(ApiEndpoints.DASHBOARD_KPIS)
    suspend fun getLogisticsKpis(): Response<LogisticsKpisDto>

    @POST(ApiEndpoints.GATE_IN)
    suspend fun recordGateIn(
        @Body request: GateInRequestDto
    ): Response<MovementRecordDto>

    @POST(ApiEndpoints.GATE_OUT)
    suspend fun recordGateOut(
        @Body request: GateOutRequestDto
    ): Response<MovementRecordDto>

    @GET(ApiEndpoints.MOVEMENT_HISTORY)
    suspend fun getMovementHistory(
        @Query("movementType") movementType: String? = null,
        @Query("trolleyId") trolleyId: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<PaginatedCustomerResponse<MovementRecordDto>>

    @Multipart
    @POST(ApiEndpoints.MEDIA_UPLOAD)
    suspend fun uploadGatePhoto(
        @Part file: MultipartBody.Part
    ): Response<MediaUploadResponseDto>
}
