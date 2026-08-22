package com.school.gdsportal.ui.teacher.subjects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.local.TokenManager
import com.school.gdsportal.data.remote.TeacherSubjectDTO
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TeacherSubjectsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val subjects: List<TeacherSubjectDTO> = emptyList()
)

class TeacherSubjectsViewModel(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherSubjectsUiState())
    val uiState: StateFlow<TeacherSubjectsUiState> = _uiState.asStateFlow()

    init { loadMySubjects() }

    fun loadMySubjects() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                // 1. Get Teacher ID from local token
                val teacherId = apiService.getTeacherMe().body()?.data?.teacherId ?: 0L
                if (teacherId == 0L) {
                    _uiState.update { it.copy(isLoading = false, error = "Unable to identify teacher account.") }
                    return@launch
                }

                // 2. Fetch the current Academic Year
                val yearResponse = apiService.getCurrentAcademicYear()
                val currentYear = yearResponse.body()?.data
                if (currentYear == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Could not determine the current academic year.") }
                    return@launch
                }

                // 3. Fetch assigned subjects for this teacher and year
                val subjectsResponse = apiService.getTeacherSubjects(teacherId, currentYear.academicYearId)
                if (subjectsResponse.isSuccessful) {
                    val subjects = subjectsResponse.body()?.data ?: emptyList()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            // Sort by class name, then subject name for neatness
                            subjects = subjects.sortedWith(compareBy({ s -> s.className }, { s -> s.subjectName }))
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load assigned subjects.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Network error while fetching subjects.") }
            }
        }
    }

    companion object {
        fun provideFactory(apiService: ApiService, tokenManager: TokenManager): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TeacherSubjectsViewModel(apiService, tokenManager) as T
                }
            }
    }
}
