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

data class SubjectEditUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val subjectName: String = "",
    val subjectCode: String = "",
    val description: String = "",
    val error: String? = null
)

class SubjectEditViewModel(
    private val subjectId: Int,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubjectEditUiState())
    val uiState: StateFlow<SubjectEditUiState> = _uiState.asStateFlow()

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
                            subjectName = subject.subjectName,
                            subjectCode = subject.subjectCode,
                            description = subject.description ?: ""
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Subject not found"
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load subject"
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

    fun updateForm(
        subjectName: String = _uiState.value.subjectName,
        subjectCode: String = _uiState.value.subjectCode,
        description: String = _uiState.value.description
    ) {
        _uiState.value = _uiState.value.copy(
            subjectName = subjectName,
            subjectCode = subjectCode,
            description = description
        )
    }

    fun updateSubject() {
        val state = _uiState.value
        
        if (state.subjectName.isBlank() || state.subjectCode.isBlank()) {
            _uiState.value = state.copy(error = "Subject Name and Subject Code are required")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            try {
                val subjectToUpdate = Subject(
                    subjectId = subjectId,
                    subjectName = state.subjectName.trim(),
                    subjectCode = state.subjectCode.trim(),
                    description = state.description.trim().takeIf { it.isNotEmpty() }
                )

                val response = apiService.updateSubject(subjectId, subjectToUpdate)
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
                } else {
                    val errorMsg = try {
                        val errorString = response.errorBody()?.string()
                        if (errorString != null) {
                            org.json.JSONObject(errorString).getString("message")
                        } else {
                            response.message()
                        }
                    } catch (e: Exception) {
                        "Failed to update subject"
                    }
                    _uiState.value = _uiState.value.copy(isSaving = false, error = errorMsg)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
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
            return SubjectEditViewModel(subjectId, apiService) as T
        }
    }
}
