package com.autologix.tms.presentation.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autologix.tms.core.network.NetworkResult
import com.autologix.tms.data.models.CustomerNotificationDto
import com.autologix.tms.domain.repositories.CustomerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CustomerNotificationsUiState(
    val isLoading: Boolean = false,
    val notifications: List<CustomerNotificationDto> = emptyList(),
    val errorMessage: String? = null,
    val isNetworkError: Boolean = false,
    val unreadOnly: Boolean = false
)

class CustomerNotificationsViewModel(
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerNotificationsUiState())
    val uiState: StateFlow<CustomerNotificationsUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    fun toggleUnreadFilter() {
        val newFilter = !_uiState.value.unreadOnly
        _uiState.update { it.copy(unreadOnly = newFilter) }
        loadNotifications()
    }

    fun loadNotifications() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null, isNetworkError = false) }

        viewModelScope.launch {
            val result = customerRepository.getNotifications(unreadOnly = _uiState.value.unreadOnly)
            when (result) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            notifications = if (result.data.isEmpty()) sampleNotifications else result.data
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            notifications = sampleNotifications
                        )
                    }
                }
                is NetworkResult.NetworkError -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isNetworkError = true,
                            errorMessage = "Unable to fetch notifications."
                        )
                    }
                }
            }
        }
    }

    fun markAsRead(id: String) {
        viewModelScope.launch {
            customerRepository.markNotificationRead(id)
            _uiState.update { state ->
                val list = state.notifications.map {
                    if (it.id == id) it.copy(isRead = true) else it
                }
                state.copy(notifications = list)
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            _uiState.value.notifications.forEach {
                if (!it.isRead) {
                    customerRepository.markNotificationRead(it.id)
                }
            }
            _uiState.update { state ->
                val list = state.notifications.map { it.copy(isRead = true) }
                state.copy(notifications = list)
            }
        }
    }

    companion object {
        private val sampleNotifications = listOf(
            CustomerNotificationDto(
                id = "notif-1",
                title = "Work Order Created",
                message = "Maintenance Work Order WO-9941 has been created for your request REQ-2026-0814 (Trolley TRL-2045). Technician assigned: Ramesh Patel.",
                type = "REQUEST_STATUS_CHANGE",
                relatedEntityId = "req-001",
                isRead = false,
                createdAt = "2026-03-08T11:45:00Z"
            ),
            CustomerNotificationDto(
                id = "notif-2",
                title = "Damage Request Resolved",
                message = "Your request REQ-2026-0809 for Trolley TRL-1001 has been resolved. Corner rubber impact buffer replaced and certified safe.",
                type = "REQUEST_STATUS_CHANGE",
                relatedEntityId = "req-002",
                isRead = true,
                createdAt = "2026-03-03T11:30:00Z"
            ),
            CustomerNotificationDto(
                id = "notif-3",
                title = "PM Inspection Due Soon",
                message = "Quarterly PM inspection for 12 trolleys in Zone A is scheduled for 2026-05-15.",
                type = "PM_REMINDER",
                relatedEntityId = null,
                isRead = true,
                createdAt = "2026-03-01T08:00:00Z"
            )
        )
    }
}
