package com.school.gdsportal.ui.admin.teachers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.Teacher
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class TeachersDirectoryUiState {
    object Loading : TeachersDirectoryUiState()
    data class Success(val teachers: List<Teacher>, val totalCount: Int) : TeachersDirectoryUiState()
    data class Error(val message: String) : TeachersDirectoryUiState()
}

class TeachersDirectoryViewModel(private val apiService: ApiService) : ViewModel() {

    private val _uiState = MutableStateFlow<TeachersDirectoryUiState>(TeachersDirectoryUiState.Loading)
    val uiState: StateFlow<TeachersDirectoryUiState> = _uiState

    private var totalCount: Int = 0

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = TeachersDirectoryUiState.Loading
            try {
                // Fetch count first if not fetched yet (or always refresh on full load)
                val countResponse = apiService.getTotalTeacherCount()
                if (countResponse.isSuccessful) {
                    totalCount = countResponse.body()?.data ?: 0
                }

                val teachersResponse = apiService.getAllTeachers()
                if (teachersResponse.isSuccessful) {
                    val teachersList = teachersResponse.body()?.data ?: emptyList()
                    _uiState.value = TeachersDirectoryUiState.Success(teachersList, totalCount)
                } else {
                    _uiState.value = TeachersDirectoryUiState.Error("Failed to load teachers: ${teachersResponse.message()}")
                }
            } catch (e: Exception) {
                _uiState.value = TeachersDirectoryUiState.Error("Network error: ${e.message}")
            }
        }
    }

    fun searchTeachers(term: String) {
        if (term.isBlank()) {
            loadData() // Reload normal list if search is empty
            return
        }

        viewModelScope.launch {
            _uiState.value = TeachersDirectoryUiState.Loading
            try {
                val response = apiService.searchTeachers(term)
                if (response.isSuccessful) {
                    val teachersList = response.body()?.data ?: emptyList()
                    // Still use the cached totalCount
                    _uiState.value = TeachersDirectoryUiState.Success(teachersList, totalCount)
                } else {
                    _uiState.value = TeachersDirectoryUiState.Error("Failed to search teachers: ${response.message()}")
                }
            } catch (e: Exception) {
                _uiState.value = TeachersDirectoryUiState.Error("Network error: ${e.message}")
            }
        }
    }

    class Factory(private val apiService: ApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TeachersDirectoryViewModel(apiService) as T
        }
    }
}
