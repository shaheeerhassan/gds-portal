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

enum class NotificationFilter {
    ALL,
    UNREAD
}

data class NotificationsDirectoryUiState(
    val isLoading: Boolean = false,
    val isActionLoading: Boolean = false,
    val notifications: List<Notification> = emptyList(),
    val filteredNotifications: List<Notification> = emptyList(),
    val unreadCount: Int = 0,
    val selectedFilter: NotificationFilter = NotificationFilter.ALL,
    val searchQuery: String = "",
    val showClearAllDialog: Boolean = false,
    val error: String? = null
)

class NotificationsDirectoryViewModel(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsDirectoryUiState())
    val uiState: StateFlow<NotificationsDirectoryUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // Fetch unread count
                val countRes = apiService.getUnreadNotificationCount()
                val count = if (countRes.isSuccessful) countRes.body()?.data ?: 0 else 0

                // Fetch list based on filter
                val listRes = if (_uiState.value.selectedFilter == NotificationFilter.UNREAD) {
                    apiService.getUnreadNotifications()
                } else {
                    apiService.getMyNotifications()
                }

                if (listRes.isSuccessful) {
                    val list = listRes.body()?.data ?: emptyList()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        notifications = list,
                        unreadCount = count
                    )
                    applyFilter()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        unreadCount = count,
                        error = "Failed to load notifications."
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

    fun setFilter(filter: NotificationFilter) {
        if (_uiState.value.selectedFilter == filter) return
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
        loadData()
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilter()
    }

    private fun applyFilter() {
        val query = _uiState.value.searchQuery.trim().lowercase()
        val baseList = _uiState.value.notifications
        val result = if (query.isBlank()) {
            baseList
        } else {
            baseList.filter {
                it.title.lowercase().contains(query) ||
                it.message.lowercase().contains(query) ||
                it.notificationType.lowercase().contains(query)
            }
        }
        _uiState.value = _uiState.value.copy(filteredNotifications = result)
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActionLoading = true, error = null)
            try {
                val res = apiService.markAllNotificationsAsRead()
                if (res.isSuccessful) {
                    loadData()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isActionLoading = false,
                        error = "Failed to mark all as read."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isActionLoading = false,
                    error = "Network error: ${e.localizedMessage ?: "Unable to connect."}"
                )
            }
        }
    }

    fun showClearAllDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showClearAllDialog = show)
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActionLoading = true, showClearAllDialog = false, error = null)
            try {
                val res = apiService.deleteAllMyNotifications()
                if (res.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isActionLoading = false,
                        notifications = emptyList(),
                        filteredNotifications = emptyList(),
                        unreadCount = 0
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isActionLoading = false,
                        error = "Failed to clear notifications."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isActionLoading = false,
                    error = "Network error: ${e.localizedMessage ?: "Unable to connect."}"
                )
            }
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    class Factory(private val apiService: ApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NotificationsDirectoryViewModel(apiService) as T
        }
    }
}
