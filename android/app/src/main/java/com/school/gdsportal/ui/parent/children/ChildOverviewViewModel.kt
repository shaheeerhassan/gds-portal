package com.school.gdsportal.ui.parent.children

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

data class ChildOverviewUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val student: Student? = null
)

class ChildOverviewViewModel(
    private val studentId: Long,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChildOverviewUiState())
    val uiState: StateFlow<ChildOverviewUiState> = _uiState.asStateFlow()

    init {
        loadStudentDetails()
    }

    fun loadStudentDetails() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                // Parents have Pa(linked) permission to view their own child's profile
                val response = apiService.getStudent(studentId)
                if (response.isSuccessful) {
                    _uiState.update { it.copy(isLoading = false, student = response.body()?.data) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load student details.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Network error. Please try again.") }
            }
        }
    }

    companion object {
        fun provideFactory(studentId: Long, apiService: ApiService): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ChildOverviewViewModel(studentId, apiService) as T
                }
            }
    }
}