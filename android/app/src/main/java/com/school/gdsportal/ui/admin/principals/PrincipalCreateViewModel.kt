package com.school.gdsportal.ui.admin.principals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.CreatePrincipalRequest
import com.school.gdsportal.data.remote.Principal
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PrincipalCreateUiState(
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false,
    val createdPrincipalId: Long? = null,
    
    // User Form fields
    val email: String = "",
    val password: String = "",
    val username: String = "",
    
    // Principal Form fields
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "",
    val employeeId: String = ""
) {
    val isFormValid: Boolean
        get() = email.isNotBlank() &&
                password.isNotBlank() &&
                username.isNotBlank() &&
                firstName.isNotBlank() &&
                lastName.isNotBlank() &&
                phone.isNotBlank() &&
                employeeId.isNotBlank()
}

class PrincipalCreateViewModel(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrincipalCreateUiState())
    val uiState: StateFlow<PrincipalCreateUiState> = _uiState.asStateFlow()

    fun updateField(field: String, value: String) {
        _uiState.update { state ->
            when (field) {
                "email" -> state.copy(email = value)
                "password" -> state.copy(password = value)
                "username" -> state.copy(username = value)
                "firstName" -> state.copy(firstName = value)
                "lastName" -> state.copy(lastName = value)
                "phone" -> state.copy(phone = value)
                "employeeId" -> state.copy(employeeId = value)
                else -> state
            }
        }
    }

    fun createPrincipal() {
        val state = _uiState.value
        
        if (!state.isFormValid) {
            _uiState.update { it.copy(error = "Please fill all required fields.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val request = CreatePrincipalRequest(
                    email = state.email.trim(),
                    password = state.password.trim(),
                    username = state.username.trim(),
                    principal = Principal(
                        principalId = 0,
                        userId = 0,
                        firstName = state.firstName.trim(),
                        lastName = state.lastName.trim(),
                        phone = state.phone.trim(),
                        employeeId = state.employeeId.trim(),
                        isActive = true
                    )
                )
                
                val response = apiService.createPrincipal(request)
                if (response.isSuccessful && response.body() != null && response.body()?.data != null) {
                    val principalId = response.body()?.data?.principalId
                    _uiState.update { it.copy(isSaving = false, saveSuccess = true, createdPrincipalId = principalId) }
                } else {
                    _uiState.update { 
                        it.copy(isSaving = false, error = "Failed to create principal. Please check details and try again.")
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
                return PrincipalCreateViewModel(apiService) as T
            }
        }
    }
}
