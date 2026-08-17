package com.school.gdsportal.ui.admin.communication.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.Notification
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NotificationDetailUiState(
    val isLoading: Boolean = false,
    val isDeleting: Boolean = false,
    val notification: Notification? = null,
    val isDeleted: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val error: String? = null
)

class NotificationDetailViewModel(
    private val apiService: ApiService,
    private val notificationId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationDetailUiState())
    val uiState: StateFlow<NotificationDetailUiState> = _uiState.asStateFlow()

    init {
        loadNotification()
    }

    fun loadNotification() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val res = apiService.getNotificationById(notificationId)
                if (res.isSuccessful) {
                    val notif = res.body()?.data
                    if (notif != null) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            notification = notif
                        )
                        // If unread, mark as read
                        if (!notif.isRead) {
                            markAsRead()
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Notification not found."
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load notification."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Network error: ${e.localizedMessage ?: "Unable to connect."}"
                )
            }
        }
    }

    private fun markAsRead() {
        viewModelScope.launch {
            try {
                val res = apiService.markNotificationAsRead(notificationId)
                if (res.isSuccessful) {
                    val current = _uiState.value.notification
                    if (current != null) {
                        _uiState.value = _uiState.value.copy(
                            notification = current.copy(isRead = true)
                        )
                    }
                }
            } catch (_: Exception) {
                // Silently ignore mark-as-read failures in background
            }
        }
    }

    fun showDeleteDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showDeleteDialog = show)
    }

    fun deleteNotification() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true, showDeleteDialog = false, error = null)
            try {
                val res = apiService.deleteNotificationById(notificationId)
                if (res.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isDeleting = false, isDeleted = true)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        error = "Failed to delete notification."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    error = "Network error: ${e.localizedMessage ?: "Unable to connect."}"
                )
            }
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    class Factory(
        private val apiService: ApiService,
        private val notificationId: Long
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NotificationDetailViewModel(apiService, notificationId) as T
        }
    }
}
