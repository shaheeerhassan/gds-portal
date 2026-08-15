package com.school.gdsportal.ui.admin.assessment.submissions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.SubmissionDisplay
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SubmissionDetailUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val submission: SubmissionDisplay? = null,
    val subjectName: String = "",
    val sectionName: String = "",
    val teacherName: String = "",
    val maxMarks: String = ""
)

class SubmissionDetailViewModel(
    private val apiService: ApiService,
    private val submissionId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubmissionDetailUiState())
    val uiState: StateFlow<SubmissionDetailUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val submissionRes = apiService.getSubmissionById(submissionId)
                if (submissionRes.isSuccessful) {
                    val submission = submissionRes.body()?.data
                    if (submission != null) {
                        coroutineScope {
                            val studentDef = async { apiService.getStudent(submission.studentId) }
                            val assignmentDef = async { apiService.getAssignmentById(submission.assignmentId) }

                            val student = studentDef.await().body()?.data
                            val assignment = assignmentDef.await().body()?.data

                            var subjectName = "Unknown Subject"
                            var sectionName = "Unknown Section"
                            var teacherName = "Unknown Teacher"
                            var maxMarks = ""

                            if (assignment != null) {
                                maxMarks = assignment.maxMarks.toString()

                                val subjectDef = async { apiService.getSubjectById(assignment.subjectId) }
                                val sectionDef = async { apiService.getSectionById(assignment.sectionId) }
                                val teacherDef = async { apiService.getTeacherById(assignment.teacherId) }

                                val subject = subjectDef.await().body()?.data
                                val section = sectionDef.await().body()?.data
                                val teacher = teacherDef.await().body()?.data

                                subjectName = subject?.subjectName ?: "Unknown Subject"
                                if (teacher != null) teacherName = "${teacher.firstName} ${teacher.lastName}"

                                if (section != null) {
                                    val classDef = async { apiService.getClassById(section.classId) }
                                    val clazz = classDef.await().body()?.data
                                    sectionName = "${clazz?.className ?: ""} ${section.sectionName}"
                                }
                            }

                            val display = SubmissionDisplay(
                                submissionId = submission.submissionId,
                                studentName = if (student != null) "${student.firstName} ${student.lastName}" else "Unknown Student",
                                registrationNumber = student?.registrationNumber ?: "",
                                assignmentTitle = assignment?.title ?: "Assignment #${submission.assignmentId}",
                                submittedAt = submission.submittedAt?.let { it.replace("T", " ").take(16) } ?: "Unknown",
                                status = submission.status,
                                marksAwarded = submission.marksAwarded?.toString() ?: "—",
                                feedback = submission.feedback ?: "",
                                fileUrl = submission.fileUrl ?: ""
                            )

                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                submission = display,
                                subjectName = subjectName,
                                sectionName = sectionName,
                                teacherName = teacherName,
                                maxMarks = maxMarks
                            )
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false, error = "Submission not found")
                    }
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to load submission")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Network error: ${e.message}")
            }
        }
    }

    // No mutation methods — read-only

    class Factory(private val apiService: ApiService, private val submissionId: Long) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SubmissionDetailViewModel(apiService, submissionId) as T
        }
    }
}
