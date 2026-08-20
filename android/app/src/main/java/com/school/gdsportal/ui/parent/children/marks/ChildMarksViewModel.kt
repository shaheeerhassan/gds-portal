package com.school.gdsportal.ui.parent.children.marks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.school.gdsportal.data.remote.Mark

data class ChildMarksUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val academicYearName: String = "",
    val marks: List<Mark> = emptyList()
)

class ChildMarksViewModel(
    private val studentId: Long,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChildMarksUiState())
    val uiState: StateFlow<ChildMarksUiState> = _uiState.asStateFlow()

    init {
        loadMarks()
    }

    fun loadMarks() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                // 1. Get current academic year
                val yearResponse = apiService.getCurrentAcademicYear()
                val currentYear = yearResponse.body()?.data

                if (currentYear == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Could not determine current academic year.") }
                    return@launch
                }

                // 2. Fetch marks for this student and year
                // Ensure ApiService has: @GET("api/marks/student/{studentId}/year/{academicYearId}")
                val marksResponse = apiService.getMarksByStudentAndYear(studentId, currentYear.academicYearId)

                if (marksResponse.isSuccessful) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            academicYearName = currentYear.yearName,
                            marks = marksResponse.body()?.data ?: emptyList()
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load marks.") }
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
                    return ChildMarksViewModel(studentId, apiService) as T
                }
            }
    }
}