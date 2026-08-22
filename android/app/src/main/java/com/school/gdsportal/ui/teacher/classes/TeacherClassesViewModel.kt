package com.school.gdsportal.ui.teacher.classes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.local.TokenManager
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
    val assignedClasses: List<TeacherClassDTO> = emptyList()
)

class TeacherClassesViewModel(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherClassesUiState())
    val uiState: StateFlow<TeacherClassesUiState> = _uiState.asStateFlow()

    init {
        loadMyClasses()
    }

    fun loadMyClasses() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                // 1. Get Teacher ID from local token
                val teacherId = tokenManager.getUserProfile()?.userId ?: 0L
                if (teacherId == 0L) {
                    _uiState.update { it.copy(isLoading = false, error = "Unable to identify teacher account.") }
                    return@launch
                }

                // 2. Fetch the current Academic Year
                val yearResponse = apiService.getCurrentAcademicYear()
                val currentYear = yearResponse.body()?.data
                if (currentYear == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Could not determine the current academic year.") }
                    return@launch
                }

                // 3. Fetch assigned classes for this specific teacher and year
                val classesResponse = apiService.getTeacherClasses(teacherId, currentYear.academicYearId)
                if (classesResponse.isSuccessful) {
                    val classes = classesResponse.body()?.data ?: emptyList()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            assignedClasses = classes.sortedBy { c -> c.className } // Optional sort
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load assigned classes.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Network error while fetching classes.") }
            }
        }
    }

    companion object {
        fun provideFactory(apiService: ApiService, tokenManager: TokenManager): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TeacherClassesViewModel(apiService, tokenManager) as T
                }
            }
    }
}