package com.school.gdsportal.ui.admin.reports.studentperformance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.*
import com.school.gdsportal.data.remote.dto.StudentDirectoryDTO
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StudentPerformanceSelectorUiState(
    val isLoading: Boolean = true,
    val error: String? = null,

    val academicYears: List<AcademicYear> = emptyList(),
    val selectedAcademicYearId: Int? = null,

    val classes: List<SchoolClass> = emptyList(),
    val selectedClassId: Int? = null,

    val sections: List<Section> = emptyList(),
    val selectedSectionId: Int? = null,

    val searchQuery: String = "",
    val students: List<StudentDirectoryDTO> = emptyList(),
    val isSearching: Boolean = false,

    val selectedStudent: StudentDirectoryDTO? = null
)

class StudentPerformanceSelectorViewModel(private val apiService: ApiService) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentPerformanceSelectorUiState())
    val uiState: StateFlow<StudentPerformanceSelectorUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                coroutineScope {
                    val yearsDef = async { apiService.getAcademicYears() }
                    val classesDef = async { apiService.getClasses() }
                    
                    val years = yearsDef.await().body()?.data ?: emptyList()
                    val classes = classesDef.await().body()?.data ?: emptyList()

                    val defaultYearId = years.firstOrNull { it.isCurrent }?.academicYearId
                        ?: years.firstOrNull()?.academicYearId

                    _uiState.value = _uiState.value.copy(
                        academicYears = years,
                        selectedAcademicYearId = defaultYearId,
                        classes = classes,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to load data: ${e.message}")
            }
        }
    }

    fun selectAcademicYear(yearId: Int) {
        _uiState.value = _uiState.value.copy(selectedAcademicYearId = yearId)
        val classId = _uiState.value.selectedClassId
        if (classId != null) {
            loadSections(classId, yearId)
        }
    }

    fun selectClass(classId: Int?) {
        val id = if (classId == 0) null else classId
        _uiState.value = _uiState.value.copy(
            selectedClassId = id,
            selectedSectionId = null,
            sections = emptyList(),
            selectedStudent = null,
            searchQuery = "",
            students = emptyList()
        )
        val yearId = _uiState.value.selectedAcademicYearId
        if (id != null && yearId != null) {
            loadSections(id, yearId)
        }
    }

    private fun loadSections(classId: Int, yearId: Int) {
        viewModelScope.launch {
            try {
                val res = apiService.getSections(classId, yearId)
                if (res.isSuccessful) {
                    val sections = res.body()?.data ?: emptyList()
                    _uiState.value = _uiState.value.copy(sections = sections)
                }
            } catch (_: Exception) {
            }
        }
    }

    fun selectSection(sectionId: Int?) {
        val id = if (sectionId == 0) null else sectionId
        _uiState.value = _uiState.value.copy(
            selectedSectionId = id,
            selectedStudent = null,
            searchQuery = "",
            students = emptyList()
        )
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        if (query.length >= 2) {
            searchStudents(query)
        } else {
            _uiState.value = _uiState.value.copy(students = emptyList())
        }
    }

    private fun searchStudents(query: String) {
        val yearId = _uiState.value.selectedAcademicYearId
        val classId = _uiState.value.selectedClassId
        val sectionId = _uiState.value.selectedSectionId
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true)
            try {
                val res = apiService.getStudentsDirectory(
                    query = query,
                    academicYearId = yearId,
                    classId = classId,
                    sectionId = sectionId,
                    enrolled = true,
                    page = 0,
                    size = 20
                )
                if (res.isSuccessful) {
                    val students = res.body()?.data?.content ?: emptyList()
                    _uiState.value = _uiState.value.copy(students = students, isSearching = false)
                } else {
                    _uiState.value = _uiState.value.copy(isSearching = false)
                }
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isSearching = false)
            }
        }
    }

    fun selectStudent(student: StudentDirectoryDTO) {
        _uiState.value = _uiState.value.copy(
            selectedStudent = student,
            searchQuery = "${student.firstName} ${student.lastName}",
            students = emptyList()
        )
    }

    fun clearStudent() {
        _uiState.value = _uiState.value.copy(
            selectedStudent = null,
            searchQuery = "",
            students = emptyList()
        )
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    class Factory(private val apiService: ApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StudentPerformanceSelectorViewModel(apiService) as T
        }
    }
}
