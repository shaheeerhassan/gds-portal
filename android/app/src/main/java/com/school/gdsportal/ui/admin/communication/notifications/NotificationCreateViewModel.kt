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
    BROADCAST
}

data class NotificationCreateUiState(
    val recipientMode: NotificationRecipientMode = NotificationRecipientMode.SINGLE,
    val selectedRoleId: Int? = null, // Used to decide which API to hit for search in SINGLE mode
    val searchQuery: String = "",
    val searchResults: List<Pair<Long, String>> = emptyList(), // userId to displayName
    val isLoadingUsers: Boolean = false,
    val selectedUserId: Long? = null,
    val selectedUserName: String? = null,
    val selectedType: String = "NEW_ANNOUNCEMENT",
    val title: String = "",
    val message: String = "",
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    
    // For BROADCAST mode
    val broadcastGlobal: Boolean = false,
    val broadcastSelectedRoleIds: Set<Int> = emptySet()
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
            selectedUserName = null,
            searchQuery = "",
            searchResults = emptyList(),
            broadcastGlobal = false,
            broadcastSelectedRoleIds = emptySet()
        )
    }

    fun onRoleSelected(roleId: Int?) {
        _uiState.value = _uiState.value.copy(
            selectedRoleId = roleId,
            selectedUserId = null,
            selectedUserName = null,
            searchQuery = "",
            searchResults = emptyList()
        )
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            selectedUserId = null,
            selectedUserName = null
        )
        if (query.length >= 2) {
            performSearch(query)
        } else {
            _uiState.value = _uiState.value.copy(searchResults = emptyList())
        }
    }

    private fun performSearch(query: String) {
        val roleId = _uiState.value.selectedRoleId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingUsers = true)
            try {
                // Roles: 3=Teacher, 4=Student
                if (roleId == 3) {
                    val res = apiService.searchTeachers(query)
                    if (res.isSuccessful) {
                        val teachers = res.body()?.data ?: emptyList()
                        _uiState.value = _uiState.value.copy(
                            isLoadingUsers = false,
                            searchResults = teachers.map { Pair(it.userId, "${it.firstName} ${it.lastName}") }
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(isLoadingUsers = false, searchResults = emptyList())
                    }
                } else if (roleId == 4) {
                    val res = apiService.searchStudents(query)
                    if (res.isSuccessful) {
                        val students = res.body()?.data ?: emptyList()
                        _uiState.value = _uiState.value.copy(
                            isLoadingUsers = false,
                            searchResults = students.map { Pair(it.userId, "${it.firstName} ${it.lastName}") }
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(isLoadingUsers = false, searchResults = emptyList())
                    }
                } else {
                    _uiState.value = _uiState.value.copy(isLoadingUsers = false, searchResults = emptyList())
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoadingUsers = false, searchResults = emptyList())
            }
        }
    }

    fun onUserSelected(userId: Long?, userName: String?) {
        _uiState.value = _uiState.value.copy(
            selectedUserId = userId,
            selectedUserName = userName,
            searchQuery = userName ?: "",
            searchResults = emptyList()
        )
    }

    fun onBroadcastGlobalChanged(isGlobal: Boolean) {
        _uiState.value = _uiState.value.copy(
            broadcastGlobal = isGlobal,
            broadcastSelectedRoleIds = if (isGlobal) emptySet() else _uiState.value.broadcastSelectedRoleIds
        )
    }

    fun onBroadcastRoleToggled(roleId: Int) {
        val current = _uiState.value.broadcastSelectedRoleIds.toMutableSet()
        if (current.contains(roleId)) {
            current.remove(roleId)
        } else {
            current.add(roleId)
        }
        _uiState.value = _uiState.value.copy(
            broadcastSelectedRoleIds = current,
            broadcastGlobal = false
        )
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
                    _uiState.value = state.copy(error = "Please search and select a recipient.")
                    return
                }
                sendSingleNotification(state.selectedUserId, state.selectedType, state.title.trim(), state.message.trim())
            }
            NotificationRecipientMode.BROADCAST -> {
                if (!state.broadcastGlobal && state.broadcastSelectedRoleIds.isEmpty()) {
                    _uiState.value = state.copy(error = "Please select 'Global' or at least one target role.")
                    return
                }
                sendBroadcastNotification(
                    state.broadcastGlobal,
                    state.broadcastSelectedRoleIds.toList(),
                    state.selectedType,
                    state.title.trim(),
                    state.message.trim()
                )
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

    private fun sendBroadcastNotification(isGlobal: Boolean, roleIds: List<Int>, type: String, title: String, message: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, error = null)
            try {
                val notification = Notification(
                    notificationType = type,
                    title = title,
                    message = message
                )
                val request = com.school.gdsportal.data.remote.BroadcastNotificationRequest(
                    global = isGlobal,
                    targetRoleIds = roleIds,
                    notification = notification
                )
                val res = apiService.broadcastNotification(request)
                if (res.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isSubmitting = false, isSuccess = true)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        error = "Failed to send broadcast notifications."
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
