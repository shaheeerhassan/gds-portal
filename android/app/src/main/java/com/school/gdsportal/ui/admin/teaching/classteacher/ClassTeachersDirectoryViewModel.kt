package com.school.gdsportal.ui.admin.teaching.classteacher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.AcademicYear
import com.school.gdsportal.data.remote.SchoolClass
import com.school.gdsportal.data.remote.Section
import com.school.gdsportal.data.remote.Teacher
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ClassTeacherAssignmentRow(
    val section: Section,
    val schoolClass: SchoolClass?,
    val teacher: Teacher?,
    val assignmentId: Long?
)

data class ClassTeachersDirectoryUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val academicYears: List<AcademicYear> = emptyList(),
    val selectedAcademicYearId: Int? = null,
    val assignments: List<ClassTeacherAssignmentRow> = emptyList(),
    val error: String? = null
)

class ClassTeachersDirectoryViewModel(private val apiService: ApiService) : ViewModel() {

    private val _uiState = MutableStateFlow(ClassTeachersDirectoryUiState())
    val uiState: StateFlow<ClassTeachersDirectoryUiState> = _uiState.asStateFlow()

    private var allClasses: List<SchoolClass> = emptyList()
    private var allTeachers: List<Teacher> = emptyList()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // Load Academic Years
                val yearsRes = apiService.getAcademicYears()
                if (yearsRes.isSuccessful) {
                    val years = yearsRes.body()?.data ?: emptyList()
                    _uiState.value = _uiState.value.copy(academicYears = years)

                    // Find current
                    val currentRes = apiService.getCurrentAcademicYear()
                    var selectedYearId = years.firstOrNull()?.academicYearId
                    if (currentRes.isSuccessful) {
                        val current = currentRes.body()?.data
                        if (current != null) {
                            selectedYearId = current.academicYearId
                        }
                    }

                    // Load classes and teachers for local mapping
                    coroutineScope {
                        val classesDef = async { apiService.getClasses() }
                        val teachersDef = async { apiService.getAllTeachers() }

                        val cRes = classesDef.await()
                        if (cRes.isSuccessful) {
                            allClasses = cRes.body()?.data ?: emptyList()
                        }

                        val tRes = teachersDef.await()
                        if (tRes.isSuccessful) {
                            allTeachers = tRes.body()?.data ?: emptyList()
                        }
                    }

                    if (selectedYearId != null) {
                        _uiState.value = _uiState.value.copy(selectedAcademicYearId = selectedYearId)
                        loadAssignmentsForYear(selectedYearId, isRefresh = false)
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to load academic years")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Network error: ${e.message}")
            }
        }
    }

    fun selectAcademicYear(yearId: Int) {
        if (_uiState.value.selectedAcademicYearId == yearId) return
        _uiState.value = _uiState.value.copy(selectedAcademicYearId = yearId)
        loadAssignmentsForYear(yearId, isRefresh = false)
    }

    fun refresh() {
        val yearId = _uiState.value.selectedAcademicYearId
        if (yearId != null) {
            loadAssignmentsForYear(yearId, isRefresh = true)
        }
    }

    private fun loadAssignmentsForYear(academicYearId: Int, isRefresh: Boolean) {
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            }

            try {
                // 1. Get all sections
                val sectionsRes = apiService.getAllSections()
                if (sectionsRes.isSuccessful) {
                    val allSections = sectionsRes.body()?.data ?: emptyList()
                    val yearSections = allSections.filter { it.academicYearId == academicYearId }

                    // 2. Fetch assignments concurrently
                    val assignmentRows = coroutineScope {
                        yearSections.map { section ->
                            async {
                                var teacher: Teacher? = null
                                var assignmentId: Long? = null
                                try {
                                    val assignRes = apiService.getClassTeacherBySection(section.sectionId)
                                    if (assignRes.isSuccessful) {
                                        val assignment = assignRes.body()?.data
                                        if (assignment != null && assignment.isActive) {
                                            assignmentId = assignment.assignmentId
                                            teacher = allTeachers.find { it.teacherId == assignment.teacherId }
                                        }
                                    }
                                } catch (e: Exception) {
                                    // Ignore individual errors, just show unassigned
                                }
                                
                                val schoolClass = allClasses.find { it.classId == section.classId }
                                
                                ClassTeacherAssignmentRow(
                                    section = section,
                                    schoolClass = schoolClass,
                                    teacher = teacher,
                                    assignmentId = assignmentId
                                )
                            }
                        }.awaitAll()
                    }

                    // Sort by class level then section name
                    val sortedRows = assignmentRows.sortedWith(
                        compareBy<ClassTeacherAssignmentRow> { it.schoolClass?.numericLevel ?: 0 }
                            .thenBy { it.section.sectionName }
                    )

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        assignments = sortedRows
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = "Failed to load sections"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = "Network error: ${e.message}"
                )
            }
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    class Factory(private val apiService: ApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ClassTeachersDirectoryViewModel(apiService) as T
        }
    }
}
