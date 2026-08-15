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
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TimetableDisplay(
    val timetableId: Long,
    val dayOfWeek: String,
    val subjectId: Int,
    val subjectName: String,
    val teacherId: Long,
    val teacherName: String,
    val periodId: Int,
    val periodNumber: Int,
    val startTime: String,
    val endTime: String,
    val sectionId: Int,
    val sectionName: String,
    val classId: Int,
    val className: String,
    val academicYearId: Int,
    val academicYearName: String
)

data class TimetableDirectoryUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    
    val academicYears: List<AcademicYear> = emptyList(),
    val selectedAcademicYearId: Int? = null,
    
    val classes: List<SchoolClass> = emptyList(),
    val selectedClassId: Int? = null,
    
    val sections: List<Section> = emptyList(),
    val selectedSectionId: Int? = null,
    
    val timetableEntries: List<TimetableDisplay> = emptyList(),
    
    // Lookups
    val teachersMap: Map<Long, Teacher> = emptyMap(),
    val subjectsMap: Map<Int, Subject> = emptyMap(),
    val periodsMap: Map<Int, Period> = emptyMap()
)

class TimetableDirectoryViewModel(private val apiService: ApiService) : ViewModel() {

    private val _uiState = MutableStateFlow(TimetableDirectoryUiState())
    val uiState: StateFlow<TimetableDirectoryUiState> = _uiState.asStateFlow()

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
                    
                    // Lookups
                    val teachersDef = async { apiService.getAllTeachers() }
                    val subjectsDef = async { apiService.getSubjects() }
                    val periodsDef = async { apiService.getAllPeriods() }

                    val years = yearsDef.await().body()?.data ?: emptyList()
                    val classes = classesDef.await().body()?.data ?: emptyList()
                    
                    val teachers = teachersDef.await().body()?.data ?: emptyList()
                    val subjects = subjectsDef.await().body()?.data ?: emptyList()
                    val periods = periodsDef.await().body()?.data ?: emptyList()

                    val teachersMap = teachers.associateBy { it.teacherId }
                    val subjectsMap = subjects.associateBy { it.subjectId }
                    val periodsMap = periods.associateBy { it.periodId }

                    var selectedYearId = years.firstOrNull()?.academicYearId
                    val currentRes = currentYearDef.await()
                    if (currentRes.isSuccessful) {
                        currentRes.body()?.data?.let { selectedYearId = it.academicYearId }
                    }

                    _uiState.value = _uiState.value.copy(
                        academicYears = years,
                        classes = classes,
                        selectedAcademicYearId = selectedYearId,
                        teachersMap = teachersMap,
                        subjectsMap = subjectsMap,
                        periodsMap = periodsMap,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to load initial data: ${e.message}")
            }
        }
    }

    fun selectAcademicYear(yearId: Int) {
        if (_uiState.value.selectedAcademicYearId == yearId) return
        _uiState.value = _uiState.value.copy(
            selectedAcademicYearId = yearId,
            selectedClassId = null,
            selectedSectionId = null,
            sections = emptyList(),
            timetableEntries = emptyList()
        )
    }

    fun selectClass(classId: Int?) {
        _uiState.value = _uiState.value.copy(
            selectedClassId = classId,
            selectedSectionId = null,
            sections = emptyList(),
            timetableEntries = emptyList()
        )
        if (classId != null) {
            val yearId = _uiState.value.selectedAcademicYearId
            if (yearId != null) {
                loadSections(classId, yearId)
            }
        }
    }

    fun selectSection(sectionId: Int?) {
        _uiState.value = _uiState.value.copy(selectedSectionId = sectionId, timetableEntries = emptyList())
        if (sectionId != null) {
            val yearId = _uiState.value.selectedAcademicYearId
            if (yearId != null) {
                loadTimetable(sectionId, yearId)
            }
        }
    }

    fun refresh() {
        val sectionId = _uiState.value.selectedSectionId
        val yearId = _uiState.value.selectedAcademicYearId
        if (sectionId != null && yearId != null) {
            loadTimetable(sectionId, yearId)
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

    private fun loadTimetable(sectionId: Int, yearId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val res = apiService.getTimetableBySection(sectionId, yearId)
                if (res.isSuccessful) {
                    val rawTimetable = res.body()?.data ?: emptyList()
                    val displayEntries = rawTimetable.mapNotNull { buildDisplayModel(it) }
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        timetableEntries = displayEntries
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to load timetable")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Network error: ${e.message}")
            }
        }
    }

    private fun buildDisplayModel(raw: Timetable): TimetableDisplay? {
        val state = _uiState.value
        val teacher = state.teachersMap[raw.teacherId] ?: return null
        val subject = state.subjectsMap[raw.subjectId] ?: return null
        val period = state.periodsMap[raw.periodId] ?: return null
        val section = state.sections.find { it.sectionId == raw.sectionId } ?: return null
        val schoolClass = state.classes.find { it.classId == section.classId } ?: return null
        val academicYear = state.academicYears.find { it.academicYearId == raw.academicYearId } ?: return null

        return TimetableDisplay(
            timetableId = raw.timetableId,
            dayOfWeek = raw.dayOfWeek,
            subjectId = raw.subjectId,
            subjectName = subject.subjectName,
            teacherId = raw.teacherId,
            teacherName = "${teacher.firstName} ${teacher.lastName}",
            periodId = raw.periodId,
            periodNumber = period.periodNumber,
            startTime = period.startTime,
            endTime = period.endTime,
            sectionId = raw.sectionId,
            sectionName = section.sectionName,
            classId = schoolClass.classId,
            className = schoolClass.className,
            academicYearId = raw.academicYearId,
            academicYearName = academicYear.yearName
        )
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    class Factory(private val apiService: ApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TimetableDirectoryViewModel(apiService) as T
        }
    }
}
