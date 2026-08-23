package com.school.gdsportal.ui.teacher.assignments.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.Assignment
import com.school.gdsportal.data.remote.Submission
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import com.school.gdsportal.data.remote.SubmissionDisplay

data class TeacherAssignmentDetailUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val assignment: Assignment? = null,
    val submissions: List<SubmissionDisplay> = emptyList(),
    val subjectName: String? = null,
    val sectionName: String? = null,
    val isPublishing: Boolean = false
)

class TeacherAssignmentDetailViewModel(
    private val assignmentId: Long,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherAssignmentDetailUiState())
    val uiState: StateFlow<TeacherAssignmentDetailUiState> = _uiState.asStateFlow()

    init {
        loadDetails()
    }

    fun loadDetails() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val assignmentDeferred = async { apiService.getAssignmentById(assignmentId) }
                val assignmentResponse = assignmentDeferred.await()

                if (assignmentResponse.isSuccessful) {
                    val assignment = assignmentResponse.body()?.data
                    var subName: String? = null
                    var secName: String? = null
                    var submissionDisplays: List<SubmissionDisplay> = emptyList()

                    if (assignment != null) {
                        try {
                            val subjectsDef = async { apiService.getSubjects() }
                            val sectionDef = async { apiService.getSectionById(assignment.sectionId) }
                            val submissionsDef = async { apiService.getSubmissionsByAssignment(assignmentId) }
                            val studentsDef = async { apiService.getStudentsDirectory(null, null, null, assignment.sectionId, true, 0, 1000) }
                            
                            val subjectsRes = subjectsDef.await()
                            val sectionRes = sectionDef.await()
                            val submissionsRes = submissionsDef.await()
                            val studentsRes = studentsDef.await()

                            val subjects = subjectsRes.body()?.data ?: emptyList()
                            subName = subjects.find { it.subjectId == assignment.subjectId }?.subjectName
                            
                            val section = sectionRes.body()?.data
                            if (section != null) {
                                val cName = apiService.getClassById(section.classId).body()?.data?.className
                                secName = "${cName ?: "Class ${section.classId}"} - ${section.sectionName}"
                            }

                            val rawSubmissions = submissionsRes.body()?.data ?: emptyList()
                            val students = studentsRes.body()?.data?.content ?: emptyList()

                            submissionDisplays = rawSubmissions.map { sub ->
                                val student = students.find { it.studentId == sub.studentId }
                                SubmissionDisplay(
                                    submissionId = sub.submissionId,
                                    studentName = if (student != null) "${student.firstName} ${student.lastName}" else "Unknown",
                                    registrationNumber = student?.registrationNumber ?: "",
                                    assignmentTitle = assignment.title,
                                    submittedAt = sub.submittedAt ?: "Not Submitted",
                                    status = sub.status,
                                    marksAwarded = sub.marksAwarded?.toString() ?: "—",
                                    feedback = sub.feedback ?: "",
                                    fileUrl = sub.fileUrl ?: ""
                                )
                            }
                        } catch (e: Exception) {
                            // ignore lookup errors
                        }
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            assignment = assignment,
                            submissions = submissionDisplays,
                            subjectName = subName,
                            sectionName = secName,
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load assignment details.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Network error while fetching details.") }
            }
        }
    }

    fun publishAssignment() {
        val currentAssignment = _uiState.value.assignment ?: return
        _uiState.update { it.copy(isPublishing = true, error = null) }
        viewModelScope.launch {
            try {
                val updated = currentAssignment.copy(status = com.school.gdsportal.data.remote.AssignmentStatus.PUBLISHED)
                val response = apiService.updateAssignment(currentAssignment.assignmentId, updated)
                if (response.isSuccessful) {
                    _uiState.update { it.copy(isPublishing = false, assignment = updated) }
                } else {
                    _uiState.update { it.copy(isPublishing = false, error = "Failed to publish assignment.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isPublishing = false, error = "Network error while publishing.") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun gradeSubmission(submissionId: Long, marks: Double, feedback: String) {
        viewModelScope.launch {
            try {
                val currentTeacherRes = apiService.getTeacherMe()
                if (currentTeacherRes.isSuccessful) {
                    val teacherId = currentTeacherRes.body()?.data?.teacherId ?: return@launch
                    val request = com.school.gdsportal.data.remote.GradeRequest(
                        marksAwarded = marks,
                        feedback = feedback,
                        gradedBy = teacherId
                    )
                    
                    val updateRes = apiService.gradeSubmission(submissionId, request)
                    if (updateRes.isSuccessful) {
                        loadDetails() // Reload to reflect changes
                    } else {
                        val errorBody = updateRes.errorBody()?.string() ?: "Failed to save grade."
                        _uiState.update { it.copy(error = "Error: $errorBody") }
                    }
                } else {
                    _uiState.update { it.copy(error = "Failed to fetch user.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Network error while saving grade.") }
            }
        }
    }

    companion object {
        fun provideFactory(assignmentId: Long, apiService: ApiService): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TeacherAssignmentDetailViewModel(assignmentId, apiService) as T
                }
            }
    }
}


