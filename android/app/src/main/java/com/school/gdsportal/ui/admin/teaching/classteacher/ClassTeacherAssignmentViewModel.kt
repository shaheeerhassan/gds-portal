package com.school.gdsportal.ui.admin.teaching.classteacher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.AcademicYear
import com.school.gdsportal.data.remote.ClassTeacherAssignment
import com.school.gdsportal.data.remote.SchoolClass
import com.school.gdsportal.data.remote.Section
import com.school.gdsportal.data.remote.Teacher
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ClassTeacherAssignmentUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val saveSuccess: Boolean = false,
    
    // Loaded Data
    val academicYears: List<AcademicYear> = emptyList(),
    val classes: List<SchoolClass> = emptyList(),
    val allSections: List<Section> = emptyList(),
    val availableSections: List<Section> = emptyList(),
    val teachers: List<Teacher> = emptyList(),
    
    // Existing Assignment (if any)
    val existingAssignment: ClassTeacherAssignment? = null,
    val isDetailMode: Boolean = false,
    val isChangeMode: Boolean = false,
    
    // Form Selections
    val selectedAcademicYearId: Int? = null,
    val selectedClassId: Int? = null,
    val selectedSectionId: Int? = null,
    val selectedTeacherId: Long? = null,
    
    val error: String? = null
)

class ClassTeacherAssignmentViewModel(
    private val sectionId: Int?,
    private val academicYearId: Int?,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClassTeacherAssignmentUiState())
    val uiState: StateFlow<ClassTeacherAssignmentUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                coroutineScope {
                    val yearsDef = async { apiService.getAcademicYears() }
                    val classesDef = async { apiService.getClasses() }
                    val sectionsDef = async { apiService.getAllSections() }
                    val teachersDef = async { apiService.getAllTeachers() }

                    val years = yearsDef.await().body()?.data ?: emptyList()
                    val classes = classesDef.await().body()?.data ?: emptyList()
                    val sections = sectionsDef.await().body()?.data ?: emptyList()
                    val teachers = teachersDef.await().body()?.data ?: emptyList()

                    var existingAssign: ClassTeacherAssignment? = null
                    var isDetail = false
                    
                    var selYearId = academicYearId
                    var selSectionId = sectionId
                    var selClassId: Int? = null
                    var selTeacherId: Long? = null

                    if (selSectionId != null && selYearId != null) {
                        // Look up assignment
                        try {
                            val assignRes = apiService.getClassTeacherBySection(selSectionId)
                            if (assignRes.isSuccessful) {
                                val assignment = assignRes.body()?.data
                                if (assignment != null && assignment.isActive) {
                                    existingAssign = assignment
                                    selTeacherId = assignment.teacherId
                                    isDetail = true
                                }
                            }
                        } catch (e: Exception) {
                            // Ignored, might be unassigned
                        }
                        
                        // Infer class
                        val targetSection = sections.find { it.sectionId == selSectionId }
                        selClassId = targetSection?.classId
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        academicYears = years,
                        classes = classes,
                        allSections = sections,
                        teachers = teachers,
                        existingAssignment = existingAssign,
                        isDetailMode = isDetail,
                        selectedAcademicYearId = selYearId,
                        selectedClassId = selClassId,
                        selectedSectionId = selSectionId,
                        selectedTeacherId = selTeacherId
                    )
                    
                    updateAvailableSections()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Network error: ${e.message}")
            }
        }
    }

    fun selectAcademicYear(id: Int) {
        if (_uiState.value.isDetailMode && !_uiState.value.isChangeMode) return
        _uiState.value = _uiState.value.copy(
            selectedAcademicYearId = id,
            selectedClassId = null,
            selectedSectionId = null
        )
        updateAvailableSections()
    }

    fun selectClass(id: Int) {
        if (_uiState.value.isDetailMode && !_uiState.value.isChangeMode) return
        _uiState.value = _uiState.value.copy(
            selectedClassId = id,
            selectedSectionId = null
        )
        updateAvailableSections()
    }

    fun selectSection(id: Int) {
        if (_uiState.value.isDetailMode && !_uiState.value.isChangeMode) return
        _uiState.value = _uiState.value.copy(selectedSectionId = id)
    }

    fun selectTeacher(id: Long) {
        _uiState.value = _uiState.value.copy(selectedTeacherId = id)
    }

    fun enterChangeMode() {
        _uiState.value = _uiState.value.copy(isChangeMode = true, isDetailMode = false)
    }

    private fun updateAvailableSections() {
        val state = _uiState.value
        val yId = state.selectedAcademicYearId
        val cId = state.selectedClassId
        if (yId != null && cId != null) {
            val filtered = state.allSections.filter { it.academicYearId == yId && it.classId == cId }
            _uiState.value = _uiState.value.copy(availableSections = filtered)
        } else {
            _uiState.value = _uiState.value.copy(availableSections = emptyList())
        }
    }

    fun submitAssignment() {
        val state = _uiState.value
        val yId = state.selectedAcademicYearId
        val sId = state.selectedSectionId
        val tId = state.selectedTeacherId

        if (yId == null || sId == null || tId == null) {
            _uiState.value = state.copy(error = "Academic Year, Section, and Teacher are required")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            try {
                // If there's an existing assignment to change, remove it first
                if (state.existingAssignment != null && state.existingAssignment.teacherId != tId) {
                    val delRes = apiService.removeClassTeacher(sId, yId)
                    if (!delRes.isSuccessful) {
                        _uiState.value = _uiState.value.copy(isSaving = false, error = "Failed to remove previous assignment")
                        return@launch
                    }
                }

                val newAssignment = ClassTeacherAssignment(
                    assignmentId = 0,
                    teacherId = tId,
                    sectionId = sId,
                    academicYearId = yId,
                    assignedDate = null,
                    removedDate = null,
                    isActive = true
                )
                
                val res = apiService.assignClassTeacher(newAssignment)
                if (res.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
                } else {
                    val errorMsg = try {
                        val errorString = res.errorBody()?.string()
                        if (errorString != null) {
                            org.json.JSONObject(errorString).getString("message")
                        } else {
                            res.message()
                        }
                    } catch (e: Exception) {
                        "Failed to assign teacher"
                    }
                    _uiState.value = _uiState.value.copy(isSaving = false, error = errorMsg)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, error = "Network error: ${e.message}")
            }
        }
    }

    fun removeAssignment() {
        val state = _uiState.value
        val yId = state.selectedAcademicYearId
        val sId = state.selectedSectionId
        if (yId == null || sId == null) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true, error = null)
            try {
                val res = apiService.removeClassTeacher(sId, yId)
                if (res.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isDeleting = false, saveSuccess = true)
                } else {
                    val errorMsg = try {
                        val errorString = res.errorBody()?.string()
                        if (errorString != null) {
                            org.json.JSONObject(errorString).getString("message")
                        } else {
                            res.message()
                        }
                    } catch (e: Exception) {
                        "Failed to remove assignment"
                    }
                    _uiState.value = _uiState.value.copy(isDeleting = false, error = errorMsg)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isDeleting = false, error = "Network error: ${e.message}")
            }
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    class Factory(
        private val sectionId: Int?,
        private val academicYearId: Int?,
        private val apiService: ApiService
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ClassTeacherAssignmentViewModel(sectionId, academicYearId, apiService) as T
        }
    }
}
