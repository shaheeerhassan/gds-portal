package com.school.gdsportal.ui.admin.teachers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.Teacher
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class TeacherProfileUiState(
    val isLoading: Boolean = true,
    val teacher: Teacher? = null,
    val error: String? = null,
    val isDeactivating: Boolean = false,
    val deactivateSuccess: Boolean = false,
    val deactivateError: String? = null
)

class TeacherProfileViewModel(
    private val teacherId: Long,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherProfileUiState())
    val uiState: StateFlow<TeacherProfileUiState> = _uiState

    init {
        loadTeacherProfile()
    }

    fun loadTeacherProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = apiService.getTeacherById(teacherId)
                if (response.isSuccessful) {
                    val teacher = response.body()?.data
                    if (teacher != null) {
                        _uiState.value = _uiState.value.copy(isLoading = false, teacher = teacher)
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false, error = "Teacher not found")
                    }
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to load teacher: ${response.message()}")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Network error: ${e.message}")
            }
        }
    }

    fun deactivateTeacher() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeactivating = true, deactivateError = null)
            try {
                val response = apiService.deactivateTeacher(teacherId)
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isDeactivating = false, deactivateSuccess = true)
                } else {
                    _uiState.value = _uiState.value.copy(isDeactivating = false, deactivateError = "Failed to deactivate teacher")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isDeactivating = false, deactivateError = "Network error: ${e.message}")
            }
        }
    }

    fun dismissDeactivateError() {
        _uiState.value = _uiState.value.copy(deactivateError = null)
    }

    class Factory(
        private val teacherId: Long,
        private val apiService: ApiService
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TeacherProfileViewModel(teacherId, apiService) as T
        }
    }
}
