package com.school.gdsportal.ui.teacher.attendance.studentattendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.local.TokenManager
import com.school.gdsportal.data.remote.StudentAttendance
import com.school.gdsportal.data.remote.TeacherClassDTO
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

import com.school.gdsportal.data.remote.StudentAttendanceDisplay
import com.school.gdsportal.data.remote.StudentAttendanceStatus
import com.school.gdsportal.data.remote.Enrollment
import kotlinx.coroutines.async

data class TeacherStudentAttendanceUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccessMessage: String? = null,
    val assignedClasses: List<TeacherClassDTO> = emptyList(),
    val selectedSection: TeacherClassDTO? = null,
    val selectedDate: String = "", // Format: yyyy-MM-dd
    val displayRecords: List<StudentAttendanceDisplay> = emptyList(),
    val enrollments: List<Enrollment> = emptyList()
)

class TeacherStudentAttendanceViewModel(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherStudentAttendanceUiState())
    val uiState: StateFlow<TeacherStudentAttendanceUiState> = _uiState.asStateFlow()

    private var currentAcademicYearId: Int = 0

    init {
        // Default to today's date
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
        _uiState.update { it.copy(selectedDate = today) }
        loadAssignedClasses()
    }

    private fun loadAssignedClasses() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val teacherId = apiService.getTeacherMe().body()?.data?.teacherId ?: 0L
                val yearResponse = apiService.getCurrentAcademicYear()
                val currentYear = yearResponse.body()?.data

                if (teacherId != 0L && currentYear != null) {
                    currentAcademicYearId = currentYear.academicYearId
                    val classesResponse = apiService.getTeacherClasses(teacherId, currentAcademicYearId)

                    if (classesResponse.isSuccessful) {
                        val classes = classesResponse.body()?.data ?: emptyList()
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                assignedClasses = classes,
                                // Auto-select the first class if available
                                selectedSection = classes.firstOrNull()
                            )
                        }
                        // Automatically load records for the first class
                        classes.firstOrNull()?.let { loadAttendanceRecords(it.sectionId, _uiState.value.selectedDate) }
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = "Failed to load your assigned classes.") }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Authentication or Academic Year error.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Network error.") }
            }
        }
    }

    fun onSectionSelected(section: TeacherClassDTO) {
        _uiState.update { it.copy(selectedSection = section, saveSuccessMessage = null) }
        loadAttendanceRecords(section.sectionId, _uiState.value.selectedDate)
    }

    fun onDateSelected(date: String) {
        _uiState.update { it.copy(selectedDate = date, saveSuccessMessage = null) }
        _uiState.value.selectedSection?.let { loadAttendanceRecords(it.sectionId, date) }
    }

    private var currentTeacherId: Long = 0L

    private fun loadAttendanceRecords(sectionId: Int, date: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                currentTeacherId = apiService.getTeacherMe().body()?.data?.teacherId ?: 0L

                val studentsDeferred = async { apiService.getStudentsDirectory(null, null, null, sectionId, true, 0, 1000) }
                val enrollmentsDeferred = async { apiService.getEnrollmentsBySection(sectionId, currentAcademicYearId) }
                val attendanceDeferred = async { apiService.getStudentAttendanceBySectionAndDate(sectionId, date) }

                val studentsRes = studentsDeferred.await()
                val enrollmentsRes = enrollmentsDeferred.await()
                val attendanceRes = attendanceDeferred.await()

                if (studentsRes.isSuccessful && enrollmentsRes.isSuccessful) {
                    val students = studentsRes.body()?.data?.content ?: emptyList()
                    val enrollments = enrollmentsRes.body()?.data ?: emptyList()
                    val existingAttendance = attendanceRes.body()?.data ?: emptyList()

                    val displayRecords = students.mapNotNull { student ->
                        val enrollment = enrollments.find { it.studentId == student.studentId }
                        if (enrollment != null) {
                            val existing = existingAttendance.find { it.studentClassId == enrollment.studentClassId }
                            StudentAttendanceDisplay(
                                studentId = student.studentId,
                                studentName = "${student.firstName} ${student.lastName}",
                                registrationNumber = student.registrationNumber,
                                status = existing?.status,
                                remarks = existing?.remarks ?: ""
                            )
                        } else null
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            displayRecords = displayRecords,
                            enrollments = enrollments
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load students and enrollments.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Network error while fetching attendance.") }
            }
        }
    }

    fun updateLocalAttendanceStatus(studentId: Long, newStatusString: String) {
        val currentRecords = _uiState.value.displayRecords.toMutableList()
        val index = currentRecords.indexOfFirst { it.studentId == studentId }
        if (index != -1) {
            val record = currentRecords[index]
            currentRecords[index] = record.copy(status = StudentAttendanceStatus.valueOf(newStatusString))
            _uiState.update { it.copy(displayRecords = currentRecords) }
        }
    }

    fun saveAttendance() {
        val enrollments = _uiState.value.enrollments
        val records = _uiState.value.displayRecords.mapNotNull { display ->
            display.status?.let { status ->
                val enrollment = enrollments.find { it.studentId == display.studentId }
                if (enrollment != null) {
                    StudentAttendance(
                        studentClassId = enrollment.studentClassId,
                        attendanceDate = _uiState.value.selectedDate,
                        status = status,
                        periodId = 0, // Period not implemented yet or default to 0
                        markedBy = currentTeacherId,
                        markedAt = null,
                        isLocked = false,
                        remarks = display.remarks
                    )
                } else null
            }
        }
        
        if (records.isEmpty()) {
            _uiState.update { it.copy(error = "No attendance marked.") }
            return
        }

        _uiState.update { it.copy(isSaving = true, error = null, saveSuccessMessage = null) }
        viewModelScope.launch {
            try {
                val request = com.school.gdsportal.data.remote.MarkStudentAttendanceRequest(records = records)
                val response = apiService.markStudentAttendance(request)
                if (response.isSuccessful) {
                    _uiState.update { it.copy(isSaving = false, saveSuccessMessage = "Attendance saved successfully!") }
                } else {
                    _uiState.update { it.copy(isSaving = false, error = "Failed to save attendance.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = "Network error while saving attendance.") }
            }
        }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(saveSuccessMessage = null) }
    }

    companion object {
        fun provideFactory(apiService: ApiService, tokenManager: TokenManager): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TeacherStudentAttendanceViewModel(apiService, tokenManager) as T
                }
            }
    }
}
