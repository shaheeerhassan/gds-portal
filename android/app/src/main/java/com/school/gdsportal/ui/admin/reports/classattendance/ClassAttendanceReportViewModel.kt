package com.school.gdsportal.ui.admin.reports.classattendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.AcademicYear
import com.school.gdsportal.data.remote.SchoolClass
import com.school.gdsportal.data.remote.Section
import com.school.gdsportal.data.remote.dto.ClassAttendanceReportData
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ClassAttendanceReportUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val reportData: ClassAttendanceReportData? = null,
    
    // For displaying context string at top
    val academicYearName: String = "",
    val className: String = "",
    val sectionName: String = ""
)

class ClassAttendanceReportViewModel(
    private val apiService: ApiService,
    private val sectionId: Int,
    private val month: Int,
    private val year: Int,
    private val classId: Int,
    private val academicYearId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClassAttendanceReportUiState())
    val uiState: StateFlow<ClassAttendanceReportUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                coroutineScope {
                    val reportDef = async { apiService.getClassAttendanceReport(sectionId, month, year) }
                    val yearsDef = async { apiService.getAcademicYears() }
                    val classesDef = async { apiService.getClasses() }
                    val sectionsDef = async { apiService.getSections(classId, academicYearId) }

                    val reportRes = reportDef.await()
                    val yearsRes = yearsDef.await()
                    val classesRes = classesDef.await()
                    val sectionsRes = sectionsDef.await()

                    if (reportRes.isSuccessful) {
                        val years = yearsRes.body()?.data ?: emptyList()
                        val classes = classesRes.body()?.data ?: emptyList()
                        val sections = sectionsRes.body()?.data ?: emptyList()

                        val yearName = years.find { it.academicYearId == academicYearId }?.yearName ?: "Unknown Year"
                        val className = classes.find { it.classId == classId }?.className ?: "Unknown Class"
                        val sectionName = sections.find { it.sectionId == sectionId }?.sectionName ?: "Unknown Section"

                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            reportData = reportRes.body()?.data,
                            academicYearName = yearName,
                            className = className,
                            sectionName = sectionName
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
        private val sectionId: Int,
        private val month: Int,
        private val year: Int,
        private val classId: Int,
        private val academicYearId: Int
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ClassAttendanceReportViewModel(apiService, sectionId, month, year, classId, academicYearId) as T
        }
    }
}
