package com.school.gdsportal.ui.student.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.StudentAttendance
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class StudentAttendanceUiState(
    val isLoading: Boolean = true,
    val error: String? = null,

    val currentMonthLabel: String = "",
    val records: List<StudentAttendance> = emptyList(),

    val presentCount: Int = 0,
    val absentCount: Int = 0,
    val lateCount: Int = 0,
    val attendancePercentage: Int = 0
)

class StudentAttendanceViewModel(private val apiService: ApiService) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentAttendanceUiState())
    val uiState: StateFlow<StudentAttendanceUiState> = _uiState.asStateFlow()

    private val calendar = Calendar.getInstance()
    private val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    init {
        loadAttendanceForCurrentMonth()
    }

    private fun loadAttendanceForCurrentMonth() {
        val monthLabel = monthFormat.format(calendar.time)

        // Calculate start and end of the currently selected month
        val tempCal = calendar.clone() as Calendar
        tempCal.set(Calendar.DAY_OF_MONTH, 1)
        val startDate = apiDateFormat.format(tempCal.time)

        tempCal.set(Calendar.DAY_OF_MONTH, tempCal.getActualMaximum(Calendar.DAY_OF_MONTH))
        val endDate = apiDateFormat.format(tempCal.time)

        _uiState.update { it.copy(isLoading = true, error = null, currentMonthLabel = monthLabel) }

        viewModelScope.launch {
            try {
                val studentRes = apiService.getStudentMe()
                val student = studentRes.body()?.data

                if (student != null) {
                    val attRes = apiService.getStudentAttendanceRecords(student.studentId, startDate, endDate)
                    if (attRes.isSuccessful) {
                        val records = attRes.body()?.data ?: emptyList()

                        val present = records.count { it.status.name == "PRESENT" }
                        val late = records.count { it.status.name == "LATE" }
                        val absent = records.count { it.status.name == "ABSENT" }
                        val total = present + late + absent

                        val percentage = if (total > 0) ((present + late).toFloat() / total * 100).toInt() else 100

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                records = records.sortedByDescending { r -> r.attendanceDate },
                                presentCount = present,
                                lateCount = late,
                                absentCount = absent,
                                attendancePercentage = percentage
                            )
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = "Failed to load attendance records.") }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load student profile.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Network error while loading attendance.") }
            }
        }
    }

    fun nextMonth() {
        calendar.add(Calendar.MONTH, 1)
        loadAttendanceForCurrentMonth()
    }

    fun previousMonth() {
        calendar.add(Calendar.MONTH, -1)
        loadAttendanceForCurrentMonth()
    }

    companion object {
        fun provideFactory(apiService: ApiService): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return StudentAttendanceViewModel(apiService) as T
                }
            }
    }
}