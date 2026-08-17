package com.school.gdsportal.ui.admin.reports.teacherperformance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.Section
import com.school.gdsportal.data.remote.dto.TeacherPerformanceReportData
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TeacherPerformanceReportUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val reportData: TeacherPerformanceReportData? = null,
    
    val teacherName: String = "",
    val academicYearName: String = "",
    val sectionLookup: Map<Long, String> = emptyMap()
)

class TeacherPerformanceReportViewModel(
    private val apiService: ApiService,
    private val teacherId: Long,
    private val academicYearId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherPerformanceReportUiState())
    val uiState: StateFlow<TeacherPerformanceReportUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                coroutineScope {
                    val reportDef = async { apiService.getTeacherPerformanceReport(teacherId, academicYearId) }
                    val teachersDef = async { apiService.getAllTeachers() }
                    val yearsDef = async { apiService.getAcademicYears() }
                    val sectionsDef = async { apiService.getAllSections() }

                    val reportRes = reportDef.await()
                    val teachersRes = teachersDef.await()
                    val yearsRes = yearsDef.await()
                    val sectionsRes = sectionsDef.await()

                    if (reportRes.isSuccessful) {
                        val teachers = teachersRes.body()?.data ?: emptyList()
                        val years = yearsRes.body()?.data ?: emptyList()
                        val sections = sectionsRes.body()?.data ?: emptyList()

                        val teacher = teachers.find { it.teacherId == teacherId }
                        val tName = teacher?.let { "${it.firstName} ${it.lastName}" } ?: "Unknown Teacher"
                        val yearName = years.find { it.academicYearId == academicYearId }?.yearName ?: "Unknown Year"

                        val secMap = sections.associate { it.sectionId.toLong() to it.sectionName }

                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            reportData = reportRes.body()?.data,
                            teacherName = tName,
                            academicYearName = yearName,
                            sectionLookup = secMap
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Failed to load report."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Network error while loading report."
                )
            }
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    class Factory(
        private val apiService: ApiService,
        private val teacherId: Long,
        private val academicYearId: Int
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TeacherPerformanceReportViewModel(apiService, teacherId, academicYearId) as T
        }
    }
}
