package com.autologix.tms.data.remote

import com.autologix.tms.core.constants.ApiEndpoints
import com.autologix.tms.data.models.OrganizationDetailDto
import com.autologix.tms.data.models.SuperAdminKpisDto
import com.autologix.tms.data.models.SystemHealthDto
import retrofit2.Response
import retrofit2.http.GET

interface AdminApiService {

    @GET(ApiEndpoints.DASHBOARD_KPIS)
    suspend fun getSuperAdminKpis(): Response<SuperAdminKpisDto>

    @GET(ApiEndpoints.ORGANIZATIONS)
    suspend fun getOrganizations(): Response<List<OrganizationDetailDto>>

    @GET("api/v1/health")
    suspend fun getSystemHealth(): Response<SystemHealthDto>
}
