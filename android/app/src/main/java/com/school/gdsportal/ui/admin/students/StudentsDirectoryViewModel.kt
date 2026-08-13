package com.school.gdsportal.ui.admin.students

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.network.ApiService
import com.school.gdsportal.data.remote.dto.StudentDirectoryDTO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StudentsDirectoryState(
    val students: List<StudentDirectoryDTO> = emptyList(),
    val searchQuery: String = "",
    
    // Filters
    val selectedAcademicYearId: Int? = null,
    val selectedClassId: Int? = null,
    val selectedSectionId: Int? = null,
    val enrolledStatus: Boolean? = null,
    
    // Pagination & Loading
    val isLoading: Boolean = true,
    val isLoadingNextPage: Boolean = false,
    val hasNextPage: Boolean = false,
    val currentPage: Int = 0,
    val totalElements: Int = 0,
    
    val error: String? = null
)

class StudentsDirectoryViewModel(
    private val apiService: ApiService
) : ViewModel() {

    private val _state = MutableStateFlow(StudentsDirectoryState())
    val state: StateFlow<StudentsDirectoryState> = _state.asStateFlow()

    private var searchJob: Job? = null
    private var loadJob: Job? = null

    init {
        // We will default to a specific academic year ID if needed, 
        // but for now we'll load the first page with null (or we could fetch current academic year first)
        loadPage(0, reset = true)
    }

    fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query) }
        
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400) // Debounce 400ms
            loadPage(0, reset = true)
        }
    }

    fun updateFilters(academicYearId: Int?, classId: Int?, sectionId: Int?, enrolled: Boolean?) {
        _state.update { 
            it.copy(
                selectedAcademicYearId = academicYearId,
                selectedClassId = classId,
                selectedSectionId = sectionId,
                enrolledStatus = enrolled
            )
        }
        loadPage(0, reset = true)
    }

    fun clearFilters() {
        _state.update { 
            it.copy(
                selectedClassId = null,
                selectedSectionId = null,
                enrolledStatus = null
            )
        }
        loadPage(0, reset = true)
    }

    fun loadNextPage() {
        val currentState = _state.value
        if (currentState.isLoading || currentState.isLoadingNextPage || !currentState.hasNextPage) return

        loadPage(currentState.currentPage + 1, reset = false)
    }

    private fun loadPage(page: Int, reset: Boolean) {
        val currentState = _state.value
        
        if (reset) {
            _state.update { it.copy(isLoading = true, error = null) }
        } else {
            _state.update { it.copy(isLoadingNextPage = true, error = null) }
        }
        
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            try {
                // If the user hasn't explicitly selected an academic year, we might pass null or a hardcoded current year ID.
                // In a full implementation, we'd fetch the current academic year ID on init. 
                // We'll pass the selectedAcademicYearId.
                
                val response = apiService.getStudentsDirectory(
                    query = currentState.searchQuery.takeIf { it.isNotBlank() },
                    academicYearId = currentState.selectedAcademicYearId ?: 1, // Defaulting to 1 for prototype if not set
                    classId = currentState.selectedClassId,
                    sectionId = currentState.selectedSectionId,
                    enrolled = currentState.enrolledStatus,
                    page = page,
                    size = 20
                )
                
                if (response.isSuccessful) {
                    val paginatedResponse = response.body()
                    if (paginatedResponse != null) {
                        _state.update { 
                            it.copy(
                                students = if (reset) paginatedResponse.content else it.students + paginatedResponse.content,
                                currentPage = paginatedResponse.currentPage,
                                hasNextPage = paginatedResponse.hasNext,
                                totalElements = paginatedResponse.totalElements,
                                isLoading = false,
                                isLoadingNextPage = false
                            )
                        }
                    } else {
                        _state.update { it.copy(error = "Empty response body", isLoading = false, isLoadingNextPage = false) }
                    }
                } else {
                    _state.update { it.copy(error = "Failed to load students", isLoading = false, isLoadingNextPage = false) }
                }
                
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Unknown error", isLoading = false, isLoadingNextPage = false) }
            }
        }
    }

    companion object {
        fun provideFactory(apiService: ApiService): androidx.lifecycle.ViewModelProvider.Factory = 
            object : androidx.lifecycle.ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return StudentsDirectoryViewModel(apiService) as T
                }
            }
    }
}
