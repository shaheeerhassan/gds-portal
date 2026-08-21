package com.school.gdsportal.ui.parent.children.examinations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.Examination
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChildExaminationsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val examinations: List<Examination> = emptyList()
)

class ChildExaminationsViewModel(
    private val studentId: Long,
    private val apiService: ApiService
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChildExaminationsUiState())
    val uiState: StateFlow<ChildExaminationsUiState> = _uiState.asStateFlow()

    init { loadExaminations() }

    fun loadExaminations() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val enrollmentRes = apiService.getCurrentEnrollment(studentId)
                val enrollment = enrollmentRes.body()?.data

                if (enrollment == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Active enrollment not found.") }
                    return@launch
                }

                val examsRes = apiService.getExaminationsBySection(enrollment.sectionId, enrollment.academicYearId)
                if (examsRes.isSuccessful) {
                    val exams = examsRes.body()?.data ?: emptyList()
                    _uiState.update { it.copy(isLoading = false, examinations = exams.sortedBy { it.examDate }) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load examinations.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Network error.") }
            }
        }
    }

    companion object {
        fun provideFactory(studentId: Long, apiService: ApiService): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ChildExaminationsViewModel(studentId, apiService) as T
                }
            }
    }
}