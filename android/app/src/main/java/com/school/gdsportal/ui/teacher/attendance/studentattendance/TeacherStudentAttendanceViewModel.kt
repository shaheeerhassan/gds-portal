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

data class TeacherStudentAttendanceUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccessMessage: String? = null,
    val assignedClasses: List<TeacherClassDTO> = emptyList(),
    val selectedSection: TeacherClassDTO? = null,
    val selectedDate: String = "", // Format: yyyy-MM-dd
    val attendanceRecords: List<StudentAttendance> = emptyList()
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

    private fun loadAttendanceRecords(sectionId: Int, date: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val response = apiService.getStudentAttendanceBySectionAndDate(sectionId, date)
                if (response.isSuccessful) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            attendanceRecords = response.body()?.data ?: emptyList()
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load attendance for this date.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Network error while fetching attendance.") }
            }
        }
    }

    // Update status locally before saving
    fun updateLocalAttendanceStatus(attendanceId: Long, newStatusString: String) {
        val currentRecords = _uiState.value.attendanceRecords.toMutableList()
        val index = currentRecords.indexOfFirst { it.attendanceId == attendanceId }
        if (index != -1) {
            val record = currentRecords[index]
            // Note: Adjust depending on how your StudentAttendance data class handles Enums vs Strings
            // currentRecords[index] = record.copy(status = StatusEnum.valueOf(newStatusString))
            _uiState.update { it.copy(attendanceRecords = currentRecords) }
        }
    }

    fun saveAttendance() {
        _uiState.update { it.copy(isSaving = true, error = null, saveSuccessMessage = null) }
        viewModelScope.launch {
            try {
                // TODO: Replace this with your actual Admin bulk save API endpoint
                // val response = apiService.saveStudentAttendanceBulk(_uiState.value.attendanceRecords)

                // Simulating a successful network request
                kotlinx.coroutines.delay(1000)

                _uiState.update { it.copy(isSaving = false, saveSuccessMessage = "Attendance saved successfully!") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = "Failed to save attendance.") }
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
