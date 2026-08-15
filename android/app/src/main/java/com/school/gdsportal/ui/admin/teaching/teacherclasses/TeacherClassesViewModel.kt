package com.school.gdsportal.ui.admin.teaching.teacherclasses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.AcademicYear
import com.school.gdsportal.data.remote.SchoolClass
import com.school.gdsportal.data.remote.Section
import com.school.gdsportal.data.remote.Teacher
import com.school.gdsportal.data.remote.TeacherClassDTO
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TeacherClassRow(
    val dto: TeacherClassDTO,
    val teacher: Teacher
)

data class TeacherClassesUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    
    val academicYears: List<AcademicYear> = emptyList(),
    val selectedAcademicYearId: Int? = null,
    
    val classes: List<SchoolClass> = emptyList(),
    val selectedClassId: Int? = null,
    
    val sections: List<Section> = emptyList(),
    val selectedSectionId: Int? = null,
    
    val allAssignments: List<TeacherClassRow> = emptyList(),
    val filteredAssignments: List<TeacherClassRow> = emptyList()
)

class TeacherClassesViewModel(private val apiService: ApiService) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherClassesUiState())
    val uiState: StateFlow<TeacherClassesUiState> = _uiState.asStateFlow()

    private var allTeachers: List<Teacher> = emptyList()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                coroutineScope {
                    val yearsDef = async { apiService.getAcademicYears() }
                    val currentYearDef = async { apiService.getCurrentAcademicYear() }
                    val classesDef = async { apiService.getClasses() }
                    val teachersDef = async { apiService.getAllTeachers() }

                    val yearsRes = yearsDef.await()
                    val classesRes = classesDef.await()
                    val teachersRes = teachersDef.await()

                    val years = yearsRes.body()?.data ?: emptyList()
                    val classes = classesRes.body()?.data ?: emptyList()
                    allTeachers = teachersRes.body()?.data ?: emptyList()

                    var selectedYearId = years.firstOrNull()?.academicYearId
                    val currentRes = currentYearDef.await()
                    if (currentRes.isSuccessful) {
                        currentRes.body()?.data?.let { selectedYearId = it.academicYearId }
                    }

                    _uiState.value = _uiState.value.copy(
                        academicYears = years,
                        classes = classes,
                        selectedAcademicYearId = selectedYearId
                    )
                    
                    if (selectedYearId != null) {
                        loadAssignments(selectedYearId!!, isRefresh = false)
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Network error: ${e.message}")
            }
        }
    }

    fun selectAcademicYear(yearId: Int) {
        if (_uiState.value.selectedAcademicYearId == yearId) return
        _uiState.value = _uiState.value.copy(
            selectedAcademicYearId = yearId,
            selectedClassId = null,
            selectedSectionId = null,
            sections = emptyList()
        )
        loadAssignments(yearId, isRefresh = false)
    }

    fun selectClass(classId: Int?) {
        _uiState.value = _uiState.value.copy(selectedClassId = classId, selectedSectionId = null, sections = emptyList())
        if (classId != null) {
            val yearId = _uiState.value.selectedAcademicYearId
            if (yearId != null) {
                loadSections(classId, yearId)
            }
        }
        applyFilters()
    }

    fun selectSection(sectionId: Int?) {
        _uiState.value = _uiState.value.copy(selectedSectionId = sectionId)
        applyFilters()
    }

    fun refresh() {
        val yearId = _uiState.value.selectedAcademicYearId
        if (yearId != null) {
            loadAssignments(yearId, isRefresh = true)
        }
    }

    private fun loadSections(classId: Int, yearId: Int) {
        viewModelScope.launch {
            try {
                val res = apiService.getSections(classId, yearId)
                if (res.isSuccessful) {
                    val sections = res.body()?.data?.filter { it.academicYearId == yearId } ?: emptyList()
                    _uiState.value = _uiState.value.copy(sections = sections)
                }
            } catch (e: Exception) {
                // Ignore failure for filter list
            }
        }
    }

    private fun loadAssignments(yearId: Int, isRefresh: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = !isRefresh,
                isRefreshing = isRefresh,
                error = null
            )
            try {
                val assignmentsRows = coroutineScope {
                    allTeachers.map { teacher ->
                        async {
                            var list = emptyList<TeacherClassDTO>()
                            try {
                                val res = apiService.getTeacherClasses(teacher.teacherId, yearId)
                                if (res.isSuccessful) {
                                    list = res.body()?.data ?: emptyList()
                                }
                            } catch (e: Exception) {
                                // Ignore individual fails
                            }
                            list.map { dto -> TeacherClassRow(dto, teacher) }
                        }
                    }.awaitAll().flatten()
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    allAssignments = assignmentsRows
                )
                applyFilters()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = "Failed to load assignments: ${e.message}"
                )
            }
        }
    }

    private fun applyFilters() {
        val state = _uiState.value
        var filtered = state.allAssignments

        if (state.selectedClassId != null) {
            filtered = filtered.filter { it.dto.classId == state.selectedClassId }
        }
        if (state.selectedSectionId != null) {
            filtered = filtered.filter { it.dto.sectionId == state.selectedSectionId }
        }
        
        // Sort by class name, then section name, then teacher name
        filtered = filtered.sortedWith(
            compareBy<TeacherClassRow> { it.dto.className }
                .thenBy { it.dto.sectionName }
                .thenBy { it.teacher.firstName }
        )

        _uiState.value = _uiState.value.copy(filteredAssignments = filtered)
    }

    fun removeAssignment(teacherClassId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val res = apiService.unassignTeacherClass(teacherClassId)
                if (res.isSuccessful) {
                    // Re-fetch
                    val yearId = _uiState.value.selectedAcademicYearId
                    if (yearId != null) {
                        loadAssignments(yearId, isRefresh = false)
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to remove assignment")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Network error: ${e.message}")
            }
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    class Factory(private val apiService: ApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TeacherClassesViewModel(apiService) as T
        }
    }
}
