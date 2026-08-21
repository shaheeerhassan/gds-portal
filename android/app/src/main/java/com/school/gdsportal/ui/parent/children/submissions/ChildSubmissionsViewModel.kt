package com.school.gdsportal.ui.parent.children.submissions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.SubmissionDisplay
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChildSubmissionsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val submissions: List<SubmissionDisplay> = emptyList()
)

class ChildSubmissionsViewModel(
    private val studentId: Long,
    private val apiService: ApiService
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChildSubmissionsUiState())
    val uiState: StateFlow<ChildSubmissionsUiState> = _uiState.asStateFlow()

    init { loadSubmissions() }

    fun loadSubmissions() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val response = apiService.getSubmissionsByStudent(studentId)
                if (response.isSuccessful) {
                    val submissions = response.body()?.data ?: emptyList()
                    _uiState.update { it.copy(isLoading = false, submissions = submissions.sortedByDescending { it.submittedAt }) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load submissions.") }
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
                    return ChildSubmissionsViewModel(studentId, apiService) as T
                }
            }
    }
}