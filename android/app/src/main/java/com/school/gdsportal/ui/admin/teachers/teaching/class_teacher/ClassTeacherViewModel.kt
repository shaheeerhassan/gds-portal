package com.school.gdsportal.ui.admin.teachers.teaching.class_teacher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.AcademicYear
import com.school.gdsportal.data.remote.ClassTeacherAssignment
import com.school.gdsportal.data.remote.SchoolClass
import com.school.gdsportal.data.remote.Section
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.async

data class ClassTeacherAssignmentUiModel(
    val assignmentId: Long,
    val sectionId: Int,
    val academicYearId: Int,
    val className: String,
    val sectionName: String,
    val academicYearName: String,
    val assignedDate: String?,
    val removedDate: String?,
    val isActive: Boolean
)

data class ClassTeacherUiState(
    val isLoading: Boolean = true,
    val isAssigning: Boolean = false,
    val isRemoving: Boolean = false,
    val activeAssignments: List<ClassTeacherAssignmentUiModel> = emptyList(),
    val historicalAssignments: List<ClassTeacherAssignmentUiModel> = emptyList(),
    val error: String? = null,
    val assignError: String? = null,
    val removeError: String? = null,
    val academicYears: List<AcademicYear> = emptyList(),
    val classes: List<SchoolClass> = emptyList(),
    val availableSections: List<Section> = emptyList() // Sections populated when year & class selected
)

class ClassTeacherViewModel(
    private val teacherId: Long,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClassTeacherUiState())
    val uiState: StateFlow<ClassTeacherUiState> = _uiState.asStateFlow()

    private var allSectionsCache: List<Section> = emptyList()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // Fetch assignments, academic years, classes, and sections concurrently
                val assignmentsDeferred = async { apiService.getClassTeacherHistory(teacherId) }
                val academicYearsDeferred = async { apiService.getAcademicYears() }
                val classesDeferred = async { apiService.getClasses() }
                val sectionsDeferred = async { apiService.getAllSections() }

                val assignmentsResponse = assignmentsDeferred.await()
                val academicYearsResponse = academicYearsDeferred.await()
                val classesResponse = classesDeferred.await()
                val sectionsResponse = sectionsDeferred.await()

                if (assignmentsResponse.isSuccessful && academicYearsResponse.isSuccessful && classesResponse.isSuccessful && sectionsResponse.isSuccessful) {
                    val rawAssignments = assignmentsResponse.body()?.data ?: emptyList<ClassTeacherAssignment>()
                    val academicYears = academicYearsResponse.body()?.data ?: emptyList<AcademicYear>()
                    val classes = classesResponse.body()?.data ?: emptyList<SchoolClass>()
                    val sections = sectionsResponse.body()?.data ?: emptyList<Section>()

                    allSectionsCache = sections

                    val uiModels = rawAssignments.map { assignment ->
                        val section = sections.find { it.sectionId == assignment.sectionId }
                        val schoolClass = classes.find { it.classId == section?.classId }
                        val year = academicYears.find { it.academicYearId == assignment.academicYearId }

                        ClassTeacherAssignmentUiModel(
                            assignmentId = assignment.assignmentId,
                            sectionId = assignment.sectionId,
                            academicYearId = assignment.academicYearId,
                            className = schoolClass?.className ?: "Unknown Class",
                            sectionName = section?.sectionName ?: "Unknown Section",
                            academicYearName = year?.yearName ?: "Unknown Year",
                            assignedDate = assignment.assignedDate,
                            removedDate = assignment.removedDate,
                            isActive = assignment.isActive
                        )
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            activeAssignments = uiModels.filter { m -> m.isActive }.sortedByDescending { m -> m.assignedDate },
                            historicalAssignments = uiModels.filter { m -> !m.isActive }.sortedByDescending { m -> m.assignedDate },
                            academicYears = academicYears,
                            classes = classes
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load data") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Network error: ${e.message}") }
            }
        }
    }

    fun loadSectionsForAssignment(classId: Int, academicYearId: Int) {
        // We can just filter from cache if we want, but the requirement specifies GET /api/sections/class/{classId}/{academicYearId}
        // Let's do the API call to ensure we only get sections that exist for that year and class.
        viewModelScope.launch {
            try {
                val response = apiService.getSections(classId, academicYearId)
                if (response.isSuccessful) {
                    _uiState.update { it.copy(availableSections = response.body()?.data ?: emptyList()) }
                }
            } catch (e: Exception) {
                // Ignore or handle
            }
        }
    }

    fun clearSections() {
        _uiState.update { it.copy(availableSections = emptyList()) }
    }

    fun assignClassTeacher(sectionId: Int, academicYearId: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAssigning = true, assignError = null) }
            try {
                val assignment = ClassTeacherAssignment(
                    assignmentId = 0, // Ignored on create
                    teacherId = teacherId,
                    sectionId = sectionId,
                    academicYearId = academicYearId,
                    assignedDate = null,
                    removedDate = null,
                    isActive = true
                )
                val response = apiService.assignClassTeacher(assignment)
                if (response.isSuccessful) {
                    _uiState.update { it.copy(isAssigning = false) }
                    loadData() // Refresh
                    onSuccess()
                } else {
                    _uiState.update { it.copy(isAssigning = false, assignError = "Failed to assign class teacher") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isAssigning = false, assignError = "Network error: ${e.message}") }
            }
        }
    }

    fun removeClassTeacher(sectionId: Int, academicYearId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRemoving = true, removeError = null) }
            try {
                val response = apiService.removeClassTeacher(sectionId, academicYearId)
                if (response.isSuccessful) {
                    _uiState.update { it.copy(isRemoving = false) }
                    loadData() // Refresh list
                } else {
                    _uiState.update { it.copy(isRemoving = false, removeError = "Failed to remove assignment") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isRemoving = false, removeError = "Network error: ${e.message}") }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null, assignError = null, removeError = null) }
    }

    companion object {
        fun provideFactory(teacherId: Long, apiService: ApiService): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ClassTeacherViewModel(teacherId, apiService) as T
                }
            }
    }
}
