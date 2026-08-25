package com.school.gdsportal.ui.student.assessment.examinations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.Examination
import com.school.gdsportal.data.remote.ExaminationStatus
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StudentExaminationsUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false, // Added for swipe-to-refresh
    val error: String? = null,
    val examinations: List<Examination> = emptyList(),
    val subjectNames: Map<Int, String> = emptyMap()
)

class StudentExaminationsViewModel(private val apiService: ApiService) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentExaminationsUiState())
    val uiState: StateFlow<StudentExaminationsUiState> = _uiState.asStateFlow()

    fun loadExaminations(isRefresh: Boolean = false) {
        _uiState.update {
            if (isRefresh) it.copy(isRefreshing = true, error = null)
            else it.copy(isLoading = true, error = null)
        }

        viewModelScope.launch {
            try {
                val studentRes = apiService.getStudentMe()
                val yearRes = apiService.getCurrentAcademicYear()
                val student = studentRes.body()?.data
                val year = yearRes.body()?.data

                if (student != null && year != null) {
                    val enrollRes = apiService.getCurrentEnrollment(student.studentId)
                    val enrollment = enrollRes.body()?.data

                    if (enrollment != null) {
                        val examsRes = apiService.getExaminationsBySection(enrollment.sectionId, year.academicYearId)
                        val allExams = examsRes.body()?.data ?: emptyList()

                        // FIX: Filter out scheduled exams so students don't see them prematurely
                        val visibleExams = allExams.filter { it.status != ExaminationStatus.SCHEDULED }

                        // Fetch subjects to map IDs to Names
                        val subjectsRes = apiService.getSubjectsBySection(enrollment.sectionId, year.academicYearId)
                        val subjects = subjectsRes.body()?.data ?: emptyList()
                        val subjectMap = subjects.associate { it.subjectId to it.subjectName }

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                examinations = visibleExams.sortedByDescending { e -> e.examDate },
                                subjectNames = subjectMap
                            )
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false, isRefreshing = false, error = "No active enrollment found.") }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, isRefreshing = false, error = "Failed to load profile data.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, isRefreshing = false, error = "Network error while loading examinations.") }
            }
        }
    }

    companion object {
        fun provideFactory(apiService: ApiService): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return StudentExaminationsViewModel(apiService) as T
                }
            }
    }
}