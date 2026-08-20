package com.school.gdsportal.ui.parent.children.assignments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.Assignment
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChildAssignmentsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val assignments: List<Assignment> = emptyList()
)

class ChildAssignmentsViewModel(
    private val studentId: Long,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChildAssignmentsUiState())
    val uiState: StateFlow<ChildAssignmentsUiState> = _uiState.asStateFlow()

    init {
        loadAssignments()
    }

    fun loadAssignments() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                // 1. Get current enrollment to find the student's section and academic year
                val enrollmentResponse = apiService.getCurrentEnrollment(studentId)
                val enrollment = enrollmentResponse.body()?.data

                if (enrollment == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Could not find active enrollment for this student.") }
                    return@launch
                }

                // 2. Fetch assignments for that specific section and year
                val assignmentsResponse = apiService.getAssignmentsBySection(
                    sectionId = enrollment.sectionId,
                    academicYearId = enrollment.academicYearId
                )

                if (assignmentsResponse.isSuccessful) {
                    val data = assignmentsResponse.body()?.data ?: emptyList()
                    // Sort by deadline descending (or ascending, depending on preference)
                    val sortedData = data.sortedByDescending { it.createdAt }
                    _uiState.update { it.copy(isLoading = false, assignments = sortedData) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load assignments.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Network error while loading assignments.") }
            }
        }
    }

    companion object {
        fun provideFactory(studentId: Long, apiService: ApiService): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ChildAssignmentsViewModel(studentId, apiService) as T
                }
            }
    }
}