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

data class TeacherAssignmentDetailUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val assignment: Assignment? = null,
    val submissions: List<Submission> = emptyList(),
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
                    if (assignment != null) {
                        try {
                            val subjects = apiService.getSubjects().body()?.data ?: emptyList()
                            subName = subjects.find { it.subjectId == assignment.subjectId }?.subjectName
                            
                            val section = apiService.getSectionById(assignment.sectionId).body()?.data
                            if (section != null) {
                                val cName = apiService.getClassById(section.classId).body()?.data?.className
                                secName = "${cName ?: "Class ${section.classId}"} - ${section.sectionName}"
                            }
                        } catch (e: Exception) {
                            // ignore lookup errors
                        }
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            assignment = assignment,
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


