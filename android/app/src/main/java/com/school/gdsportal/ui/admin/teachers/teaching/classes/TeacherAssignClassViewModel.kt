package com.school.gdsportal.ui.admin.teachers.teaching.classes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.SchoolClass
import com.school.gdsportal.data.remote.Section
import com.school.gdsportal.data.remote.TeacherClassAssignRequest
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TeacherAssignClassUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val classes: List<SchoolClass> = emptyList(),
    val sections: List<Section> = emptyList(),
    val selectedClass: SchoolClass? = null,
    val selectedSection: Section? = null,
    val isAssigning: Boolean = false,
    val assignSuccess: Boolean = false
)

class TeacherAssignClassViewModel(
    private val teacherId: Long,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherAssignClassUiState())
    val uiState: StateFlow<TeacherAssignClassUiState> = _uiState.asStateFlow()

    private val currentAcademicYearId = 1

    init {
        loadClasses()
    }

    fun loadClasses() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val classesRes = apiService.getClasses()
                if (classesRes.isSuccessful && classesRes.body()?.data != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            classes = classesRes.body()?.data ?: emptyList()
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to load classes."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Network error while loading classes."
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

    fun submitAssignment() {
        val selectedClass = _uiState.value.selectedClass
        val selectedSection = _uiState.value.selectedSection

        if (selectedClass == null || selectedSection == null) {
            _uiState.update { it.copy(error = "Please select both a class and a section.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isAssigning = true, error = null) }
            try {
                val request = TeacherClassAssignRequest(
                    teacherId = teacherId,
                    classId = selectedClass.classId,
                    sectionId = selectedSection.sectionId,
                    academicYearId = currentAcademicYearId
                )
                val response = apiService.assignTeacherClass(request)
                if (response.isSuccessful) {
                    _uiState.update { it.copy(isAssigning = false, assignSuccess = true) }
                } else {
                    val errMsg = response.body()?.message ?: "Failed to assign class."
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
                return TeacherAssignClassViewModel(teacherId, apiService) as T
            }
        }
    }
}
