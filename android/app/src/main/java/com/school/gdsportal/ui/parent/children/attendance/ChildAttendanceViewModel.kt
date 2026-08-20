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
                val response = apiService.getStudentAttendanceRecords(studentId)
                if (response.isSuccessful) {
                    val data = response.body()?.data ?: emptyList<StudentAttendance>()
                    // Sort descending so newest is at the top
                    val sortedData = data.sortedByDescending { it.attendanceDate }
                    _uiState.update { it.copy(isLoading = false, records = sortedData) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load attendance.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Network error.") }
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