package com.autologix.tms.domain.repositories

import com.autologix.tms.core.network.NetworkResult
import com.autologix.tms.data.models.CreateWorkOrderRequestDto
import com.autologix.tms.data.models.DamageReportDto
import com.autologix.tms.data.models.MaintenanceManagerKpisDto
import com.autologix.tms.data.models.ManagerReviewRequestDto
import com.autologix.tms.data.models.MediaUploadResponseDto
import com.autologix.tms.data.models.PmScheduleDto
import com.autologix.tms.data.models.TechnicianDto
import com.autologix.tms.data.models.WorkOrderActionRequestDto
import com.autologix.tms.data.models.WorkOrderDto

interface MaintenanceRepository {
    suspend fun getMaintenanceKpis(): NetworkResult<MaintenanceManagerKpisDto>
    suspend fun getPmSchedules(status: String? = null, technicianId: String? = null): NetworkResult<List<PmScheduleDto>>
    suspend fun getAllWorkOrders(status: String? = null, priority: String? = null): NetworkResult<List<WorkOrderDto>>
    suspend fun getMyWorkOrders(status: String? = null): NetworkResult<List<WorkOrderDto>>
    suspend fun createOrAssignWorkOrder(request: CreateWorkOrderRequestDto): NetworkResult<WorkOrderDto>
    suspend fun performWorkOrderAction(id: String, request: WorkOrderActionRequestDto): NetworkResult<WorkOrderDto>
    suspend fun reviewWorkOrder(id: String, request: ManagerReviewRequestDto): NetworkResult<WorkOrderDto>
    suspend fun getTechnicians(availableOnly: Boolean? = null): NetworkResult<List<TechnicianDto>>
    suspend fun getCustomerDamageRequests(status: String? = null): NetworkResult<List<DamageReportDto>>
    suspend fun uploadPhoto(bytes: ByteArray, fileName: String, mimeType: String): NetworkResult<MediaUploadResponseDto>
}
