package com.school.gdsportal.ui.admin.academics.classes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.SchoolClass
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ClassCreateUiState(
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false,
    val createdClassId: Int? = null,
    
    val className: String = "",
    val numericLevel: String = "",
    val description: String = ""
) {
    val isFormValid: Boolean
        get() = className.isNotBlank() && numericLevel.isNotBlank()
}

class ClassCreateViewModel(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClassCreateUiState())
    val uiState: StateFlow<ClassCreateUiState> = _uiState.asStateFlow()

    fun updateField(field: String, value: String) {
        _uiState.update { state ->
            when (field) {
                "className" -> state.copy(className = value)
                "numericLevel" -> state.copy(numericLevel = value)
                "description" -> state.copy(description = value)
                else -> state
            }
        }
    }

    fun createClass() {
        val state = _uiState.value
        
        if (!state.isFormValid) {
            _uiState.update { it.copy(error = "Please fill all required fields.") }
            return
        }
        
        val numericLevelInt = state.numericLevel.toIntOrNull()
        if (numericLevelInt == null) {
            _uiState.update { it.copy(error = "Numeric Level must be a valid number.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val request = SchoolClass(
                    classId = 0,
                    className = state.className.trim(),
                    numericLevel = numericLevelInt,
                    description = state.description.trim().takeIf { it.isNotBlank() }
                )
                
                val response = apiService.createClass(request)
                if (response.isSuccessful && response.body()?.data != null) {
                    val id = response.body()?.data?.classId
                    _uiState.update { it.copy(isSaving = false, saveSuccess = true, createdClassId = id) }
                } else {
                    val errorMsg = try {
                        val errorString = response.errorBody()?.string()
                        if (errorString != null) {
                            org.json.JSONObject(errorString).getString("message")
                        } else {
                            response.message()
                        }
                    } catch (e: Exception) {
                        "Failed to create class"
                    }
                    _uiState.update { 
                        it.copy(isSaving = false, error = errorMsg)
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isSaving = false, error = "Network error while saving. Please try again.")
                }
            }
        }
    }
    
    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    companion object {
        fun provideFactory(apiService: ApiService): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ClassCreateViewModel(apiService) as T
            }
        }
    }
}
