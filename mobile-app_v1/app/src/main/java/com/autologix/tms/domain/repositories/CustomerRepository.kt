package com.autologix.tms.domain.repositories

import com.autologix.tms.core.network.NetworkResult
import com.autologix.tms.data.models.CustomerDashboardKpiDto
import com.autologix.tms.data.models.CustomerFleetReportDto
import com.autologix.tms.data.models.DamageChecklistTemplateDto
import com.autologix.tms.data.models.DamageReportDto
import com.autologix.tms.data.models.MediaUploadResponseDto
import com.autologix.tms.data.models.NotificationItemDto
import com.autologix.tms.data.models.ReportDamageRequestDto
import com.autologix.tms.data.models.Trolley360ViewDto
import com.autologix.tms.data.models.TrolleyFullDetailDto
import com.autologix.tms.data.models.TrolleyQuickScanDto
import com.autologix.tms.data.models.TrolleySummaryDto

interface CustomerRepository {
    suspend fun getCustomerDashboardKpis(): NetworkResult<CustomerDashboardKpiDto>
    suspend fun getTrolleys(search: String? = null, status: String? = null): NetworkResult<List<TrolleySummaryDto>>
    suspend fun scanTrolley(barcode: String): NetworkResult<TrolleyQuickScanDto>
    suspend fun getTrolleyDetail(id: String): NetworkResult<TrolleyFullDetailDto>
    suspend fun getTrolley360(id: String): NetworkResult<Trolley360ViewDto>
    suspend fun getDamageChecklist(typeId: String): NetworkResult<DamageChecklistTemplateDto>
    suspend fun uploadDamagePhoto(fileBytes: ByteArray, fileName: String, mimeType: String): NetworkResult<MediaUploadResponseDto>
    suspend fun submitDamageRequest(request: ReportDamageRequestDto): NetworkResult<DamageReportDto>
    suspend fun getCustomerDamageRequests(status: String? = null): NetworkResult<List<DamageReportDto>>
    suspend fun getDamageRequestDetail(id: String): NetworkResult<DamageReportDto>
    suspend fun getNotifications(unreadOnly: Boolean = false): NetworkResult<List<NotificationItemDto>>
    suspend fun markNotificationAsRead(id: String): NetworkResult<NotificationItemDto>
    suspend fun getCustomerFleetReport(): NetworkResult<CustomerFleetReportDto>
}
