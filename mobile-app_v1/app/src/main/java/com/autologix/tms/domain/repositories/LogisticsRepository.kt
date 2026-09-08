package com.autologix.tms.domain.repositories

import com.autologix.tms.core.network.NetworkResult
import com.autologix.tms.data.models.GateInRequestDto
import com.autologix.tms.data.models.GateOutRequestDto
import com.autologix.tms.data.models.LogisticsKpisDto
import com.autologix.tms.data.models.MediaUploadResponseDto
import com.autologix.tms.data.models.MovementRecordDto

interface LogisticsRepository {
    suspend fun getLogisticsKpis(): NetworkResult<LogisticsKpisDto>
    suspend fun recordGateIn(request: GateInRequestDto): NetworkResult<MovementRecordDto>
    suspend fun recordGateOut(request: GateOutRequestDto): NetworkResult<MovementRecordDto>
    suspend fun getMovementHistory(movementType: String? = null, trolleyId: String? = null): NetworkResult<List<MovementRecordDto>>
    suspend fun uploadPhoto(bytes: ByteArray, fileName: String, mimeType: String): NetworkResult<MediaUploadResponseDto>
}
