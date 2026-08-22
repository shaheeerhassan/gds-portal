package com.school.gdsportal.ui.teacher.assignments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.local.TokenManager
import com.school.gdsportal.data.remote.Assignment
import com.school.gdsportal.data.remote.TeacherClassDTO
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TeacherAssignmentsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val assignments: List<Assignment> = emptyList(),
    val assignedClasses: List<TeacherClassDTO> = emptyList() // Useful for the Create screen later
)

class TeacherAssignmentsViewModel(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherAssignmentsUiState())
    val uiState: StateFlow<TeacherAssignmentsUiState> = _uiState.asStateFlow()

    init { loadAssignments() }

    fun loadAssignments() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                // 1. Identify Teacher
                val teacherId = apiService.getTeacherMe().body()?.data?.teacherId ?: 0L
                if (teacherId == 0L) {
                    _uiState.update { it.copy(isLoading = false, error = "Unable to identify teacher account.") }
                    return@launch
                }

                // 2. Get Academic Year
                val yearResponse = apiService.getCurrentAcademicYear()
                val currentYear = yearResponse.body()?.data
                if (currentYear == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Could not determine academic year.") }
                    return@launch
                }

                // 3. Get Assigned Classes
                val classesResponse = apiService.getTeacherClasses(teacherId, currentYear.academicYearId)
                if (!classesResponse.isSuccessful) {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load assigned classes.") }
                    return@launch
                }

                val assignedClasses = classesResponse.body()?.data ?: emptyList()

                // 4. Concurrently fetch assignments for every assigned section
                val assignmentDeferreds = assignedClasses.map { section ->
                    async {
                        apiService.getAssignmentsBySection(section.sectionId, currentYear.academicYearId)
                    }
                }

                val responses = assignmentDeferreds.awaitAll()

                // 5. Flatten the lists and sort by deadline
                val allAssignments = responses
                    .filter { it.isSuccessful }
                    .flatMap { it.body()?.data ?: emptyList() }
                    .sortedByDescending { it.deadline ?: it.createdAt }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        assignments = allAssignments,
                        assignedClasses = assignedClasses
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Network error while fetching assignments.") }
            }
        }
    }

    companion object {
        fun provideFactory(apiService: ApiService, tokenManager: TokenManager): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TeacherAssignmentsViewModel(apiService, tokenManager) as T
                }
            }
    }
}
