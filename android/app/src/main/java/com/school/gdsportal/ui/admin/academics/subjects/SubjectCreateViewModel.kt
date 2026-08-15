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

data class SubjectCreateUiState(
    val isSaving: Boolean = false,
    val subjectName: String = "",
    val subjectCode: String = "",
    val description: String = "",
    val error: String? = null,
    val createdSubjectId: Int? = null
)

class SubjectCreateViewModel(private val apiService: ApiService) : ViewModel() {
    private val _uiState = MutableStateFlow(SubjectCreateUiState())
    val uiState: StateFlow<SubjectCreateUiState> = _uiState.asStateFlow()

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

    fun createSubject() {
        val state = _uiState.value
        
        if (state.subjectName.isBlank() || state.subjectCode.isBlank()) {
            _uiState.value = state.copy(error = "Subject Name and Subject Code are required")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            try {
                val newSubject = Subject(
                    subjectId = 0,
                    subjectName = state.subjectName.trim(),
                    subjectCode = state.subjectCode.trim(),
                    description = state.description.trim().takeIf { it.isNotEmpty() }
                )

                val response = apiService.createSubject(newSubject)
                if (response.isSuccessful) {
                    val createdId = response.body()?.data?.subjectId
                    _uiState.value = _uiState.value.copy(isSaving = false, createdSubjectId = createdId ?: -1)
                } else {
                    val errorMsg = try {
                        val errorString = response.errorBody()?.string()
                        if (errorString != null) {
                            org.json.JSONObject(errorString).getString("message")
                        } else {
                            response.message()
                        }
                    } catch (e: Exception) {
                        "Failed to create subject"
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

    companion object {
        fun provideFactory(apiService: ApiService): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SubjectCreateViewModel(apiService) as T
            }
        }
    }
}
