package com.school.gdsportal.ui.admin.reports.classattendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.AcademicYear
import com.school.gdsportal.data.remote.SchoolClass
import com.school.gdsportal.data.remote.Section
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class ClassAttendanceReportSelectorUiState(
    val isLoading: Boolean = true,
    val error: String? = null,

    val academicYears: List<AcademicYear> = emptyList(),
    val selectedAcademicYearId: Int? = null,

    val classes: List<SchoolClass> = emptyList(),
    val selectedClassId: Int? = null,

    val sections: List<Section> = emptyList(),
    val selectedSectionId: Int? = null,

    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR)
)

class ClassAttendanceReportSelectorViewModel(private val apiService: ApiService) : ViewModel() {

    private val _uiState = MutableStateFlow(ClassAttendanceReportSelectorUiState())
    val uiState: StateFlow<ClassAttendanceReportSelectorUiState> = _uiState.asStateFlow()

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
        _uiState.value = _uiState.value.copy(
            selectedAcademicYearId = yearId,
            selectedClassId = null,
            selectedSectionId = null,
            sections = emptyList()
        )
    }

    fun selectClass(classId: Int?) {
        val id = if (classId == 0) null else classId
        _uiState.value = _uiState.value.copy(
            selectedClassId = id,
            selectedSectionId = null,
            sections = emptyList()
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
        _uiState.value = _uiState.value.copy(selectedSectionId = id)
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
            return ClassAttendanceReportSelectorViewModel(apiService) as T
        }
    }
}
