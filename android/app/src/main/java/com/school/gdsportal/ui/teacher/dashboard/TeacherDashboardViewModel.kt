package com.school.gdsportal.ui.teacher.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.local.TokenManager
import com.school.gdsportal.data.remote.AcademicYear
import com.school.gdsportal.data.remote.Announcement
import com.school.gdsportal.data.remote.User
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TeacherDashboardUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val user: User? = null,
    val currentAcademicYear: AcademicYear? = null,
    val totalAssignedClasses: Int = 0,
    val totalAssignedSubjects: Int = 0,
    val unreadNotifications: Int = 0,
    val globalAnnouncements: List<Announcement> = emptyList()
)

class TeacherDashboardViewModel(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherDashboardUiState())
    val uiState: StateFlow<TeacherDashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData(isRefresh = false)
    }

    fun refreshDashboard() {
        loadDashboardData(isRefresh = true)
    }

    private fun loadDashboardData(isRefresh: Boolean) {
        _uiState.update {
            if (isRefresh) it.copy(isRefreshing = true, error = null)
            else it.copy(isLoading = true, error = null)
        }

        viewModelScope.launch {
            try {
                val savedUser = tokenManager.getUserProfile()
                val teacherId = apiService.getTeacherMe().body()?.data?.teacherId ?: 0L

                val academicYearDef = async { apiService.getCurrentAcademicYear() }
                val announcementsDef = async { apiService.getGlobalAnnouncements() }
                val notificationsDef = async { apiService.getUnreadNotificationCount() }

                val academicYearRes = academicYearDef.await()
                val announcementsRes = announcementsDef.await()
                val notificationsRes = notificationsDef.await()
                
                val currentYear = academicYearRes.body()?.data
                
                var classesCount = 0
                var subjectsCount = 0
                
                if (currentYear != null && teacherId != 0L) {
                    val classesRes = apiService.getTeacherClasses(teacherId, currentYear.academicYearId)
                    val subjectsRes = apiService.getTeacherSubjects(teacherId, currentYear.academicYearId)
                    classesCount = if (classesRes.isSuccessful) classesRes.body()?.data?.size ?: 0 else 0
                    subjectsCount = if (subjectsRes.isSuccessful) subjectsRes.body()?.data?.size ?: 0 else 0
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        user = savedUser,
                        currentAcademicYear = currentYear,
                        totalAssignedClasses = classesCount,
                        totalAssignedSubjects = subjectsCount,
                        globalAnnouncements = announcementsRes.body()?.data ?: emptyList(),
                        unreadNotifications = notificationsRes.body()?.data ?: 0
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = "Unable to load dashboard data. Swipe down to refresh."
                    )
                }
            }
        }
    }

    companion object {
        fun provideFactory(
            apiService: ApiService,
            tokenManager: TokenManager
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TeacherDashboardViewModel(apiService, tokenManager) as T
            }
        }
    }
}

