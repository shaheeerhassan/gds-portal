package com.school.gdsportal.ui.admin.parents

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

data class ParentEditUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false,
    
    val originalParent: Parent? = null,
    
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "",
    val occupation: String = ""
) {
    val isFormValid: Boolean
        get() = firstName.isNotBlank() && lastName.isNotBlank()
}

class ParentEditViewModel(
    private val parentId: Long,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ParentEditUiState())
    val uiState: StateFlow<ParentEditUiState> = _uiState.asStateFlow()

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
                val response = apiService.getParent(parentId)
                if (response.isSuccessful && response.body()?.data != null) {
                    val parent = response.body()!!.data!!
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            originalParent = parent,
                            firstName = parent.firstName,
                            lastName = parent.lastName,
                            phone = parent.phone ?: "",
                            occupation = parent.occupation ?: ""
                        )
                    }
                } else {
                    _uiState.update { 
                        it.copy(isLoading = false, error = "Failed to load parent for editing.")
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isLoading = false, error = "Network error. Please try again.")
                }
            }
        }
    }

    fun updateField(field: String, value: String) {
        _uiState.update { state ->
            when (field) {
                "firstName" -> state.copy(firstName = value)
                "lastName" -> state.copy(lastName = value)
                "phone" -> state.copy(phone = value)
                "occupation" -> state.copy(occupation = value)
                else -> state
            }
        }
    }

    fun saveChanges() {
        val state = _uiState.value
        val original = state.originalParent ?: return
        
        if (!state.isFormValid) {
            _uiState.update { it.copy(error = "Please fill all required fields.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val updatedParent = original.copy(
                    firstName = state.firstName.trim(),
                    lastName = state.lastName.trim(),
                    phone = state.phone.trim().takeIf { it.isNotEmpty() },
                    occupation = state.occupation.trim().takeIf { it.isNotEmpty() }
                )
                
                val response = apiService.updateParent(parentId, updatedParent)
                if (response.isSuccessful) {
                    _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                } else {
                    _uiState.update { 
                        it.copy(isSaving = false, error = "Failed to save changes. Please try again.")
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
        fun provideFactory(parentId: Long, apiService: ApiService): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ParentEditViewModel(parentId, apiService) as T
            }
        }
    }
}
