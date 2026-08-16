package com.school.gdsportal.ui.admin.attendance.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.*
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class StudentAttendanceUiState(
    val isLoading: Boolean = true,
    val error: String? = null,

    val academicYears: List<AcademicYear> = emptyList(),
    val classes: List<SchoolClass> = emptyList(),
    val sections: List<Section> = emptyList(),

    val selectedAcademicYearId: Int? = null,
    val selectedClassId: Int? = null,
    val selectedSectionId: Int? = null,
    val selectedDate: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),

    val attendanceRecords: List<StudentAttendanceDisplay> = emptyList()
)

class StudentAttendanceViewModel(private val apiService: ApiService) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentAttendanceUiState())
    val uiState: StateFlow<StudentAttendanceUiState> = _uiState.asStateFlow()

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
                        classes = classes,
                        selectedAcademicYearId = defaultYearId,
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
            selectedSectionId = null,
            sections = emptyList(),
            attendanceRecords = emptyList()
        )
        loadSectionsIfPossible()
    }

    fun selectClass(classId: Int) {
        if (_uiState.value.selectedClassId == classId) return
        _uiState.value = _uiState.value.copy(
            selectedClassId = classId,
            selectedSectionId = null,
            sections = emptyList(),
            attendanceRecords = emptyList()
        )
        loadSectionsIfPossible()
    }

    fun selectSection(sectionId: Int) {
        if (_uiState.value.selectedSectionId == sectionId) return
        _uiState.value = _uiState.value.copy(
            selectedSectionId = sectionId,
            attendanceRecords = emptyList()
        )
        loadAttendance()
    }

    fun selectDate(date: String) {
        if (_uiState.value.selectedDate == date) return
        _uiState.value = _uiState.value.copy(
            selectedDate = date,
            attendanceRecords = emptyList()
        )
        loadAttendance()
    }

    private fun loadSectionsIfPossible() {
        val classId = _uiState.value.selectedClassId
        val yearId = _uiState.value.selectedAcademicYearId
        if (classId != null && yearId != null) {
            viewModelScope.launch {
                try {
                    val res = apiService.getSections(classId, yearId)
                    if (res.isSuccessful) {
                        val sections = res.body()?.data ?: emptyList()
                        _uiState.value = _uiState.value.copy(sections = sections)
                    }
                } catch (_: Exception) { }
            }
        }
    }

    fun loadAttendance() {
        val sectionId = _uiState.value.selectedSectionId
        val date = _uiState.value.selectedDate
        val academicYearId = _uiState.value.selectedAcademicYearId

        if (sectionId == null || date.isEmpty() || academicYearId == null) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                coroutineScope {
                    val attendanceDef = async { apiService.getStudentAttendanceBySectionAndDate(sectionId, date) }
                    val enrollmentsDef = async { apiService.getEnrollmentsBySection(sectionId, academicYearId) }
                    
                    // We can also fetch the students directory for this section to get names
                    val classId = _uiState.value.selectedClassId
                    val studentsDef = async { apiService.getStudentsDirectory(null, academicYearId, classId, sectionId, true, 0, 200) }

                    val attendanceRecords = attendanceDef.await().body()?.data ?: emptyList()
                    val enrollments = enrollmentsDef.await().body()?.data ?: emptyList()
                    val students = studentsDef.await().body()?.data?.content ?: emptyList()

                    val displayList = enrollments.map { enrollment ->
                        val student = students.find { it.studentId == enrollment.studentId }
                        val attendance = attendanceRecords.find { it.studentClassId == enrollment.studentClassId }
                        
                        StudentAttendanceDisplay(
                            studentId = enrollment.studentId,
                            studentName = student?.let { "${it.firstName} ${it.lastName}" } ?: "Unknown Student",
                            registrationNumber = student?.registrationNumber ?: "",
                            status = attendance?.status, // null means not marked
                            remarks = attendance?.remarks ?: ""
                        )
                    }.sortedBy { it.studentName }

                    _uiState.value = _uiState.value.copy(isLoading = false, attendanceRecords = displayList)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to load attendance: ${e.message}")
            }
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    class Factory(private val apiService: ApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StudentAttendanceViewModel(apiService) as T
        }
    }
}
