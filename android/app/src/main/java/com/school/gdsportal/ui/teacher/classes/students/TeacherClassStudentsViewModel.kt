package com.school.gdsportal.ui.teacher.classes.students

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.dto.StudentDirectoryDTO
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TeacherClassStudentsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val students: List<StudentDirectoryDTO> = emptyList()
)

class TeacherClassStudentsViewModel(
    private val sectionId: Int,
    private val apiService: ApiService
) : ViewModel() {
    private val _uiState = MutableStateFlow(TeacherClassStudentsUiState())
    val uiState: StateFlow<TeacherClassStudentsUiState> = _uiState.asStateFlow()

    init { loadStudents() }

    fun loadStudents() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                // Fetch enrolled students for this specific section.
                // Passing size = 500 to get the entire class in one request.
                val response = apiService.getStudentsDirectory(
                    query = null,
                    academicYearId = null,
                    classId = null,
                    sectionId = sectionId,
                    enrolled = true,
                    page = 0,
                    size = 500
                )

                if (response.isSuccessful) {
                    val students = response.body()?.data?.content ?: emptyList()
                    _uiState.update { it.copy(isLoading = false, students = students) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load students for this class.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Network error while fetching students.") }
            }
        }
    }

    companion object {
        fun provideFactory(sectionId: Int, apiService: ApiService): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TeacherClassStudentsViewModel(sectionId, apiService) as T
                }
            }
    }
}
