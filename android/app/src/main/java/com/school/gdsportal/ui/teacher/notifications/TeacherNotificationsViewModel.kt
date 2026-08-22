package com.school.gdsportal.ui.teacher.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.Notification
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TeacherNotificationsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val notifications: List<Notification> = emptyList()
)

class TeacherNotificationsViewModel(private val apiService: ApiService) : ViewModel() {
    private val _uiState = MutableStateFlow(TeacherNotificationsUiState())
    val uiState: StateFlow<TeacherNotificationsUiState> = _uiState.asStateFlow()

    init { loadData() }

    fun loadData() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val response = apiService.getMyNotifications()
                if (response.isSuccessful) {
                    val list = response.body()?.data ?: emptyList()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            notifications = list.sortedByDescending { n -> n.createdAt }
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load notifications.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Network error.") }
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                apiService.markAllNotificationsAsRead()
                loadData()
            } catch (e: Exception) {
                // Ignore for now
            }
        }
    }

    fun markAsRead(notificationId: Long) {
        viewModelScope.launch {
            try {
                apiService.markNotificationAsRead(notificationId)
                loadData()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    companion object {
        fun provideFactory(apiService: ApiService): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TeacherNotificationsViewModel(apiService) as T
                }
            }
    }
}
