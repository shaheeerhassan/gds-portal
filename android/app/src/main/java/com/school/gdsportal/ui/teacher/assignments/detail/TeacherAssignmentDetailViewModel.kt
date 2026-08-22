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
    val submissions: List<Submission> = emptyList()
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
                val submissionsDeferred = async { apiService.getSubmissionsByAssignment(assignmentId) }

                val assignmentResponse = assignmentDeferred.await()
                val submissionsResponse = submissionsDeferred.await()

                if (assignmentResponse.isSuccessful) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            assignment = assignmentResponse.body()?.data,
                            submissions = if (submissionsResponse.isSuccessful) submissionsResponse.body()?.data ?: emptyList() else emptyList()
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

