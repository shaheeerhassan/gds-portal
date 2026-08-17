package com.school.gdsportal.ui.admin.reports.studentattendancesummary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.AcademicYear
import com.school.gdsportal.data.remote.SchoolClass
import com.school.gdsportal.data.remote.Section
import com.school.gdsportal.data.remote.dto.StudentDirectoryDTO
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StudentAttendanceSummarySelectionUiState(
    val isLoading: Boolean = true,
    val isSearching: Boolean = false,
    val error: String? = null,

    val academicYears: List<AcademicYear> = emptyList(),
    val selectedAcademicYearId: Int? = null,

    val classes: List<SchoolClass> = emptyList(),
    val selectedClassId: Int? = null,

    val sections: List<Section> = emptyList(),
    val selectedSectionId: Int? = null,

    val searchQuery: String = "",
    val searchResults: List<StudentDirectoryDTO> = emptyList(),
    val isDropdownExpanded: Boolean = false,
    val selectedStudent: StudentDirectoryDTO? = null
)

class StudentAttendanceSummarySelectionViewModel(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentAttendanceSummarySelectionUiState())
    val uiState: StateFlow<StudentAttendanceSummarySelectionUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

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

                    val yearsRes = yearsDef.await()
                    val classesRes = classesDef.await()

                    if (yearsRes.isSuccessful && classesRes.isSuccessful) {
                        val years = yearsRes.body()?.data ?: emptyList()
                        val classes = classesRes.body()?.data ?: emptyList()

                        val defaultYearId = years.firstOrNull { it.isCurrent }?.academicYearId
                            ?: years.firstOrNull()?.academicYearId

                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            academicYears = years,
                            classes = classes,
                            selectedAcademicYearId = defaultYearId
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to load initial data.")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Network error while loading data.")
            }
        }
    }

    fun selectAcademicYear(yearId: Int) {
        _uiState.value = _uiState.value.copy(
            selectedAcademicYearId = yearId,
            selectedClassId = null,
            selectedSectionId = null,
            sections = emptyList(),
            selectedStudent = null,
            searchQuery = "",
            searchResults = emptyList(),
            isDropdownExpanded = false
        )
    }

    fun selectClass(classId: Int?) {
        val id = if (classId == 0) null else classId
        _uiState.value = _uiState.value.copy(
            selectedClassId = id,
            selectedSectionId = null,
            sections = emptyList(),
            selectedStudent = null,
            searchQuery = "",
            searchResults = emptyList(),
            isDropdownExpanded = false
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
            } catch (_: Exception) { }
        }
    }

    fun selectSection(sectionId: Int?) {
        val id = if (sectionId == 0) null else sectionId
        _uiState.value = _uiState.value.copy(
            selectedSectionId = id,
            selectedStudent = null,
            searchQuery = "",
            searchResults = emptyList(),
            isDropdownExpanded = false
        )
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            selectedStudent = null
        )

        val yearId = _uiState.value.selectedAcademicYearId
        val classId = _uiState.value.selectedClassId
        val sectionId = _uiState.value.selectedSectionId

        if (yearId == null || classId == null || sectionId == null) return

        searchJob?.cancel()
        if (query.length < 2) {
            _uiState.value = _uiState.value.copy(searchResults = emptyList(), isDropdownExpanded = false)
            return
        }

        searchJob = viewModelScope.launch {
            delay(500)
            _uiState.value = _uiState.value.copy(isSearching = true)
            try {
                val response = apiService.getStudentsDirectory(
                    query = query,
                    academicYearId = yearId,
                    classId = classId,
                    sectionId = sectionId,
                    enrolled = true,
                    page = 0,
                    size = 20
                )
                if (response.isSuccessful) {
                    val content = response.body()?.data?.content ?: emptyList()
                    _uiState.value = _uiState.value.copy(
                        searchResults = content,
                        isDropdownExpanded = content.isNotEmpty(),
                        isSearching = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isSearching = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSearching = false)
            }
        }
    }

    fun selectStudent(student: StudentDirectoryDTO) {
        _uiState.value = _uiState.value.copy(
            selectedStudent = student,
            searchQuery = "${student.firstName} ${student.lastName} (${student.registrationNumber})",
            isDropdownExpanded = false,
            searchResults = emptyList()
        )
    }

    fun dismissDropdown() {
        _uiState.value = _uiState.value.copy(isDropdownExpanded = false)
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    class Factory(private val apiService: ApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StudentAttendanceSummarySelectionViewModel(apiService) as T
        }
    }
}
