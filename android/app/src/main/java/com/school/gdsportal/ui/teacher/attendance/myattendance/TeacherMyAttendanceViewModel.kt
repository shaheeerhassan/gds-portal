package com.school.gdsportal.ui.teacher.attendance.myattendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.local.TokenManager
import com.school.gdsportal.data.remote.TeacherAttendance
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class TeacherMyAttendanceUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val attendanceRecords: List<TeacherAttendance> = emptyList(),
    val currentMonthName: String = ""
)

class TeacherMyAttendanceViewModel(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherMyAttendanceUiState())
    val uiState: StateFlow<TeacherMyAttendanceUiState> = _uiState.asStateFlow()

    init { loadAttendance() }

    fun loadAttendance() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                // 1. Identify the logged-in teacher
                val teacherId = apiService.getTeacherMe().body()?.data?.teacherId ?: 0L
                if (teacherId == 0L) {
                    _uiState.update { it.copy(isLoading = false, error = "Unable to identify teacher account.") }
                    return@launch
                }

                // 2. Calculate current month's start and end dates (yyyy-MM-dd)
                val calendar = Calendar.getInstance()
                val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                val currentMonthName = monthFormat.format(calendar.time)

                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val startDate = dateFormat.format(calendar.time)

                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                val endDate = dateFormat.format(calendar.time)

                // 3. Fetch attendance records
                val response = apiService.getTeacherAttendance(teacherId, startDate, endDate)

                if (response.isSuccessful) {
                    val records = response.body()?.data ?: emptyList()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            attendanceRecords = records.sortedByDescending { r -> r.attendanceDate },
                            currentMonthName = currentMonthName
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load attendance records.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Network error while fetching attendance.") }
            }
        }
    }

    companion object {
        fun provideFactory(apiService: ApiService, tokenManager: TokenManager): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TeacherMyAttendanceViewModel(apiService, tokenManager) as T
                }
            }
    }
}
