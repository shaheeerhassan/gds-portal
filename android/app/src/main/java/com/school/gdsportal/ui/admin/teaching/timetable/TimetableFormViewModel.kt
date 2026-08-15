package com.school.gdsportal.ui.admin.teaching.timetable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.AcademicYear
import com.school.gdsportal.data.remote.Period
import com.school.gdsportal.data.remote.SchoolClass
import com.school.gdsportal.data.remote.Section
import com.school.gdsportal.data.remote.Subject
import com.school.gdsportal.data.remote.Teacher
import com.school.gdsportal.data.remote.Timetable
import com.school.gdsportal.data.remote.TeacherSubjectDTO
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TimetableFormUiState(
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false,
    val error: String? = null,
    
    val isEditMode: Boolean = false,
    val timetableId: Long? = null,
    
    // Lookups
    val academicYears: List<AcademicYear> = emptyList(),
    val classes: List<SchoolClass> = emptyList(),
    val sections: List<Section> = emptyList(),
    val allTeachers: List<Teacher> = emptyList(),
    val subjects: List<Subject> = emptyList(),
    val periods: List<Period> = emptyList(),
    val allTeacherSubjects: List<TeacherSubjectDTO> = emptyList(),
    
    val daysOfWeek: List<String> = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"),
    
    // Selected Values
    val selectedAcademicYearId: Int? = null,
    val selectedClassId: Int? = null,
    val selectedSectionId: Int? = null,
    val selectedDayOfWeek: String? = null,
    val selectedPeriodId: Int? = null,
    val selectedSubjectId: Int? = null,
    val selectedTeacherId: Long? = null
) {
    val isFormValid: Boolean
        get() = selectedAcademicYearId != null &&
                selectedSectionId != null &&
                selectedDayOfWeek != null &&
                selectedPeriodId != null &&
                selectedSubjectId != null &&
                selectedTeacherId != null

    // Helper property to get filtered teachers based on selected subject and section
    val availableTeachers: List<Teacher>
        get() {
            if (selectedSectionId == null || selectedSubjectId == null) return emptyList()
            val validTeacherIds = allTeacherSubjects
                .filter { it.sectionId == selectedSectionId && it.subjectId == selectedSubjectId }
                .map { it.teacherId }
                .toSet()
            return allTeachers.filter { validTeacherIds.contains(it.teacherId) }
        }
}

class TimetableFormViewModel(private val apiService: ApiService) : ViewModel() {

    private val _uiState = MutableStateFlow(TimetableFormUiState())
    val uiState: StateFlow<TimetableFormUiState> = _uiState.asStateFlow()

    fun loadInitialData(
        prefillYearId: Int?, 
        prefillClassId: Int?, 
        prefillSectionId: Int?,
        editEntry: TimetableDisplay? = null
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                coroutineScope {
                    val yearsDef = async { apiService.getAcademicYears() }
                    val classesDef = async { apiService.getClasses() }
                    val teachersDef = async { apiService.getAllTeachers() }
                    val subjectsDef = async { apiService.getSubjects() }
                    val periodsDef = async { apiService.getAllPeriods() }

                    val years = yearsDef.await().body()?.data ?: emptyList()
                    val classes = classesDef.await().body()?.data ?: emptyList()
                    val teachers = teachersDef.await().body()?.data ?: emptyList()
                    val subjects = subjectsDef.await().body()?.data ?: emptyList()
                    val periods = periodsDef.await().body()?.data ?: emptyList()

                    if (editEntry != null) {
                        _uiState.value = _uiState.value.copy(
                            academicYears = years,
                            classes = classes,
                            allTeachers = teachers,
                            subjects = subjects,
                            periods = periods,
                            isEditMode = true,
                            timetableId = editEntry.timetableId,
                            selectedAcademicYearId = editEntry.academicYearId,
                            selectedClassId = editEntry.classId,
                            selectedSectionId = editEntry.sectionId,
                            selectedDayOfWeek = editEntry.dayOfWeek,
                            selectedPeriodId = editEntry.periodId,
                            selectedSubjectId = editEntry.subjectId,
                            selectedTeacherId = editEntry.teacherId,
                            isLoading = true // wait for loadSections and loadTeacherAssignments
                        )
                        loadSections(editEntry.classId, editEntry.academicYearId)
                        loadTeacherAssignments(teachers, editEntry.academicYearId)
                    } else {
                        val yearIdToUse = prefillYearId ?: years.firstOrNull()?.academicYearId
                        _uiState.value = _uiState.value.copy(
                            academicYears = years,
                            classes = classes,
                            allTeachers = teachers,
                            subjects = subjects,
                            periods = periods,
                            isEditMode = false,
                            selectedAcademicYearId = yearIdToUse,
                            selectedClassId = prefillClassId,
                            selectedSectionId = prefillSectionId,
                            isLoading = true // wait for loadTeacherAssignments
                        )
                        if (prefillClassId != null && yearIdToUse != null) {
                            loadSections(prefillClassId, yearIdToUse)
                        }
                        if (yearIdToUse != null) {
                            loadTeacherAssignments(teachers, yearIdToUse)
                        } else {
                            _uiState.value = _uiState.value.copy(isLoading = false)
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to load form data: ${e.message}")
            }
        }
    }

    private suspend fun loadTeacherAssignments(teachers: List<Teacher>, yearId: Int) {
        try {
            val assignments = coroutineScope {
                teachers.map { teacher ->
                    async {
                        var list = emptyList<TeacherSubjectDTO>()
                        try {
                            val res = apiService.getTeacherSubjects(teacher.teacherId, yearId)
                            if (res.isSuccessful) {
                                list = res.body()?.data ?: emptyList()
                            }
                        } catch (e: Exception) {
                            // Ignore individual fails
                        }
                        list
                    }
                }.awaitAll().flatten()
            }
            _uiState.value = _uiState.value.copy(allTeacherSubjects = assignments, isLoading = false)
            
            // if we are editing or something and teacher is not in valid list, we clear it (or leave it if it was legacy)
            // But let's leave as is for Edit.
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(isLoading = false)
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
                // Ignore failure
            }
        }
    }

