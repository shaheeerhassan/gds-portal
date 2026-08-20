package com.school.gdsportal.ui.parent.children.info

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.Student
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChildInfoUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val student: Student? = null
)

class ChildInfoViewModel(
    private val studentId: Long,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChildInfoUiState())
    val uiState: StateFlow<ChildInfoUiState> = _uiState.asStateFlow()

    init {
        loadStudentInfo()
    }

    fun loadStudentInfo() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val response = apiService.getStudent(studentId)
                if (response.isSuccessful) {
                    _uiState.update { it.copy(isLoading = false, student = response.body()?.data) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load student information.") }
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
                    return ChildInfoViewModel(studentId, apiService) as T
                }
            }
    }
}