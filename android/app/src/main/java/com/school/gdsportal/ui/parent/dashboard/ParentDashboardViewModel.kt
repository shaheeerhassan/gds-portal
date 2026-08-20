package com.school.gdsportal.ui.parent.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.local.TokenManager
import com.school.gdsportal.data.remote.Announcement
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

data class ParentDashboardUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val parentFirstName: String = "Parent",
    val childrenCount: Int = 0,
    val unreadNotifications: Int = 0,
    val recentAnnouncements: List<Announcement> = emptyList()
)

class ParentDashboardViewModel(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ParentDashboardUiState())
    val uiState: StateFlow<ParentDashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard(isRefresh = false)
    }

    fun refreshDashboard() {
        loadDashboard(isRefresh = true)
    }

    private fun loadDashboard(isRefresh: Boolean) {
        _uiState.update {
            if (isRefresh) it.copy(isRefreshing = true, error = null)
            else it.copy(isLoading = true, error = null)
        }

        viewModelScope.launch {
            try {
                // 1. Fetch user locally for the fast UI greeting
                val savedUser = tokenManager.getUserProfile()
                val firstName = savedUser?.firstName ?: "Parent"

                // 2. Fetch the Parent profile from the backend to securely get the parentId
                val parentMeRes = apiService.getCurrentParent()
                val parentId = parentMeRes.body()?.data?.parentId ?: 0L

                // 3. Fetch Dashboard data concurrently
                val announcementsDef = async { apiService.getGlobalAnnouncements() }
                val notificationsDef = async { apiService.getUnreadNotificationCount() }

                // 4. Fetch children count if we successfully got the parentId
                var childCount = 0
                if (parentId != 0L) {
                    val childrenRes = apiService.getStudentsByParentId(parentId)
                    if (childrenRes.isSuccessful) {
                        childCount = childrenRes.body()?.data?.size ?: 0
                    }
                }

                val announcementsRes = announcementsDef.await()
                val notificationsRes = notificationsDef.await()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        parentFirstName = firstName,
                        childrenCount = childCount,
                        unreadNotifications = notificationsRes.body()?.data ?: 0,
                        recentAnnouncements = announcementsRes.body()?.data?.take(3) ?: emptyList()
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = "Unable to load dashboard data."
                    )
                }
            }
        }
    }

    companion object {
        fun provideFactory(apiService: ApiService, tokenManager: TokenManager): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ParentDashboardViewModel(apiService, tokenManager) as T
                }
            }
    }
}