package com.school.gdsportal.ui.admin.teachers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.Teacher
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class TeacherProfileUiState {
    object Loading : TeacherProfileUiState()
    data class Success(val teacher: Teacher) : TeacherProfileUiState()
    data class Error(val message: String) : TeacherProfileUiState()
}

class TeacherProfileViewModel(
    private val teacherId: Long,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow<TeacherProfileUiState>(TeacherProfileUiState.Loading)
    val uiState: StateFlow<TeacherProfileUiState> = _uiState

    init {
        loadTeacherProfile()
    }

    fun loadTeacherProfile() {
        viewModelScope.launch {
            _uiState.value = TeacherProfileUiState.Loading
            try {
                val response = apiService.getTeacherById(teacherId)
                if (response.isSuccessful) {
                    val teacher = response.body()?.data
                    if (teacher != null) {
                        _uiState.value = TeacherProfileUiState.Success(teacher)
                    } else {
                        _uiState.value = TeacherProfileUiState.Error("Teacher not found")
                    }
                } else {
                    _uiState.value = TeacherProfileUiState.Error("Failed to load teacher: ${response.message()}")
                }
            } catch (e: Exception) {
                _uiState.value = TeacherProfileUiState.Error("Network error: ${e.message}")
            }
        }
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
