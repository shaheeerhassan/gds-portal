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

data class SubjectDetailUiState(
    val isLoading: Boolean = true,
    val subject: Subject? = null,
    val isDeleting: Boolean = false,
    val deleteSuccess: Boolean = false,
    val error: String? = null
)

class SubjectDetailViewModel(
    private val subjectId: Int,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubjectDetailUiState())
    val uiState: StateFlow<SubjectDetailUiState> = _uiState.asStateFlow()

    init {
        loadSubject()
    }

    private fun loadSubject() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = apiService.getSubjectById(subjectId)
                if (response.isSuccessful) {
                    val subject = response.body()?.data
                    if (subject != null) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            subject = subject
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false, error = "Subject not found")
                    }
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to load subject")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Network error: ${e.message}")
            }
        }
    }

    fun deleteSubject() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true, error = null)
            try {
                val response = apiService.deleteSubject(subjectId)
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isDeleting = false, deleteSuccess = true)
                } else {
                    val errorMsg = try {
                        val errorString = response.errorBody()?.string()
                        if (errorString != null) {
                            org.json.JSONObject(errorString).getString("message")
                        } else {
                            response.message()
                        }
                    } catch (e: Exception) {
                        "Failed to delete subject"
                    }
                    _uiState.value = _uiState.value.copy(isDeleting = false, error = errorMsg)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    error = "Network error: ${e.message}"
                )
            }
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    class Factory(private val subjectId: Int, private val apiService: ApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SubjectDetailViewModel(subjectId, apiService) as T
        }
    }
}
