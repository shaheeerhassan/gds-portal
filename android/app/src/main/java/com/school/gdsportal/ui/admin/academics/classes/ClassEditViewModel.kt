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

data class ClassEditUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false,
    
    val className: String = "",
    val numericLevel: String = "",
    val description: String = ""
) {
    val isFormValid: Boolean
        get() = className.isNotBlank() && numericLevel.isNotBlank()
}

class ClassEditViewModel(
    private val classId: Int,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClassEditUiState())
    val uiState: StateFlow<ClassEditUiState> = _uiState.asStateFlow()

    init {
        loadClass()
    }

    private fun loadClass() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = apiService.getClassById(classId)
                if (response.isSuccessful && response.body()?.data != null) {
                    val schoolClass = response.body()!!.data!!
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            className = schoolClass.className,
                            numericLevel = schoolClass.numericLevel.toString(),
                            description = schoolClass.description ?: ""
                        ) 
                    }
                } else {
                    val errorMsg = try {
                        val errorString = response.errorBody()?.string()
                        if (errorString != null) {
                            org.json.JSONObject(errorString).getString("message")
                        } else {
                            response.message()
                        }
                    } catch (e: Exception) {
                        "Failed to load class details"
                    }
                    _uiState.update { it.copy(isLoading = false, error = errorMsg) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Network error: ${e.message}") }
            }
        }
    }

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

    fun saveChanges() {
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
                    classId = classId,
                    className = state.className.trim(),
                    numericLevel = numericLevelInt,
                    description = state.description.trim().takeIf { it.isNotBlank() }
                )
                
                val response = apiService.updateClass(classId, request)
                if (response.isSuccessful) {
                    _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                } else {
                    val errorMsg = try {
                        val errorString = response.errorBody()?.string()
                        if (errorString != null) {
                            org.json.JSONObject(errorString).getString("message")
                        } else {
                            response.message()
                        }
                    } catch (e: Exception) {
                        "Failed to update class"
                    }
                    _uiState.update { it.copy(isSaving = false, error = errorMsg) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = "Network error while saving. Please try again.") }
            }
        }
    }
    
    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    class Factory(
        private val classId: Int,
        private val apiService: ApiService
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ClassEditViewModel(classId, apiService) as T
        }
    }
}
