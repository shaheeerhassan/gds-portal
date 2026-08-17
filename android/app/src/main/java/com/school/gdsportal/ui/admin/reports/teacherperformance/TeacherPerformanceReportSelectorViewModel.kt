package com.school.gdsportal.ui.admin.reports.teacherperformance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.AcademicYear
import com.school.gdsportal.data.remote.Teacher
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TeacherPerformanceReportSelectorUiState(
    val isLoading: Boolean = true,
    val error: String? = null,

    val teachers: List<Teacher> = emptyList(),
    val filteredTeachers: List<Teacher> = emptyList(),
    val searchQuery: String = "",
    val isDropdownExpanded: Boolean = false,
    val selectedTeacher: Teacher? = null,

    val academicYears: List<AcademicYear> = emptyList(),
    val selectedAcademicYearId: Int? = null
)

class TeacherPerformanceReportSelectorViewModel(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherPerformanceReportSelectorUiState())
    val uiState: StateFlow<TeacherPerformanceReportSelectorUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                coroutineScope {
                    val teachersDef = async { apiService.getAllTeachers() }
                    val yearsDef = async { apiService.getAcademicYears() }

                    val teachersRes = teachersDef.await()
                    val yearsRes = yearsDef.await()

                    if (teachersRes.isSuccessful && yearsRes.isSuccessful) {
                        val teachers = teachersRes.body()?.data ?: emptyList()
                        val years = yearsRes.body()?.data ?: emptyList()

                        val defaultYearId = years.firstOrNull { it.isCurrent }?.academicYearId
                            ?: years.firstOrNull()?.academicYearId

                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            teachers = teachers,
                            academicYears = years,
                            selectedAcademicYearId = defaultYearId
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to load required data.")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Network error while loading data.")
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        val filtered = if (query.isEmpty()) {
            emptyList()
        } else {
            _uiState.value.teachers.filter {
                it.firstName.contains(query, ignoreCase = true) ||
                it.lastName.contains(query, ignoreCase = true) ||
                it.employeeId.contains(query, ignoreCase = true)
            }
        }
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredTeachers = filtered,
            isDropdownExpanded = filtered.isNotEmpty(),
            selectedTeacher = null
        )
    }

    fun selectTeacher(teacher: Teacher) {
        _uiState.value = _uiState.value.copy(
            selectedTeacher = teacher,
            searchQuery = "${teacher.firstName} ${teacher.lastName}",
            isDropdownExpanded = false
        )
    }

    fun dismissDropdown() {
        _uiState.value = _uiState.value.copy(isDropdownExpanded = false)
    }

    fun selectAcademicYear(yearId: Int) {
        _uiState.value = _uiState.value.copy(selectedAcademicYearId = yearId)
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    class Factory(private val apiService: ApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TeacherPerformanceReportSelectorViewModel(apiService) as T
        }
    }
}
