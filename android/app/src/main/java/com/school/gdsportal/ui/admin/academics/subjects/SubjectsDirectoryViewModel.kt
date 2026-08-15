package com.school.gdsportal.ui.admin.academics.subjects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.Subject
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SubjectsDirectoryUiState(
    val isLoading: Boolean = false,
    val subjects: List<Subject> = emptyList(),
    val filteredSubjects: List<Subject> = emptyList(),
    val searchQuery: String = "",
    val error: String? = null
)

class SubjectsDirectoryViewModel(private val apiService: ApiService) : ViewModel() {

    private val _uiState = MutableStateFlow(SubjectsDirectoryUiState())
    val uiState: StateFlow<SubjectsDirectoryUiState> = _uiState.asStateFlow()

    init {
        loadSubjects()
    }

    fun loadSubjects() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = apiService.getSubjects()
                if (response.isSuccessful) {
                    val list = response.body()?.data ?: emptyList()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        subjects = list
                    )
                    applySearch(_uiState.value.searchQuery, list)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load subjects"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Network error: ${e.message}"
                )
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applySearch(query, _uiState.value.subjects)
    }

    private fun applySearch(query: String, allSubjects: List<Subject>) {
        val lowerQuery = query.lowercase().trim()
        val filtered = if (lowerQuery.isEmpty()) {
            allSubjects
        } else {
            allSubjects.filter { subject ->
                subject.subjectName.lowercase().contains(lowerQuery) ||
                subject.subjectCode.lowercase().contains(lowerQuery)
            }
        }
        _uiState.value = _uiState.value.copy(filteredSubjects = filtered)
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    class Factory(private val apiService: ApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SubjectsDirectoryViewModel(apiService) as T
        }
    }
}
