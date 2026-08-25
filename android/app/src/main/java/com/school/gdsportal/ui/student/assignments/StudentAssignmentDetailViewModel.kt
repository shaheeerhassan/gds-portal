package com.school.gdsportal.ui.student.assessment.assignments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.Assignment
import com.school.gdsportal.data.remote.Submission
import com.school.gdsportal.data.remote.SubmissionStatus
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StudentAssignmentDetailUiState(
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,

    val assignment: Assignment? = null,
    val submission: Submission? = null,
    val subjectName: String = "",

    val fileUrlInput: String = "",
    val isEditMode: Boolean = false
)

class StudentAssignmentDetailViewModel(
    private val apiService: ApiService,
    private val assignmentId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentAssignmentDetailUiState())
    val uiState: StateFlow<StudentAssignmentDetailUiState> = _uiState.asStateFlow()

    private var currentStudentId: Long = 0L

    init {
        loadDetails()
    }

    fun loadDetails() {
        _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
        viewModelScope.launch {
            try {
                val studentRes = apiService.getStudentMe()
                val student = studentRes.body()?.data

                if (student != null) {
                    currentStudentId = student.studentId
                    val assignmentRes = apiService.getAssignmentById(assignmentId)
                    val assignment = assignmentRes.body()?.data

                    if (assignment != null) {
                        val subjectRes = apiService.getSubjectById(assignment.subjectId)
                        val subjectName = subjectRes.body()?.data?.subjectName ?: "Unknown Subject"

                        // Try to fetch existing submission
                        var existingSub: Submission? = null
                        try {
                            val subRes = apiService.getSubmissionForStudentAssignment(assignmentId, student.studentId)
                            if (subRes.isSuccessful) {
                                existingSub = subRes.body()?.data
                            }
                        } catch (e: Exception) {
                            // 404 means no submission yet, which is fine
                        }

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                assignment = assignment,
                                subjectName = subjectName,
                                submission = existingSub,
                                fileUrlInput = existingSub?.fileUrl ?: "",
                                isEditMode = existingSub != null && existingSub.status != SubmissionStatus.GRADED
                            )
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = "Assignment not found.") }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Student profile not found.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Network error while loading details.") }
            }
        }
    }

    fun onFileUrlChanged(url: String) {
        _uiState.update { it.copy(fileUrlInput = url) }
    }

    fun submitAssignment() {
        val state = _uiState.value
        if (state.fileUrlInput.isBlank()) {
            _uiState.update { it.copy(error = "Please provide a file URL or text submission.") }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, error = null, successMessage = null) }
        viewModelScope.launch {
            try {
                val submissionRequest = Submission(
                    submissionId = state.submission?.submissionId ?: 0L,
                    assignmentId = assignmentId,
                    studentId = currentStudentId,
                    submittedAt = null, // Backend handles this
                    fileUrl = state.fileUrlInput,
                    status = SubmissionStatus.SUBMITTED,
                    marksAwarded = null,
                    feedback = null,
                    gradedBy = null,
                    gradedAt = null
                )

                val response = if (state.submission != null) {
                    apiService.updateSubmission(state.submission.submissionId, submissionRequest)
                } else {
                    apiService.submitAssignment(submissionRequest)
                }

                if (response.isSuccessful) {
                    _uiState.update { it.copy(isSubmitting = false, successMessage = "Assignment submitted successfully!") }
                    loadDetails() // Reload to get fresh timestamps
                } else {
                    _uiState.update { it.copy(isSubmitting = false, error = "Failed to submit assignment.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSubmitting = false, error = "Network error during submission.") }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }

    companion object {
        fun provideFactory(apiService: ApiService, assignmentId: Long): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return StudentAssignmentDetailViewModel(apiService, assignmentId) as T
                }
            }
    }
}