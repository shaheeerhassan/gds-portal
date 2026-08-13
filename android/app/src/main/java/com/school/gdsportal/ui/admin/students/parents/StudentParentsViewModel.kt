package com.school.gdsportal.ui.admin.students.parents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.Parent
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StudentParentsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val parents: List<Parent> = emptyList()
)

class StudentParentsViewModel(
    private val studentId: Long,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentParentsUiState())
    val uiState: StateFlow<StudentParentsUiState> = _uiState.asStateFlow()

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
                val response = apiService.getParentsByStudentId(studentId)
                if (response.isSuccessful && response.body()?.data != null) {
                    val parentsList = response.body()?.data ?: emptyList()
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            parents = parentsList
                        )
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Failed to load parents & guardians."
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
                return StudentParentsViewModel(studentId, apiService) as T
            }
        }
    }
}
