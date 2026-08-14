package com.school.gdsportal.ui.admin.teachers.teaching.subjects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.SchoolClass
import com.school.gdsportal.data.remote.Section
import com.school.gdsportal.data.remote.Subject
import com.school.gdsportal.data.remote.TeacherSubjectAssignRequest
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TeacherAssignSubjectUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val classes: List<SchoolClass> = emptyList(),
    val sections: List<Section> = emptyList(),
    val subjects: List<Subject> = emptyList(),
    val selectedClass: SchoolClass? = null,
    val selectedSection: Section? = null,
    val selectedSubject: Subject? = null,
    val isAssigning: Boolean = false,
    val assignSuccess: Boolean = false
)

class TeacherAssignSubjectViewModel(
    private val teacherId: Long,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherAssignSubjectUiState())
    val uiState: StateFlow<TeacherAssignSubjectUiState> = _uiState.asStateFlow()

    private val currentAcademicYearId = 1

    init {
        loadInitialData()
    }

    fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val classesRes = apiService.getClasses()
                val subjectsRes = apiService.getSubjects()
                
                if (classesRes.isSuccessful && subjectsRes.isSuccessful) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            classes = classesRes.body()?.data ?: emptyList(),
                            subjects = subjectsRes.body()?.data ?: emptyList()
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to load classes and subjects."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Network error while loading data."
                    )
                }
            }
        }
    }

    fun selectClass(schoolClass: SchoolClass) {
        _uiState.update {
            it.copy(
                selectedClass = schoolClass,
                selectedSection = null,
                sections = emptyList(),
                isLoading = true,
                error = null
            )
        }
        viewModelScope.launch {
            try {
                val sectionsRes = apiService.getSections(schoolClass.classId, currentAcademicYearId)
                if (sectionsRes.isSuccessful && sectionsRes.body()?.data != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            sections = sectionsRes.body()?.data ?: emptyList()
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, error = "Failed to load sections.")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = "Network error while loading sections.")
                }
            }
        }
    }

    fun selectSection(section: Section) {
        _uiState.update { it.copy(selectedSection = section) }
    }

    fun selectSubject(subject: Subject) {
        _uiState.update { it.copy(selectedSubject = subject) }
    }

    fun submitAssignment() {
        val selectedSection = _uiState.value.selectedSection
        val selectedSubject = _uiState.value.selectedSubject

        if (selectedSection == null || selectedSubject == null) {
            _uiState.update { it.copy(error = "Please select both a section and a subject.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isAssigning = true, error = null) }
            try {
                val request = TeacherSubjectAssignRequest(
                    teacherId = teacherId,
                    subjectId = selectedSubject.subjectId,
                    sectionId = selectedSection.sectionId,
                    academicYearId = currentAcademicYearId
                )
                val response = apiService.assignTeacherSubject(request)
                if (response.isSuccessful) {
                    _uiState.update { it.copy(isAssigning = false, assignSuccess = true) }
                } else {
                    val errMsg = response.body()?.message ?: "Failed to assign subject."
                    _uiState.update { it.copy(isAssigning = false, error = errMsg) }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isAssigning = false, error = "Network error. Please try again.")
                }
            }
        }
    }

    fun errorShown() {
        _uiState.update { it.copy(error = null) }
    }

    companion object {
        fun provideFactory(teacherId: Long, apiService: ApiService): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TeacherAssignSubjectViewModel(teacherId, apiService) as T
            }
        }
    }
}
