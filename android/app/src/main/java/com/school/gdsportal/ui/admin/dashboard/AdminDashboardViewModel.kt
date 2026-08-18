package com.school.gdsportal.ui.admin.dashboard

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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AdminDashboardUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false, // Added for pull-to-refresh
    val error: String? = null,
    val user: User? = null,
    val currentAcademicYear: AcademicYear? = null,
    val totalStudents: Int = 0,
    val presentStudents: Int = 0,
    val totalTeachers: Int = 0,
    val presentTeachers: Int = 0,
    val unreadNotifications: Int = 0,
    val totalClasses: Int = 0,
    val totalSections: Int = 0,
    val totalSubjects: Int = 0,
    val globalAnnouncements: List<Announcement> = emptyList()
)

class AdminDashboardViewModel(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

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
                val todayString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                // Fetch everything concurrently
                val userDef = async { apiService.getCurrentUser() }
                val academicYearDef = async { apiService.getCurrentAcademicYear() }
                val totalStudentsDef = async { apiService.getTotalStudentCount() }
                val presentStudentsDef = async { apiService.getPresentStudentCount() }
                val totalTeachersDef = async { apiService.getTotalTeacherCount() }
                val teacherAttendanceDef = async { apiService.getTeacherAttendanceByDate(todayString) }
                val announcementsDef = async { apiService.getGlobalAnnouncements() }
                val notificationsDef = async { apiService.getUnreadNotificationCount() }
                val classesDef = async { apiService.getClassesCount() }
                val sectionsDef = async { apiService.getSectionsCount() }
                val subjectsDef = async { apiService.getSubjectsCount() }

                val userRes = userDef.await()
                val academicYearRes = academicYearDef.await()
                val totalStudentsRes = totalStudentsDef.await()
                val presentStudentsRes = presentStudentsDef.await()
                val totalTeachersRes = totalTeachersDef.await()
                val teacherAttendanceRes = teacherAttendanceDef.await()
                val announcementsRes = announcementsDef.await()
                val notificationsRes = notificationsDef.await()
                val classesRes = classesDef.await()
                val sectionsRes = sectionsDef.await()
                val subjectsRes = subjectsDef.await()

                val presentTeachersCount = if (teacherAttendanceRes.isSuccessful) {
                    teacherAttendanceRes.body()?.data?.count { it.status.equals("PRESENT", ignoreCase = true) } ?: 0
                } else 0

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        user = userRes.body()?.data,
                        currentAcademicYear = academicYearRes.body()?.data,
                        totalStudents = totalStudentsRes.body()?.data ?: 0,
                        presentStudents = presentStudentsRes.body()?.data ?: 0,
                        totalTeachers = totalTeachersRes.body()?.data ?: 0,
                        presentTeachers = presentTeachersCount,
                        totalClasses = classesRes.body()?.data ?: 0,
                        totalSections = sectionsRes.body()?.data ?: 0,
                        totalSubjects = subjectsRes.body()?.data ?: 0,
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

    fun logout() {
        viewModelScope.launch {
            tokenManager.clearSession()
        }
    }

    companion object {
        fun provideFactory(
            apiService: ApiService,
            tokenManager: TokenManager
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AdminDashboardViewModel(apiService, tokenManager) as T
            }
        }
    }
}