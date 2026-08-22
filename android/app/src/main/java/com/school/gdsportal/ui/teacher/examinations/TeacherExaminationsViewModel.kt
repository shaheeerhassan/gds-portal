package com.school.gdsportal.ui.teacher.examinations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.local.TokenManager
import com.school.gdsportal.data.remote.Examination
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TeacherExaminationsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val examinations: List<Examination> = emptyList()
)

class TeacherExaminationsViewModel(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(TeacherExaminationsUiState())
    val uiState: StateFlow<TeacherExaminationsUiState> = _uiState.asStateFlow()

    init { loadData() }

    fun loadData() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val teacherId = apiService.getTeacherMe().body()?.data?.teacherId ?: 0L
                if (teacherId == 0L) {
                    _uiState.update { it.copy(isLoading = false, error = "Unable to identify teacher.") }
                    return@launch
                }

                val currentYear = apiService.getCurrentAcademicYear().body()?.data
                if (currentYear == null) {
                    _uiState.update { it.copy(isLoading = false, error = "No academic year found.") }
                    return@launch
                }

                val classesRes = apiService.getTeacherClasses(teacherId, currentYear.academicYearId)
                if (classesRes.isSuccessful) {
                    val classes = classesRes.body()?.data ?: emptyList()
                    val allExams = mutableListOf<Examination>()
                    
                    for (c in classes) {
                        val examsRes = apiService.getExaminationsBySection(c.sectionId, currentYear.academicYearId)
                        if (examsRes.isSuccessful) {
                            examsRes.body()?.data?.let { allExams.addAll(it) }
                        }
                    }

                    // Remove duplicates if same examination applies to multiple? Examinations are per-section usually, but we use distinctBy just in case
                    val distinctExams = allExams.distinctBy { it.examinationId }.sortedByDescending { it.examDate }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            examinations = distinctExams
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load classes.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Network error.") }
            }
        }
    }

    companion object {
        fun provideFactory(apiService: ApiService, tokenManager: TokenManager): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TeacherExaminationsViewModel(apiService, tokenManager) as T
                }
            }
    }
}
