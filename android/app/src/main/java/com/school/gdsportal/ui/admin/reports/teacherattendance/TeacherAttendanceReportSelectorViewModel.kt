package com.school.gdsportal.ui.admin.reports.teacherattendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.Teacher
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class TeacherAttendanceReportSelectorUiState(
    val teachers: List<Teacher> = emptyList(),
    val filteredTeachers: List<Teacher> = emptyList(),
    val searchQuery: String = "",
    val isDropdownExpanded: Boolean = false,
    val selectedTeacher: Teacher? = null,
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val isLoading: Boolean = true,
    val error: String? = null
)

class TeacherAttendanceReportSelectorViewModel(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherAttendanceReportSelectorUiState())
    val uiState: StateFlow<TeacherAttendanceReportSelectorUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = apiService.getAllTeachers()
                if (response.isSuccessful) {
                    val teachers = response.body()?.data ?: emptyList()
                    _uiState.value = _uiState.value.copy(
                        teachers = teachers,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to load teachers.")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Network error while loading teachers.")
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

    fun selectMonth(month: Int) {
        _uiState.value = _uiState.value.copy(selectedMonth = month)
    }

    fun selectYear(year: Int) {
        _uiState.value = _uiState.value.copy(selectedYear = year)
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    class Factory(private val apiService: ApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TeacherAttendanceReportSelectorViewModel(apiService) as T
        }
    }
}
