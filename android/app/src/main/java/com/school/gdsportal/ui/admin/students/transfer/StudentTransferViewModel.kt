package com.school.gdsportal.ui.admin.students.transfer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.AcademicYear
import com.school.gdsportal.data.remote.Enrollment
import com.school.gdsportal.data.remote.SchoolClass
import com.school.gdsportal.data.remote.Section
import com.school.gdsportal.data.remote.TransferStudentRequest
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StudentTransferUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false,
    
    // Base Enrollment context
    val currentEnrollment: Enrollment? = null,
    val academicYearName: String = "",
    val currentClassName: String = "",
    val currentSectionName: String = "",
    
    // Dropdown options
    val classes: List<SchoolClass> = emptyList(),
    val sections: List<Section> = emptyList(),
    val isLoadingSections: Boolean = false,
    
    // Form fields
    val selectedClass: SchoolClass? = null,
    val selectedSection: Section? = null,
    val rollNumber: String = ""
) {
    val isFormValid: Boolean
        get() = selectedClass != null && selectedSection != null && rollNumber.isNotBlank()
}

class StudentTransferViewModel(
    private val studentId: Long,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentTransferUiState())
    val uiState: StateFlow<StudentTransferUiState> = _uiState.asStateFlow()

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
                // 1. Fetch current enrollment to get academicYearId
                val enrollmentRes = apiService.getCurrentEnrollment(studentId)
                if (!enrollmentRes.isSuccessful || enrollmentRes.body()?.data == null) {
                    _uiState.update { 
                        it.copy(isLoading = false, error = "Student is not currently enrolled.")
                    }
                    return@launch
                }
                
                val enrollment = enrollmentRes.body()!!.data!!
                
                // Fetch details for display
                val yearsRes = apiService.getAcademicYears()
                val classesRes = apiService.getClasses()
                
                val years = yearsRes.body()?.data ?: emptyList()
                val classesList = classesRes.body()?.data ?: emptyList()
                
                val yearName = years.find { it.academicYearId == enrollment.academicYearId }?.yearName ?: "Unknown Year"
                val className = classesList.find { it.classId == enrollment.classId }?.className ?: "Unknown Class"
                
                val currentSectionsRes = apiService.getSections(enrollment.classId, enrollment.academicYearId)
                val currentSectionName = currentSectionsRes.body()?.data
                    ?.find { it.sectionId == enrollment.sectionId }?.sectionName ?: "Unknown Section"
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        currentEnrollment = enrollment,
                        academicYearName = yearName,
                        currentClassName = className,
                        currentSectionName = currentSectionName,
                        classes = classesList
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isLoading = false, error = "Network error. Please try again.")
                }
            }
        }
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
        val yearId = state.currentEnrollment?.academicYearId
        
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

    fun transferStudent() {
        val state = _uiState.value
        val enrollment = state.currentEnrollment ?: return
        
        if (!state.isFormValid) {
            _uiState.update { it.copy(error = "Please select Class and Section.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val request = TransferStudentRequest(
                    studentId = studentId,
                    academicYearId = enrollment.academicYearId,
                    newSectionId = state.selectedSection!!.sectionId,
                    rollNumber = state.rollNumber.trim().takeIf { it.isNotEmpty() }
                )
                
                val response = apiService.transferStudent(request)
                if (response.isSuccessful) {
                    _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                } else {
                    _uiState.update { 
                        it.copy(isSaving = false, error = "Failed to transfer student. Please check details and try again.")
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isSaving = false, error = "Network error while transferring. Please try again.")
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
                return StudentTransferViewModel(studentId, apiService) as T
            }
        }
    }
}
