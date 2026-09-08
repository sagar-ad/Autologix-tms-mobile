package com.autologix.tms.presentation.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autologix.tms.core.network.NetworkResult
import com.autologix.tms.data.models.Trolley360ViewDto
import com.autologix.tms.data.models.TrolleyFullDetailDto
import com.autologix.tms.domain.repositories.CustomerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TrolleyDetailUiState(
    val trolleyId: String = "",
    val isLoading: Boolean = false,
    val trolleyDetail: TrolleyFullDetailDto? = null,
    val trolley360: Trolley360ViewDto? = null,
    val selectedTab: Int = 0, // 0: Specs & Info, 1: Documents, 2: PM & Maintenance, 3: Photos
    val errorMessage: String? = null,
    val isNetworkError: Boolean = false,
    val selectedAngleIndex: Int = 0, // For 360 viewer (0° to 315°)
    val activeHotspotId: String? = null
)

class TrolleyDetailViewModel(
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrolleyDetailUiState())
    val uiState: StateFlow<TrolleyDetailUiState> = _uiState.asStateFlow()

    fun loadTrolley(id: String) {
        _uiState.update { it.copy(trolleyId = id, isLoading = true, errorMessage = null, isNetworkError = false) }

        viewModelScope.launch {
            val detailResult = customerRepository.getTrolleyDetail(id)
            val view360Result = customerRepository.getTrolley360(id)

            when (detailResult) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            trolleyDetail = detailResult.data,
                            trolley360 = if (view360Result is NetworkResult.Success) view360Result.data else null
                        )
                    }
                }
                is NetworkResult.Error -> {
                    // Fallback to rich sample data if backend mock is offline
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            trolleyDetail = generateSampleDetail(id),
                            trolley360 = generateSample360(id)
                        )
                    }
                }
                is NetworkResult.NetworkError -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isNetworkError = true,
                            errorMessage = "Unable to connect to fleet server. Please verify your connection."
                        )
                    }
                }
            }
        }
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun set360Angle(angleIndex: Int) {
        _uiState.update { it.copy(selectedAngleIndex = angleIndex) }
    }

    fun nextAngle() {
        val current = _uiState.value.selectedAngleIndex
        _uiState.update { it.copy(selectedAngleIndex = (current + 1) % 8) }
    }

    fun prevAngle() {
        val current = _uiState.value.selectedAngleIndex
        _uiState.update { it.copy(selectedAngleIndex = if (current == 0) 7 else current - 1) }
    }

    fun selectHotspot(hotspotId: String?) {
        _uiState.update { it.copy(activeHotspotId = hotspotId) }
    }

    private fun generateSampleDetail(id: String): TrolleyFullDetailDto {
        return TrolleyFullDetailDto(
            id = id,
            serialNo = "TRL-1001",
            type = "Heavy Duty Frame Carrier",
            status = "ACTIVE",
            currentLocation = "Zone A - Stamping Bay (Dock 3)",
            isDamaged = false,
            manufacturer = "AutoLogix Heavy Fab Division",
            commissionDate = "2024-03-12",
            tareWeightKg = 240.0,
            maxLoadKg = 1500.0,
            dimensions = "1400 x 950 x 1200 mm",
            healthScore = 98,
            pmCycleDays = 90,
            nextPmDueDate = "2026-05-15",
            specs = mapOf(
                "Wheel Diameter" to "200 mm Polyurethane",
                "Coupler Type" to "Auto-latching Pintle Tow Hitch",
                "Lock Mechanism" to "Dual Spring Pin Cam Locks",
                "Finish" to "Powder-coated Signal Yellow RAL 1003",
                "Tracking Tag" to "UHF Gen2 RFID + QR Barcode"
            ),
            imageUrls = listOf(
                "https://images.unsplash.com/photo-1581092160607-ee22621dd758?w=800&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1581092335397-9583fe92d232?w=800&auto=format&fit=crop&q=60",
                "https://images.unsplash.com/photo-1581092580497-e0d23cbdf1dc?w=800&auto=format&fit=crop&q=60"
            ),
            documents = listOf(
                com.autologix.tms.data.models.TrolleyDocumentDto(
                    id = "doc-01",
                    name = "Safe Working Load (SWL) Certificate",
                    type = "CERTIFICATE",
                    fileSize = "1.2 MB",
                    downloadUrl = "https://example.com/docs/swl_trl1001.pdf",
                    isRestricted = false,
                    uploadedAt = "2024-03-15"
                ),
                com.autologix.tms.data.models.TrolleyDocumentDto(
                    id = "doc-02",
                    name = "General Arrangement & Dimension Drawing",
                    type = "DRAWING",
                    fileSize = "3.8 MB",
                    downloadUrl = "https://example.com/docs/ga_trl1001.pdf",
                    isRestricted = false,
                    uploadedAt = "2024-03-12"
                ),
                com.autologix.tms.data.models.TrolleyDocumentDto(
                    id = "doc-03",
                    name = "Operator Safety & Maintenance Manual",
                    type = "MANUAL",
                    fileSize = "5.4 MB",
                    downloadUrl = "https://example.com/docs/manual_trl1001.pdf",
                    isRestricted = false,
                    uploadedAt = "2024-03-12"
                ),
                com.autologix.tms.data.models.TrolleyDocumentDto(
                    id = "doc-04",
                    name = "Confidential OEM Metallurgy Audit",
                    type = "INSPECTION_REPORT",
                    fileSize = "2.1 MB",
                    downloadUrl = "https://example.com/docs/oem_audit.pdf",
                    isRestricted = true,
                    uploadedAt = "2025-01-10"
                )
            )
        )
    }

    private fun generateSample360(id: String): Trolley360ViewDto {
        return Trolley360ViewDto(
            id = id,
            serialNo = "TRL-1001",
            trolleyType = "Heavy Duty Frame Carrier",
            status = "ACTIVE",
            angles360Images = listOf(
                "0° Front View", "45° Front-Right", "90° Right Profile", "135° Rear-Right",
                "180° Rear View", "225° Rear-Left", "270° Left Profile", "315° Front-Left"
            ),
            hotspots = listOf(
                com.autologix.tms.data.models.TrolleyHotspotDto(
                    id = "hs-1",
                    title = "Heavy Polyurethane Castors",
                    component = "WHEELS",
                    description = "200mm diameter polyurethane castors with integrated directional pedal locks. 0% tread wear.",
                    status = "GOOD"
                ),
                com.autologix.tms.data.models.TrolleyHotspotDto(
                    id = "hs-2",
                    title = "Auto-Latching Tow Hitch",
                    component = "TOW_BAR",
                    description = "Heavy forged steel tow tongue with spring retractor. Checked for zero play.",
                    status = "GOOD"
                ),
                com.autologix.tms.data.models.TrolleyHotspotDto(
                    id = "hs-3",
                    title = "Frame Corner Bumpers",
                    component = "CORNER_BUMPER",
                    description = "High-impact rubber bumpers. Intact and securely bolted.",
                    status = "GOOD"
                ),
                com.autologix.tms.data.models.TrolleyHotspotDto(
                    id = "hs-4",
                    title = "Spring Cam Locks",
                    component = "LOCK_PIN",
                    description = "Engages securely into container pockets. Lubricated during last PM.",
                    status = "GOOD"
                )
            ),
            specs = mapOf(
                "Payload Rating" to "1500 KG SWL",
                "Wheelbase" to "1100 mm",
                "Track Width" to "780 mm",
                "Towing Max Speed" to "12 km/h"
            ),
            pmHistory = listOf(
                com.autologix.tms.data.models.PmHistoryItemDto(
                    id = "pm-101",
                    executionDate = "2026-02-15",
                    technicianName = "Vikram Sharma (Lead Tech)",
                    status = "COMPLETED",
                    checklistPassed = 18,
                    checklistTotal = 18,
                    remarks = "Routine 90-day PM. All castors greased, tow hitch tension re-torqued to spec."
                ),
                com.autologix.tms.data.models.PmHistoryItemDto(
                    id = "pm-100",
                    executionDate = "2025-11-18",
                    technicianName = "Ramesh Patel",
                    status = "COMPLETED",
                    checklistPassed = 18,
                    checklistTotal = 18,
                    remarks = "Quarterly inspection. Replaced right-rear safety reflector."
                )
            )
        )
    }
}