    fun selectAcademicYear(yearId: Int) {
        if (_uiState.value.selectedAcademicYearId == yearId) return
        _uiState.value = _uiState.value.copy(
            selectedAcademicYearId = yearId,
            selectedClassId = null,
            selectedSectionId = null,
            selectedTeacherId = null,
            sections = emptyList(),
            isLoading = true
        )
        viewModelScope.launch {
            loadTeacherAssignments(_uiState.value.allTeachers, yearId)
        }
    }

    fun selectClass(classId: Int) {
        _uiState.value = _uiState.value.copy(
            selectedClassId = classId,
            selectedSectionId = null,
            selectedTeacherId = null,
            sections = emptyList()
        )
        val yearId = _uiState.value.selectedAcademicYearId
        if (yearId != null) {
            loadSections(classId, yearId)
        }
    }

    fun selectSection(sectionId: Int) {
        _uiState.value = _uiState.value.copy(
            selectedSectionId = sectionId,
            selectedTeacherId = null
        )
    }

    fun selectDay(day: String) {
        _uiState.value = _uiState.value.copy(selectedDayOfWeek = day)
    }

    fun selectPeriod(periodId: Int) {
        _uiState.value = _uiState.value.copy(selectedPeriodId = periodId)
    }

    fun selectSubject(subjectId: Int) {
        _uiState.value = _uiState.value.copy(
            selectedSubjectId = subjectId,
            selectedTeacherId = null
        )
    }

    fun selectTeacher(teacherId: Long) {
        _uiState.value = _uiState.value.copy(selectedTeacherId = teacherId)
    }

    fun submit() {
        val state = _uiState.value
        if (!state.isFormValid) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, error = null)
            
            val timetable = Timetable(
                timetableId = state.timetableId ?: 0,
                sectionId = state.selectedSectionId!!,
                subjectId = state.selectedSubjectId!!,
                teacherId = state.selectedTeacherId!!,
                periodId = state.selectedPeriodId!!,
                dayOfWeek = state.selectedDayOfWeek!!,
                academicYearId = state.selectedAcademicYearId!!
            )
            
            try {
                val res = if (state.isEditMode) {
                    apiService.updateTimetable(state.timetableId!!, timetable)
                } else {
                    apiService.createTimetable(timetable)
                }
                
                if (res.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isSubmitting = false, submitSuccess = true)
                } else {
                    val errString = res.errorBody()?.string()
                    val errMsg = try {
                        org.json.JSONObject(errString!!).getString("message")
                    } catch (e: Exception) {
                        "Submission failed"
                    }
                    _uiState.value = _uiState.value.copy(isSubmitting = false, error = errMsg)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSubmitting = false, error = "Network error: ${e.message}")
            }
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    class Factory(private val apiService: ApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TimetableFormViewModel(apiService) as T
        }
    }
}
