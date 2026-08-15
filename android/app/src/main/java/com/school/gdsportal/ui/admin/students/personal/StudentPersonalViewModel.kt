package com.school.gdsportal.ui.admin.students.personal

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

data class StudentPersonalUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val student: Student? = null
)

class StudentPersonalViewModel(
    private val studentId: Long,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentPersonalUiState())
    val uiState: StateFlow<StudentPersonalUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun retry() {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = apiService.getStudent(studentId)
                if (response.isSuccessful && response.body()?.data != null) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            student = response.body()!!.data
                        )
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Failed to load personal information."
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

    companion object {
        fun provideFactory(studentId: Long, apiService: ApiService): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return StudentPersonalViewModel(studentId, apiService) as T
            }
        }
    }
}
