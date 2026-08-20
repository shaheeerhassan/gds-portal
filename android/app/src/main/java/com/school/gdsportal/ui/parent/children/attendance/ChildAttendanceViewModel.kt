package com.school.gdsportal.ui.parent.children.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Assuming you have a StudentAttendance model from your Admin implementation
import com.school.gdsportal.data.remote.StudentAttendance

data class ChildAttendanceUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val records: List<StudentAttendance> = emptyList()
)

class ChildAttendanceViewModel(
    private val studentId: Long,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChildAttendanceUiState())
    val uiState: StateFlow<ChildAttendanceUiState> = _uiState.asStateFlow()

    init {
        loadAttendance()
    }

    fun loadAttendance() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                // 1. Fetch the current academic year to get the exact session dates
                val yearResponse = apiService.getCurrentAcademicYear()
                val currentYear = yearResponse.body()?.data

                if (currentYear == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Could not determine academic year dates.") }
                    return@launch
                }

                val startDate = currentYear.startDate
                val endDate = currentYear.endDate

                // 2. Fetch the attendance using those exact dates
                val response = apiService.getStudentAttendanceRecords(studentId, startDate, endDate)

                if (response.isSuccessful) {
                    val data = response.body()?.data ?: emptyList()
                    // Sort descending so newest is at the top (using the property name from your model)
                    val sortedData = data.sortedByDescending { it.attendanceDate }
                    _uiState.update { it.copy(isLoading = false, records = sortedData) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load attendance.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Network error while loading attendance.") }
            }
        }
    }

    companion object {
        fun provideFactory(studentId: Long, apiService: ApiService): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ChildAttendanceViewModel(studentId, apiService) as T
                }
            }
    }
}