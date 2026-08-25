package com.school.gdsportal.ui.student.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.local.TokenManager
import com.school.gdsportal.data.remote.*
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class StudentDashboardUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,

    val user: User? = null,
    val student: Student? = null,
    val currentAcademicYear: AcademicYear? = null,
    val currentEnrollment: Enrollment? = null,

    val attendancePercentage: Int = 0,
    val todaysClassesCount: Int = 0, // NEW STAT: Replaced pending tasks
    val recentGrades: List<MarkDisplay> = emptyList(),
    val globalAnnouncements: List<Announcement> = emptyList(),
    val unreadNotifications: Int = 0,

    val className: String = "",
    val sectionName: String = ""
)

class StudentDashboardViewModel(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentDashboardUiState())
    val uiState: StateFlow<StudentDashboardUiState> = _uiState.asStateFlow()

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
            val savedUser = tokenManager.getUserProfile()
            _uiState.update { it.copy(user = savedUser) }

            try {
                // Fetch core data first
                val studentRes = apiService.getStudentMe()
                val yearRes = apiService.getCurrentAcademicYear()
                val notificationsRes = apiService.getUnreadNotificationCount()
                val announcementsRes = apiService.getGlobalAnnouncements()

                val student = studentRes.body()?.data
                val year = yearRes.body()?.data
                val unreadCount = notificationsRes.body()?.data ?: 0
                val announcements = announcementsRes.body()?.data ?: emptyList()

                if (student != null && year != null) {
                    val enrollRes = apiService.getCurrentEnrollment(student.studentId)
                    val enrollment = if (enrollRes.isSuccessful) enrollRes.body()?.data else null

                    var className = "Unknown Class"
                    var sectionName = "Unknown Section"
                    var attendancePct = 0
                    var todaysClasses = 0
                    val recentMarksDisplays = mutableListOf<MarkDisplay>()

                    if (enrollment != null) {
                        // 1. Resolve Class/Section names
                        val secRes = apiService.getSectionById(enrollment.sectionId)
                        if (secRes.isSuccessful) {
                            val section = secRes.body()?.data
                            if (section != null) {
                                sectionName = section.sectionName ?: "Unknown Section"
                                val clsRes = apiService.getClassById(section.classId)
                                if (clsRes.isSuccessful) {
                                    className = clsRes.body()?.data?.className ?: "Class ${section.classId}"
                                }
                            }
                        }

                        // 2. Fetch Attendance
                        val startFormat = year.startDate.take(10)
                        val todayCalendar = Calendar.getInstance()
                        val todayDateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(todayCalendar.time)
                        val attRes = apiService.getStudentAttendanceRecords(student.studentId, startFormat, todayDateString)

                        if (attRes.isSuccessful) {
                            val records = attRes.body()?.data ?: emptyList()
                            if (records.isNotEmpty()) {
                                val presentOrLate = records.count { it.status.name == "PRESENT" || it.status.name == "LATE" }
                                attendancePct = ((presentOrLate.toFloat() / records.size.toFloat()) * 100).toInt()
                            } else {
                                attendancePct = 100
                            }
                        }

                        // 3. Fetch Recent Grades
                        val marksRes = apiService.getMarksByStudentAndYear(student.studentId, year.academicYearId)
                        if (marksRes.isSuccessful) {
                            val marks = marksRes.body()?.data ?: emptyList()
                            val topMarks = marks.takeLast(3).reversed()
                            for (mark in topMarks) {
                                val examRes = apiService.getExaminationById(mark.examinationId)
                                val exam = examRes.body()?.data
                                val examName = exam?.examName ?: "Exam #${mark.examinationId}"
                                val subjName = if (exam != null) {
                                    apiService.getSubjectById(exam.subjectId).body()?.data?.subjectName ?: ""
                                } else ""

                                val maxMarks = exam?.maxMarks?.toString() ?: ""
                                val marksText = if (mark.marksObtained != null) {
                                    if (maxMarks.isNotEmpty()) "${mark.marksObtained} / $maxMarks" else mark.marksObtained.toString()
                                } else {
                                    "—"
                                }

                                recentMarksDisplays.add(
                                    MarkDisplay(
                                        markId = mark.markId,
                                        studentId = student.studentId,
                                        studentName = "",
                                        registrationNumber = "",
                                        examinationName = "$subjName - $examName",
                                        marksObtained = marksText,
                                        grade = mark.grade ?: "",
                                        remarks = mark.remarks ?: ""
                                    )
                                )
                            }
                        }

                        // 4. Calculate Today's Classes
                        try {
                            val todayDayString = SimpleDateFormat("EEEE", Locale.ENGLISH).format(todayCalendar.time).uppercase()
                            val timetableRes = apiService.getTimetableBySection(enrollment.sectionId, year.academicYearId)
                            if (timetableRes.isSuccessful) {
                                val timetable = timetableRes.body()?.data ?: emptyList()
                                // Uses the exact same secure startsWith fix we applied to the Timetable screen!
                                todaysClasses = timetable.count {
                                    todayDayString.startsWith(it.dayOfWeek.toString(), ignoreCase = true)
                                }
                            }
                        } catch (e: Exception) {
                            // Failsafe for timetable calculation
                        }
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            student = student,
                            currentAcademicYear = year,
                            currentEnrollment = enrollment,
                            className = className,
                            sectionName = sectionName,
                            attendancePercentage = attendancePct,
                            todaysClassesCount = todaysClasses,
                            recentGrades = recentMarksDisplays,
                            globalAnnouncements = announcements,
                            unreadNotifications = unreadCount
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, isRefreshing = false, error = "Failed to load academic profile.") }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, isRefreshing = false, error = "Network error while loading dashboard.")
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
                return StudentDashboardViewModel(apiService, tokenManager) as T
            }
        }
    }
}