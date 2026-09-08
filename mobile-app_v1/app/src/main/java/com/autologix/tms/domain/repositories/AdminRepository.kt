package com.autologix.tms.domain.repositories

import com.autologix.tms.core.network.NetworkResult
import com.autologix.tms.data.models.OrganizationDetailDto
import com.autologix.tms.data.models.SuperAdminKpisDto
import com.autologix.tms.data.models.SystemHealthDto

interface AdminRepository {
    suspend fun getSuperAdminKpis(): NetworkResult<SuperAdminKpisDto>
    suspend fun getOrganizations(): NetworkResult<List<OrganizationDetailDto>>
    suspend fun getSystemHealth(): NetworkResult<SystemHealthDto>
}
