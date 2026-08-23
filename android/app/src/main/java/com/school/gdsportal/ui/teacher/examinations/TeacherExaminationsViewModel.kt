package com.school.gdsportal.ui.teacher.examinations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.local.TokenManager
import com.school.gdsportal.data.remote.Examination
import com.school.gdsportal.data.remote.TeacherClassDTO
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import com.school.gdsportal.data.remote.TeacherSubjectDTO

data class TeacherExaminationsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val assignedClasses: List<TeacherClassDTO> = emptyList(),
    val availableSubjects: List<TeacherSubjectDTO> = emptyList(),
    val selectedSection: TeacherClassDTO? = null,
    val examinations: List<Examination> = emptyList()
)

class TeacherExaminationsViewModel(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(TeacherExaminationsUiState())
    val uiState: StateFlow<TeacherExaminationsUiState> = _uiState.asStateFlow()

    private var currentAcademicYearId: Int = 0
    private var currentUserId: Long = 0L

    init { loadInitialData() }

    fun loadInitialData() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val teacherId = apiService.getTeacherMe().body()?.data?.teacherId ?: 0L
                val currentYear = apiService.getCurrentAcademicYear().body()?.data
                val user = apiService.getCurrentUser().body()?.data

                if (teacherId == 0L || currentYear == null || user == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Unable to identify teacher, user or year.") }
                    return@launch
                }

                currentAcademicYearId = currentYear.academicYearId
                currentUserId = user.userId
                val classesRes = apiService.getTeacherClasses(teacherId, currentAcademicYearId)
                val subjectsRes = apiService.getTeacherSubjects(teacherId, currentAcademicYearId)

                if (classesRes.isSuccessful) {
                    val classes = classesRes.body()?.data ?: emptyList()
                    val subjects = subjectsRes.body()?.data ?: emptyList()
                    val firstSection = classes.firstOrNull()

                    _uiState.update {
                        it.copy(assignedClasses = classes, availableSubjects = subjects, selectedSection = firstSection)
                    }

                    if (firstSection != null) {
                        loadExaminationsForSection(firstSection.sectionId)
                    } else {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load classes.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Network error.") }
            }
        }
    }

    fun onSectionSelected(section: TeacherClassDTO) {
        _uiState.update { it.copy(selectedSection = section) }

        viewModelScope.launch {
            loadExaminationsForSection(section.sectionId)
        }
    }

    private suspend fun loadExaminationsForSection(sectionId: Int) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        try {
            val examsRes = apiService.getExaminationsBySection(sectionId, currentAcademicYearId)
            if (examsRes.isSuccessful) {
                val exams = examsRes.body()?.data?.filter { it.createdBy == currentUserId }?.sortedByDescending { it.examDate } ?: emptyList()
                _uiState.update { it.copy(isLoading = false, examinations = exams) }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Failed to load examinations.") }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false, error = "Network error while loading exams.") }
        }
    }
    fun deleteExamination(examinationId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = apiService.deleteExamination(examinationId)
                if (response.isSuccessful) {
                    val currentSection = _uiState.value.selectedSection
                    if (currentSection != null) {
                        loadExaminationsForSection(currentSection.sectionId)
                    } else {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to delete examination.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Network error while deleting.") }
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