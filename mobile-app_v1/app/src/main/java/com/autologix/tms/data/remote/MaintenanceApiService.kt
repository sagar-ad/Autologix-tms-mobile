package com.autologix.tms.data.remote

import com.autologix.tms.core.constants.ApiEndpoints
import com.autologix.tms.data.models.CreateWorkOrderRequestDto
import com.autologix.tms.data.models.DamageReportDto
import com.autologix.tms.data.models.MaintenanceManagerKpisDto
import com.autologix.tms.data.models.ManagerReviewRequestDto
import com.autologix.tms.data.models.MediaUploadResponseDto
import com.autologix.tms.data.models.PaginatedCustomerResponse
import com.autologix.tms.data.models.PmScheduleDto
import com.autologix.tms.data.models.TechnicianDto
import com.autologix.tms.data.models.WorkOrderActionRequestDto
import com.autologix.tms.data.models.WorkOrderDto
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface MaintenanceApiService {

    @GET(ApiEndpoints.DASHBOARD_KPIS)
    suspend fun getMaintenanceKpis(
        @Query("timeRange") timeRange: String? = "WEEK"
    ): Response<MaintenanceManagerKpisDto>

    @GET(ApiEndpoints.PM_SCHEDULES)
    suspend fun getPmSchedules(
        @Query("status") status: String? = null,
        @Query("dateFrom") dateFrom: String? = null,
        @Query("dateTo") dateTo: String? = null,
        @Query("technicianId") technicianId: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<PaginatedCustomerResponse<PmScheduleDto>>

    @GET(ApiEndpoints.WORK_ORDERS)
    suspend fun getAllWorkOrders(
        @Query("status") status: String? = null,
        @Query("priority") priority: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<PaginatedCustomerResponse<WorkOrderDto>>

    @GET(ApiEndpoints.WORK_ORDERS_MY)
    suspend fun getMyWorkOrders(
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<PaginatedCustomerResponse<WorkOrderDto>>

    @POST(ApiEndpoints.WORK_ORDERS)
    suspend fun createOrAssignWorkOrder(
        @Body request: CreateWorkOrderRequestDto
    ): Response<WorkOrderDto>

    @PATCH(ApiEndpoints.WORK_ORDER_ACTION)
    suspend fun performWorkOrderAction(
        @Path("id") id: String,
        @Body request: WorkOrderActionRequestDto
    ): Response<WorkOrderDto>

    @PATCH(ApiEndpoints.WORK_ORDER_ACTION)
    suspend fun reviewWorkOrder(
        @Path("id") id: String,
        @Body request: ManagerReviewRequestDto
    ): Response<WorkOrderDto>

    @GET(ApiEndpoints.TECHNICIANS)
    suspend fun getTechnicians(
        @Query("availableOnly") availableOnly: Boolean? = null
    ): Response<List<TechnicianDto>>

    @GET(ApiEndpoints.DAMAGES)
    suspend fun getCustomerDamageRequests(
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<PaginatedCustomerResponse<DamageReportDto>>

    @Multipart
    @POST(ApiEndpoints.MEDIA_UPLOAD)
    suspend fun uploadMaintenancePhoto(
        @Part file: MultipartBody.Part
    ): Response<MediaUploadResponseDto>
}
