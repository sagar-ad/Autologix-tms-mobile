package com.autologix.tms.data.remote

import com.autologix.tms.core.constants.ApiEndpoints
import com.autologix.tms.data.models.CustomerDashboardKpiDto
import com.autologix.tms.data.models.CustomerFleetReportDto
import com.autologix.tms.data.models.DamageChecklistTemplateDto
import com.autologix.tms.data.models.DamageReportDto
import com.autologix.tms.data.models.MediaUploadResponseDto
import com.autologix.tms.data.models.NotificationItemDto
import com.autologix.tms.data.models.PaginatedCustomerResponse
import com.autologix.tms.data.models.ReportDamageRequestDto
import com.autologix.tms.data.models.Trolley360ViewDto
import com.autologix.tms.data.models.TrolleyFullDetailDto
import com.autologix.tms.data.models.TrolleyQuickScanDto
import com.autologix.tms.data.models.TrolleySummaryDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit interface for AutoLogix TMS Customer module.
 * Direct consumption of existing NestJS backend endpoints as cataloged in docs/mobile-api-inventory.md.
 */
interface CustomerApiService {

    @GET(ApiEndpoints.DASHBOARD_KPIS)
    suspend fun getCustomerDashboardKpis(
        @Query("timeRange") timeRange: String? = "MONTH"
    ): Response<CustomerDashboardKpiDto>

    @GET(ApiEndpoints.TROLLEYS)
    suspend fun getTrolleys(
        @Query("search") search: String? = null,
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 30
    ): Response<PaginatedCustomerResponse<TrolleySummaryDto>>

    @GET(ApiEndpoints.TROLLEY_SCAN)
    suspend fun scanTrolley(
        @Path("barcode") barcode: String
    ): Response<TrolleyQuickScanDto>

    @GET(ApiEndpoints.TROLLEY_DETAIL)
    suspend fun getTrolleyDetail(
        @Path("id") id: String
    ): Response<TrolleyFullDetailDto>

    @GET(ApiEndpoints.TROLLEY_360)
    suspend fun getTrolley360(
        @Path("id") id: String
    ): Response<Trolley360ViewDto>

    @GET(ApiEndpoints.PM_CHECKLIST_TEMPLATES)
    suspend fun getDamageChecklist(
        @Path("typeId") typeId: String
    ): Response<DamageChecklistTemplateDto>

    @Multipart
    @POST(ApiEndpoints.MEDIA_UPLOAD)
    suspend fun uploadMedia(
        @Part file: MultipartBody.Part,
        @Part("tag") tag: RequestBody
    ): Response<MediaUploadResponseDto>

    @POST(ApiEndpoints.DAMAGES)
    suspend fun submitDamageReport(
        @Body request: ReportDamageRequestDto
    ): Response<DamageReportDto>

    @GET(ApiEndpoints.DAMAGES)
    suspend fun getDamageRequests(
        @Query("status") status: String? = null,
        @Query("trolleyId") trolleyId: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<PaginatedCustomerResponse<DamageReportDto>>

    @GET(ApiEndpoints.DAMAGE_DETAIL)
    suspend fun getDamageRequestDetail(
        @Path("id") id: String
    ): Response<DamageReportDto>

    @GET(ApiEndpoints.NOTIFICATIONS)
    suspend fun getNotifications(
        @Query("unreadOnly") unreadOnly: Boolean = false,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<PaginatedCustomerResponse<NotificationItemDto>>

    @PATCH(ApiEndpoints.NOTIFICATION_MARK_READ)
    suspend fun markNotificationAsRead(
        @Path("id") id: String
    ): Response<NotificationItemDto>

    @GET(ApiEndpoints.DASHBOARD_FLEET_UTILIZATION)
    suspend fun getCustomerFleetReport(): Response<CustomerFleetReportDto>
}
