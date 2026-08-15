package com.school.gdsportal.ui.admin.students.enroll

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.AcademicYear
import com.school.gdsportal.data.remote.EnrollStudentRequest
import com.school.gdsportal.data.remote.SchoolClass
import com.school.gdsportal.data.remote.Section
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StudentEnrollUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false,
    
    // Dropdown options
    val academicYears: List<AcademicYear> = emptyList(),
    val classes: List<SchoolClass> = emptyList(),
    val sections: List<Section> = emptyList(),
    val isLoadingSections: Boolean = false,
    
    // Form fields
    val selectedAcademicYear: AcademicYear? = null,
    val selectedClass: SchoolClass? = null,
    val selectedSection: Section? = null,
    val rollNumber: String = ""
) {
    val isFormValid: Boolean
        get() = selectedAcademicYear != null &&
                selectedClass != null &&
                selectedSection != null &&
                rollNumber.isNotBlank()
}

class StudentEnrollViewModel(
    private val studentId: Long,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentEnrollUiState())
    val uiState: StateFlow<StudentEnrollUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    fun retry() {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val yearsRes = apiService.getAcademicYears()
                val classesRes = apiService.getClasses()
                
                if (yearsRes.isSuccessful && classesRes.isSuccessful) {
                    val years = yearsRes.body()?.data ?: emptyList()
                    val classesList = classesRes.body()?.data ?: emptyList()
                    
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            academicYears = years,
                            classes = classesList
                        )
                    }
                } else {
                    _uiState.update { 
                        it.copy(isLoading = false, error = "Failed to load academic data.")
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isLoading = false, error = "Network error. Please try again.")
                }
            }
        }
    }

    fun selectAcademicYear(year: AcademicYear) {
        _uiState.update { 
            it.copy(
                selectedAcademicYear = year,
                selectedSection = null,
                sections = emptyList()
            )
        }
        loadSections()
    }

    fun selectClass(schoolClass: SchoolClass) {
        _uiState.update { 
            it.copy(
                selectedClass = schoolClass,
                selectedSection = null,
                sections = emptyList()
            )
        }
        loadSections()
    }
    
    fun selectSection(section: Section) {
        _uiState.update { it.copy(selectedSection = section) }
    }
    
    fun updateRollNumber(value: String) {
        _uiState.update { it.copy(rollNumber = value) }
    }

    private fun loadSections() {
        val state = _uiState.value
        val classId = state.selectedClass?.classId
        val yearId = state.selectedAcademicYear?.academicYearId
        
        if (classId == null || yearId == null) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingSections = true, error = null) }
            try {
                val res = apiService.getSections(classId, yearId)
                if (res.isSuccessful) {
                    val sectionList = res.body()?.data ?: emptyList()
                    _uiState.update { 
                        it.copy(
                            isLoadingSections = false,
                            sections = sectionList,
                            // Auto-select if there's only one section
                            selectedSection = if (sectionList.size == 1) sectionList.first() else null
                        )
                    }
                } else {
                    _uiState.update { 
                        it.copy(isLoadingSections = false, error = "Failed to load sections.")
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isLoadingSections = false, error = "Network error while loading sections.")
                }
            }
        }
    }

    fun enrollStudent() {
        val state = _uiState.value
        
        if (!state.isFormValid) {
            _uiState.update { it.copy(error = "Please select Academic Year, Class, and Section.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val request = EnrollStudentRequest(
                    studentId = studentId,
                    classId = state.selectedClass!!.classId,
                    sectionId = state.selectedSection!!.sectionId,
                    academicYearId = state.selectedAcademicYear!!.academicYearId,
                    rollNumber = state.rollNumber.trim().takeIf { it.isNotEmpty() }
                )
                
                val response = apiService.enrollStudent(request)
                if (response.isSuccessful) {
                    _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                } else {
                    _uiState.update { 
                        it.copy(isSaving = false, error = "Failed to enroll student. They might already be enrolled in this term.")
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isSaving = false, error = "Network error while enrolling. Please try again.")
                }
            }
        }
    }
    
    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    companion object {
        fun provideFactory(studentId: Long, apiService: ApiService): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return StudentEnrollViewModel(studentId, apiService) as T
            }
        }
    }
}
