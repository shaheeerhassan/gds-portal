package com.school.gdsportal.ui.admin.students

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.dto.StudentDirectoryDTO
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StudentsDirectoryUiState(
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val error: String? = null,
    val students: List<StudentDirectoryDTO> = emptyList(),
    val totalStudents: Int = 0
)

class StudentsDirectoryViewModel(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentsDirectoryUiState())
    val uiState: StateFlow<StudentsDirectoryUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadData(isDebounced = false)
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        loadData(isDebounced = true)
    }

    fun retry() {
        loadData(isDebounced = false)
    }

    private fun loadData(isDebounced: Boolean) {
        // Cancel any pending/ongoing search or load request
        searchJob?.cancel()
        
        searchJob = viewModelScope.launch {
            if (isDebounced) {
                delay(500) // Debounce user typing
            }
            
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val currentQuery = _uiState.value.searchQuery.takeIf { it.isNotBlank() }
                
                // Initial page load with search parameters
                val response = apiService.getStudentsDirectory(
                    query = currentQuery,
                    academicYearId = null,
                    classId = null,
                    sectionId = null,
                    enrolled = null,
                    page = 0,
                    size = 20
                )
                
                if (response.isSuccessful) {
                    val paginatedResponse = response.body()?.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            students = paginatedResponse?.content ?: emptyList(),
                            totalStudents = paginatedResponse?.totalElements ?: 0
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to load students. Please try again."
                        )
                    }
                }
            } catch (e: Exception) {
                // Ignore cancellation exceptions caused by debouncing/re-searching
                if (e !is kotlinx.coroutines.CancellationException) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Unable to load students. Please check your connection."
                        )
                    }
                }
            }
        }
    }

    companion object {
        fun provideFactory(apiService: ApiService): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return StudentsDirectoryViewModel(apiService) as T
            }
        }
    }
}
