package com.school.gdsportal.ui.admin.reports.teacherattendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.dto.TeacherAttendanceReportData
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TeacherAttendanceReportUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val reportData: TeacherAttendanceReportData? = null
)

class TeacherAttendanceReportViewModel(
    private val apiService: ApiService,
    private val teacherId: Long,
    private val month: Int,
    private val year: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherAttendanceReportUiState())
    val uiState: StateFlow<TeacherAttendanceReportUiState> = _uiState.asStateFlow()

    init {
        loadReport()
    }

    fun loadReport() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = apiService.getTeacherAttendanceReport(teacherId, month, year)
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        reportData = response.body()?.data
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load report."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Network error while loading report."
                )
            }
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    class Factory(
        private val apiService: ApiService,
        private val teacherId: Long,
        private val month: Int,
        private val year: Int
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TeacherAttendanceReportViewModel(apiService, teacherId, month, year) as T
        }
    }
}
