package com.school.gdsportal.ui.admin.teachers.teaching.classes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.TeacherClassDTO
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TeacherClassesUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val assignments: List<TeacherClassDTO> = emptyList(),
    val isUnassigning: Boolean = false
)

class TeacherClassesViewModel(
    private val teacherId: Long,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherClassesUiState())
    val uiState: StateFlow<TeacherClassesUiState> = _uiState.asStateFlow()

    private val currentAcademicYearId = 1

    init {
        loadAssignments()
    }

    fun loadAssignments() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = apiService.getTeacherClasses(teacherId, currentAcademicYearId)
                if (response.isSuccessful && response.body()?.data != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            assignments = response.body()?.data ?: emptyList()
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to load classes."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Network error. Please try again."
                    )
                }
            }
        }
    }

    fun unassignClass(teacherClassId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUnassigning = true, error = null) }
            try {
                val response = apiService.unassignTeacherClass(teacherClassId)
                if (response.isSuccessful) {
                    _uiState.update { it.copy(isUnassigning = false) }
                    loadAssignments() // Reload list
                } else {
                    _uiState.update {
                        it.copy(
                            isUnassigning = false,
                            error = "Failed to unassign class."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isUnassigning = false,
                        error = "Network error while unassigning."
                    )
                }
            }
        }
    }

    companion object {
        fun provideFactory(teacherId: Long, apiService: ApiService): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TeacherClassesViewModel(teacherId, apiService) as T
            }
        }
    }
}
