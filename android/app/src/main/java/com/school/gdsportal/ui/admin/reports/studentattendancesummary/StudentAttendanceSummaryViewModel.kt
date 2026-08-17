package com.school.gdsportal.ui.admin.reports.studentattendancesummary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.dto.StudentAttendanceSummaryData
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StudentAttendanceSummaryUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val reportData: StudentAttendanceSummaryData? = null
)

class StudentAttendanceSummaryViewModel(
    private val apiService: ApiService,
    private val studentId: Long,
    private val academicYearId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentAttendanceSummaryUiState())
    val uiState: StateFlow<StudentAttendanceSummaryUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val res = apiService.getStudentAttendanceSummary(studentId, academicYearId)
                if (res.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        reportData = res.body()?.data
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load attendance summary."
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
        private val studentId: Long,
        private val academicYearId: Int
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StudentAttendanceSummaryViewModel(apiService, studentId, academicYearId) as T
        }
    }
}
