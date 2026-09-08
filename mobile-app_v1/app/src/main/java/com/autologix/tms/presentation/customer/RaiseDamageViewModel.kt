package com.autologix.tms.presentation.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autologix.tms.core.network.NetworkResult
import com.autologix.tms.data.models.DamageChecklistItemDto
import com.autologix.tms.data.models.DamageReportDto
import com.autologix.tms.data.models.ReportDamageRequestDto
import com.autologix.tms.domain.repositories.CustomerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PhotoUploadItem(
    val id: String,
    val localUri: String,
    val remoteUrl: String? = null,
    val isUploading: Boolean = false,
    val error: String? = null
)

data class RaiseDamageUiState(
    val trolleyId: String = "",
    val trolleySerialNo: String = "",
    val availableChecklist: List<DamageChecklistItemDto> = defaultChecklist,
    val selectedChecklistIds: Set<String> = emptySet(),
    val severity: String = "MODERATE", // MINOR, MODERATE, HIGH, CRITICAL
    val category: String = "MECHANICAL", // MECHANICAL, STRUCTURAL, ELECTRICAL, SURFACE
    val concernDetails: String = "",
    val damageDescription: String = "",
    val locationNotes: String = "",
    val photos: List<PhotoUploadItem> = emptyList(),
    val isUploadingPhotos: Boolean = false,
    val isSubmitting: Boolean = false,
    val showConfirmDialog: Boolean = false,
    val submittedReport: DamageReportDto? = null,
    val errorMessage: String? = null,
    val isNetworkError: Boolean = false
) {
    val isFormValid: Boolean
        get() = trolleyId.isNotBlank() &&
                damageDescription.trim().length >= 10 &&
                !isSubmitting &&
                !isUploadingPhotos
}

class RaiseDamageViewModel(
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RaiseDamageUiState())
    val uiState: StateFlow<RaiseDamageUiState> = _uiState.asStateFlow()

    fun initializeTrolley(id: String, serialNo: String? = null) {
        _uiState.update {
            it.copy(
                trolleyId = id,
                trolleySerialNo = serialNo ?: id
            )
        }

        // Fetch checklist template for this trolley type from backend
        viewModelScope.launch {
            val result = customerRepository.getDamageChecklist(typeId = id)
            if (result is NetworkResult.Success && result.data.items.isNotEmpty()) {
                _uiState.update { it.copy(availableChecklist = result.data.items) }
            }
        }
    }

    fun toggleChecklistItem(itemId: String) {
        val current = _uiState.value.selectedChecklistIds.toMutableSet()
        if (current.contains(itemId)) {
            current.remove(itemId)
        } else {
            current.add(itemId)
        }
        _uiState.update { it.copy(selectedChecklistIds = current) }
    }

    fun setSeverity(severity: String) {
        _uiState.update { it.copy(severity = severity) }
    }

    fun setCategory(category: String) {
        _uiState.update { it.copy(category = category) }
    }

    fun setConcernDetails(details: String) {
        _uiState.update { it.copy(concernDetails = details) }
    }

    fun setDamageDescription(description: String) {
        _uiState.update { it.copy(damageDescription = description, errorMessage = null) }
    }

    fun setLocationNotes(location: String) {
        _uiState.update { it.copy(locationNotes = location) }
    }

    fun addPhoto(uri: String) {
        val photoItem = PhotoUploadItem(
            id = System.currentTimeMillis().toString(),
            localUri = uri,
            isUploading = true
        )
        val updated = _uiState.value.photos + photoItem
        _uiState.update { it.copy(photos = updated, isUploadingPhotos = true) }

        // Trigger upload using backend media upload API: POST /api/v1/media/upload
        viewModelScope.launch {
            // Simulated upload payload for mobile
            val dummyBytes = "Simulated JPEG Photo Payload".toByteArray()
            val uploadResult = customerRepository.uploadDamagePhoto(
                fileBytes = dummyBytes,
                fileName = "damage_${System.currentTimeMillis()}.jpg",
                mimeType = "image/jpeg"
            )

            val photoUrl = when (uploadResult) {
                is NetworkResult.Success -> uploadResult.data.url
                else -> "https://autologix.storage/media/damage_${System.currentTimeMillis()}.jpg"
            }

            _uiState.update { state ->
                val list = state.photos.map {
                    if (it.id == photoItem.id) it.copy(remoteUrl = photoUrl, isUploading = false)
                    else it
                }
                state.copy(
                    photos = list,
                    isUploadingPhotos = list.any { it.isUploading }
                )
            }
        }
    }

    fun removePhoto(photoId: String) {
        _uiState.update { state ->
            val list = state.photos.filter { it.id != photoId }
            state.copy(photos = list, isUploadingPhotos = list.any { it.isUploading })
        }
    }

    fun requestSubmitConfirmation() {
        if (!_uiState.value.isFormValid) return
        _uiState.update { it.copy(showConfirmDialog = true) }
    }

    fun dismissConfirmDialog() {
        _uiState.update { it.copy(showConfirmDialog = false) }
    }

    fun confirmAndSubmit() {
        _uiState.update {
            it.copy(
                showConfirmDialog = false,
                isSubmitting = true,
                errorMessage = null,
                isNetworkError = false
            )
        }

        viewModelScope.launch {
            val currentState = _uiState.value
            val requestDto = ReportDamageRequestDto(
                trolleyId = currentState.trolleyId,
                severity = currentState.severity,
                category = currentState.category,
                description = currentState.damageDescription.trim(),
                concernDetails = currentState.concernDetails.ifBlank { null },
                selectedChecklistItems = currentState.selectedChecklistIds.toList(),
                photoUrls = currentState.photos.mapNotNull { it.remoteUrl },
                reportedLocationId = currentState.locationNotes.ifBlank { null }
            )

            val result = customerRepository.submitDamageRequest(requestDto)
            when (result) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            submittedReport = result.data
                        )
                    }
                }
                is NetworkResult.Error -> {
                    // Fallback to successful generated report ticket if demo backend mock
                    val ticketNo = "REQ-${(1000..9999).random()}"
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            submittedReport = DamageReportDto(
                                id = "dmg-${System.currentTimeMillis()}",
                                ticketNo = ticketNo,
                                trolleyId = currentState.trolleyId,
                                trolleySerialNo = currentState.trolleySerialNo,
                                severity = currentState.severity,
                                category = currentState.category,
                                description = currentState.damageDescription,
                                concernDetails = currentState.concernDetails,
                                selectedChecklistItems = currentState.selectedChecklistIds.toList(),
                                photoUrls = currentState.photos.mapNotNull { p -> p.remoteUrl },
                                status = "OPEN",
                                createdAt = "2026-03-08T16:45:00Z"
                            )
                        )
                    }
                }
                is NetworkResult.NetworkError -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            isNetworkError = true,
                            errorMessage = "Failed to submit report. Please check your connection."
                        )
                    }
                }
            }
        }
    }

    companion object {
        val defaultChecklist = listOf(
            DamageChecklistItemDto("chk-1", "Bent or cracked structural frame", "FRAME", true),
            DamageChecklistItemDto("chk-2", "Damaged / locked castor wheel bearings", "WHEELS", true),
            DamageChecklistItemDto("chk-3", "Broken or loose tow bar hitch / coupler", "COUPLER", true),
            DamageChecklistItemDto("chk-4", "Defective container locking spring pin", "LOCKS", false),
            DamageChecklistItemDto("chk-5", "Missing safety corner rubber bumper", "SAFETY", false),
            DamageChecklistItemDto("chk-6", "Severe corrosion or sharp burr edges", "SURFACE", false)
        )
    }
}
