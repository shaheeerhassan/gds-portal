package com.school.gdsportal.ui.admin.communication.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.BulkNotificationsRequest
import com.school.gdsportal.data.remote.Notification
import com.school.gdsportal.data.remote.User
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class NotificationRecipientMode {
    SINGLE,
    BULK_ROLE
}

data class NotificationCreateUiState(
    val recipientMode: NotificationRecipientMode = NotificationRecipientMode.SINGLE,
    val selectedRoleId: Int? = null,
    val users: List<User> = emptyList(),
    val isLoadingUsers: Boolean = false,
    val selectedUserId: Long? = null,
    val selectedType: String = "NEW_ANNOUNCEMENT",
    val title: String = "",
    val message: String = "",
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

class NotificationCreateViewModel(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationCreateUiState())
    val uiState: StateFlow<NotificationCreateUiState> = _uiState.asStateFlow()

    fun onRecipientModeChanged(mode: NotificationRecipientMode) {
        _uiState.value = _uiState.value.copy(
            recipientMode = mode,
            selectedRoleId = null,
            selectedUserId = null,
            users = emptyList()
        )
    }

    fun onRoleSelected(roleId: Int?) {
        _uiState.value = _uiState.value.copy(
            selectedRoleId = roleId,
            selectedUserId = null,
            users = emptyList()
        )
        if (roleId != null) {
            loadUsersForRole(roleId)
        }
    }

    private fun loadUsersForRole(roleId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingUsers = true)
            try {
                val res = apiService.getUsersByRole(roleId)
                if (res.isSuccessful) {
                    val userList = res.body()?.data ?: emptyList()
                    _uiState.value = _uiState.value.copy(
                        isLoadingUsers = false,
                        users = userList
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoadingUsers = false,
                        error = "Failed to load users for selected role."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingUsers = false,
                    error = "Network error: ${e.localizedMessage ?: "Unable to connect."}"
                )
            }
        }
    }

    fun onUserSelected(userId: Long?) {
        _uiState.value = _uiState.value.copy(selectedUserId = userId)
    }

    fun onTypeSelected(type: String) {
        _uiState.value = _uiState.value.copy(selectedType = type)
    }

    fun onTitleChanged(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun onMessageChanged(message: String) {
        _uiState.value = _uiState.value.copy(message = message)
    }

    fun submit() {
        val state = _uiState.value

        if (state.title.isBlank()) {
            _uiState.value = state.copy(error = "Title is required.")
            return
        }
        if (state.message.isBlank()) {
            _uiState.value = state.copy(error = "Message is required.")
            return
        }

        when (state.recipientMode) {
            NotificationRecipientMode.SINGLE -> {
                if (state.selectedUserId == null || state.selectedUserId <= 0) {
                    _uiState.value = state.copy(error = "Please select a recipient.")
                    return
                }
                sendSingleNotification(state.selectedUserId, state.selectedType, state.title.trim(), state.message.trim())
            }
            NotificationRecipientMode.BULK_ROLE -> {
                if (state.selectedRoleId == null) {
                    _uiState.value = state.copy(error = "Please select a target role.")
                    return
                }
                if (state.users.isEmpty()) {
                    _uiState.value = state.copy(error = "No users found for the selected role.")
                    return
                }
                sendBulkRoleNotifications(state.users, state.selectedType, state.title.trim(), state.message.trim())
            }
        }
    }

    private fun sendSingleNotification(userId: Long, type: String, title: String, message: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, error = null)
            try {
                val notification = Notification(
                    userId = userId,
                    notificationType = type,
                    title = title,
                    message = message
                )
                val res = apiService.createNotification(notification)
                if (res.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isSubmitting = false, isSuccess = true)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        error = "Failed to send notification."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    error = "Network error: ${e.localizedMessage ?: "Unable to connect."}"
                )
            }
        }
    }

    private fun sendBulkRoleNotifications(users: List<User>, type: String, title: String, message: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, error = null)
            try {
                val notifications = users.map { user ->
                    Notification(
                        userId = user.userId,
                        notificationType = type,
                        title = title,
                        message = message
                    )
                }
                val request = BulkNotificationsRequest(notifications = notifications)
                val res = apiService.createBulkNotifications(request)
                if (res.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isSubmitting = false, isSuccess = true)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        error = "Failed to send bulk notifications."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    error = "Network error: ${e.localizedMessage ?: "Unable to connect."}"
                )
            }
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetSuccess() {
        _uiState.value = _uiState.value.copy(isSuccess = false)
    }

    class Factory(private val apiService: ApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NotificationCreateViewModel(apiService) as T
        }
    }
}
