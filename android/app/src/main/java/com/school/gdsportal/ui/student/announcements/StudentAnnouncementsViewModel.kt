package com.school.gdsportal.ui.student.announcements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.Announcement
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StudentAnnouncementsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val announcements: List<Announcement> = emptyList()
)

class StudentAnnouncementsViewModel(private val apiService: ApiService) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentAnnouncementsUiState())
    val uiState: StateFlow<StudentAnnouncementsUiState> = _uiState.asStateFlow()

    init {
        loadAnnouncements()
    }

    private fun loadAnnouncements() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                // 1. Fetch Student's specific Class and Section
                val studentRes = apiService.getStudentMe()
                val student = studentRes.body()?.data

                var studentSectionId: Int? = null
                var studentClassId: Int? = null

                if (student != null) {
                    val enrollRes = apiService.getCurrentEnrollment(student.studentId)
                    studentSectionId = enrollRes.body()?.data?.sectionId

                    if (studentSectionId != null) {
                        val secRes = apiService.getSectionById(studentSectionId)
                        studentClassId = secRes.body()?.data?.classId
                    }
                }

                // 2. Fetch all announcements
                val response = apiService.getAnnouncements()
                if (response.isSuccessful) {
                    val all = response.body()?.data ?: emptyList()

                    // 3. Apply Strict Targeting Rules
                    val studentAnnouncements = all.filter { ann ->
                        if (!ann.isActive) return@filter false

                        // Check 1: Is it completely Global?
                        val isGlobal = (ann.targetRoleId == null || ann.targetRoleId == 0) &&
                                (ann.classId == null || ann.classId == 0) &&
                                (ann.sectionId == null || ann.sectionId == 0)

                        // Check 2: Is it targeted to ALL students globally?
                        val isAllStudents = ann.targetRoleId == 4 &&
                                (ann.classId == null || ann.classId == 0) &&
                                (ann.sectionId == null || ann.sectionId == 0)

                        // Check 3: Is it targeted to this student's specific Class?
                        val isMyClass = ann.classId == studentClassId &&
                                (ann.sectionId == null || ann.sectionId == 0) &&
                                (ann.targetRoleId == null || ann.targetRoleId == 0 || ann.targetRoleId == 4)

                        // Check 4: Is it targeted to this student's specific Section?
                        val isMySection = ann.sectionId == studentSectionId &&
                                (ann.targetRoleId == null || ann.targetRoleId == 0 || ann.targetRoleId == 4)

                        isGlobal || isAllStudents || isMyClass || isMySection
                    }.sortedByDescending { it.createdAt }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            announcements = studentAnnouncements
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load announcements.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Network error.") }
            }
        }
    }

    companion object {
        fun provideFactory(apiService: ApiService): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return StudentAnnouncementsViewModel(apiService) as T
                }
            }
    }
}