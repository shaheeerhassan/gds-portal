package com.school.gdsportal.ui.admin.teachers.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.MarkTeacherAttendanceRequest
import com.school.gdsportal.data.remote.Teacher
import com.school.gdsportal.data.remote.TeacherAttendance
import com.school.gdsportal.data.remote.TeacherAttendanceRecord
import com.school.gdsportal.data.remote.TeacherAttendanceStatusRequest
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Status options matching the backend enum
val ATTENDANCE_STATUSES = listOf("PRESENT", "ABSENT", "LATE", "LEAVE")

// Merged view of a teacher + their attendance record for today (if any)
data class TeacherAttendanceRow(
    val teacher: Teacher,
    // null means not yet marked today
    val existingRecord: TeacherAttendance?,
    // current selection in the dropdown (defaults to existing or "ABSENT")
    val selectedStatus: String
)

data class TeacherAttendanceUiState(
    val isLoading: Boolean = true,
    val rows: List<TeacherAttendanceRow> = emptyList(),
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val successMessage: String? = null,
    val error: String? = null,
    val selectedDate: LocalDate = LocalDate.now()
)

class TeacherAttendanceViewModel(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherAttendanceUiState())
    val uiState: StateFlow<TeacherAttendanceUiState> = _uiState.asStateFlow()

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val date = _uiState.value.selectedDate
                val dateStr = date.format(formatter)

                // Fetch all teachers
                val teachersResp = apiService.getAllTeachers()
                // Fetch today's attendance records (by date endpoint — no teacher ID needed)
                val attendanceResp = apiService.getTeacherAttendanceByDate(dateStr)

                if (!teachersResp.isSuccessful) {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load teachers") }
                    return@launch
                }

                val teachers = teachersResp.body()?.data ?: emptyList()
                val records = if (attendanceResp.isSuccessful) {
                    attendanceResp.body()?.data ?: emptyList()
                } else {
                    emptyList()
                }

                // Index records by teacherId for fast lookup
                val recordMap: Map<Long, TeacherAttendance> = records.associateBy { it.teacherId }

                val rows = teachers.map { teacher ->
                    val existing = recordMap[teacher.teacherId]
                    TeacherAttendanceRow(
                        teacher = teacher,
                        existingRecord = existing,
                        selectedStatus = existing?.status ?: "ABSENT"
                    )
                }

                _uiState.update { it.copy(isLoading = false, rows = rows) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Network error: ${e.message}") }
            }
        }
    }

    fun setDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
        loadData()
    }

    fun setStatus(teacherId: Long, status: String) {
        _uiState.update { state ->
            state.copy(
                rows = state.rows.map { row ->
                    if (row.teacher.teacherId == teacherId) row.copy(selectedStatus = status)
                    else row
                }
            )
        }
    }

    fun saveAttendance() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null, successMessage = null) }
            val dateStr = _uiState.value.selectedDate.format(formatter)
            val rows = _uiState.value.rows

            try {
                // Split rows into those that need INSERT vs UPDATE
                val toInsert = rows.filter { it.existingRecord == null }
                val toUpdate = rows.filter { it.existingRecord != null }

                // INSERT new records in one batch
                if (toInsert.isNotEmpty()) {
                    val request = MarkTeacherAttendanceRequest(
                        records = toInsert.map { row ->
                            TeacherAttendanceRecord(
                                teacherId = row.teacher.teacherId,
                                attendanceDate = dateStr,
                                status = row.selectedStatus
                            )
                        }
                    )
                    val resp = apiService.markTeacherAttendance(request)
                    if (!resp.isSuccessful) {
                        _uiState.update { it.copy(isSaving = false, saveError = "Failed to save attendance") }
                        return@launch
                    }
                }

                // UPDATE changed records individually
                for (row in toUpdate) {
                    val existing = row.existingRecord!!
                    if (existing.status != row.selectedStatus) {
                        val resp = apiService.updateTeacherAttendanceStatus(
                            attendanceId = existing.attendanceId,
                            request = TeacherAttendanceStatusRequest(status = row.selectedStatus)
                        )
                        if (!resp.isSuccessful) {
                            _uiState.update { it.copy(isSaving = false, saveError = "Failed to update status for ${row.teacher.firstName}") }
                            return@launch
                        }
                    }
                }

                _uiState.update { it.copy(isSaving = false, successMessage = "Attendance saved") }
                loadData() // Refresh to reflect persisted state
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = "Network error: ${e.message}") }
            }
        }
    }

    fun dismissMessages() {
        _uiState.update { it.copy(saveError = null, successMessage = null) }
    }

    companion object {
        fun provideFactory(apiService: ApiService): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TeacherAttendanceViewModel(apiService) as T
                }
            }
    }
}
