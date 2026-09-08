package com.autologix.tms.presentation.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autologix.tms.core.network.NetworkResult
import com.autologix.tms.data.models.TrolleyQuickScanDto
import com.autologix.tms.domain.repositories.CustomerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TrolleyScanUiState(
    val isScanning: Boolean = true,
    val isLoading: Boolean = false,
    val scannedBarcode: String = "",
    val manualInputBarcode: String = "",
    val isFlashlightOn: Boolean = false,
    val scannedTrolley: TrolleyQuickScanDto? = null,
    val errorMessage: String? = null,
    val isNotFound: Boolean = false,
    val isUnauthorized: Boolean = false
)

class TrolleyScanViewModel(
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrolleyScanUiState())
    val uiState: StateFlow<TrolleyScanUiState> = _uiState.asStateFlow()

    fun onBarcodeDetected(barcode: String) {
        if (_uiState.value.isLoading) return
        processBarcode(barcode)
    }

    fun onManualInputChanged(value: String) {
        _uiState.update { it.copy(manualInputBarcode = value, errorMessage = null) }
    }

    fun submitManualBarcode() {
        val code = _uiState.value.manualInputBarcode.trim()
        if (code.isNotBlank()) {
            processBarcode(code)
        }
    }

    fun toggleFlashlight() {
        _uiState.update { it.copy(isFlashlightOn = !it.isFlashlightOn) }
    }

    fun resetScanner() {
        _uiState.update {
            it.copy(
                isScanning = true,
                isLoading = false,
                scannedBarcode = "",
                scannedTrolley = null,
                errorMessage = null,
                isNotFound = false,
                isUnauthorized = false
            )
        }
    }

    private fun processBarcode(barcode: String) {
        _uiState.update {
            it.copy(
                isScanning = false,
                isLoading = true,
                scannedBarcode = barcode,
                errorMessage = null,
                isNotFound = false,
                isUnauthorized = false
            )
        }

        viewModelScope.launch {
            // Call GET /api/v1/trolleys/scan/{barcode}
            val result = customerRepository.scanTrolley(barcode)
            when (result) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            scannedTrolley = result.data,
                            errorMessage = null
                        )
                    }
                }
                is NetworkResult.Error -> {
                    val isAuth = result.statusCode == 403 || result.statusCode == 401
                    val is404 = result.statusCode == 404
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = if (isAuth) "Unauthorized: You do not have permission to access this trolley in your organization."
                            else if (is404) "Trolley with identifier '$barcode' was not found in the fleet database."
                            else result.message,
                            isNotFound = is404,
                            isUnauthorized = isAuth
                        )
                    }
                }
                is NetworkResult.NetworkError -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Network communication error while validating barcode. Please check connection and retry."
                        )
                    }
                }
            }
        }
    }
}
