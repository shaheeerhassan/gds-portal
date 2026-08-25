package com.school.gdsportal.ui.student.assessment.assignments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.Assignment
import com.school.gdsportal.data.remote.AssignmentStatus
import com.school.gdsportal.data.remote.SubmissionDisplay
import com.school.gdsportal.data.remote.SubmissionStatus
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StudentAssignmentItem(
    val assignment: Assignment,
    val subjectName: String,
    val submission: SubmissionDisplay?
)

data class StudentAssignmentsUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val filter: String = "All", // "All", "Pending", "Submitted"
    val allAssignments: List<StudentAssignmentItem> = emptyList(),
    val displayAssignments: List<StudentAssignmentItem> = emptyList()
)

class StudentAssignmentsViewModel(private val apiService: ApiService) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentAssignmentsUiState())
    val uiState: StateFlow<StudentAssignmentsUiState> = _uiState.asStateFlow()

    init {
        loadAssignments()
    }

    fun loadAssignments(isRefresh: Boolean = false) {
        _uiState.update {
            if (isRefresh) it.copy(isRefreshing = true, error = null)
            else it.copy(isLoading = true, error = null)
        }
        viewModelScope.launch {
            try {
                // 1. Get Student & Year
                val studentRes = apiService.getStudentMe()
                val yearRes = apiService.getCurrentAcademicYear()
                val student = studentRes.body()?.data
                val year = yearRes.body()?.data

                if (student != null && year != null) {
                    val enrollRes = apiService.getCurrentEnrollment(student.studentId)
                    val enrollment = enrollRes.body()?.data

                    if (enrollment != null) {
                        // 2. Fetch Assignments for the Section, Submissions for the Student, and Subjects
                        val assignmentsDef = async { apiService.getAssignmentsBySection(enrollment.sectionId, year.academicYearId) }
                        val submissionsDef = async { apiService.getSubmissionsByStudent(student.studentId) }
                        val subjectsDef = async { apiService.getSubjectsBySection(enrollment.sectionId, year.academicYearId) }

                        val assignments = assignmentsDef.await().body()?.data?.filter { it.status == AssignmentStatus.PUBLISHED } ?: emptyList()
                        val submissions = submissionsDef.await().body()?.data ?: emptyList()
                        val subjects = subjectsDef.await().body()?.data ?: emptyList()

                        // 3. Map them together
                        val mappedItems = assignments.map { assignment ->
                            val sub = submissions.find { it.assignmentTitle == assignment.title } // Match by title based on DTO
                            val subject = subjects.find { it.subjectId == assignment.subjectId }?.subjectName ?: "Unknown Subject"
                            StudentAssignmentItem(assignment, subject, sub)
                        }.sortedByDescending { it.assignment.createdAt }

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                allAssignments = mappedItems
                            )
                        }
                        applyFilter(_uiState.value.filter)
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = "No active enrollment found.") }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load profile data.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Network error: ${e.localizedMessage}") }
            }
        }
    }

    fun setFilter(filter: String) {
        _uiState.update { it.copy(filter = filter) }
        applyFilter(filter)
    }

    private fun applyFilter(filter: String) {
        val all = _uiState.value.allAssignments
        val filtered = when (filter) {
            "Pending" -> all.filter { it.submission == null }
            "Submitted" -> all.filter { it.submission != null }
            else -> all
        }
        _uiState.update { it.copy(displayAssignments = filtered) }
    }

    companion object {
        fun provideFactory(apiService: ApiService): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return StudentAssignmentsViewModel(apiService) as T
                }
            }
    }
}