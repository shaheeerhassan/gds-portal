package com.school.gdsportal.ui.admin.administrators

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.Administrator
import com.school.gdsportal.data.remote.CreateAdministratorRequest
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdministratorCreateUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false,
    val createdAdminId: Long? = null,
    
    // Account fields
    val username: String = "",
    val email: String = "",
    val password: String = "",
    
    // Administrator Form fields
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "",
    val employeeId: String = ""
) {
    val isFormValid: Boolean
        get() = username.isNotBlank() &&
                email.isNotBlank() &&
                password.isNotBlank() &&
                firstName.isNotBlank() &&
                lastName.isNotBlank() &&
                phone.isNotBlank() &&
                employeeId.isNotBlank()
}

class AdministratorCreateViewModel(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdministratorCreateUiState())
    val uiState: StateFlow<AdministratorCreateUiState> = _uiState.asStateFlow()

    fun updateField(field: String, value: String) {
        _uiState.update { state ->
            when (field) {
                "username" -> state.copy(username = value)
                "email" -> state.copy(email = value)
                "password" -> state.copy(password = value)
                "firstName" -> state.copy(firstName = value)
                "lastName" -> state.copy(lastName = value)
                "phone" -> state.copy(phone = value)
                "employeeId" -> state.copy(employeeId = value)
                else -> state
            }
        }
    }

    fun createAdministrator() {
        val state = _uiState.value
        
        if (!state.isFormValid) {
            _uiState.update { it.copy(error = "Please fill all required fields.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val administrator = Administrator(
                    adminId = 0L,
                    userId = 0L,
                    employeeId = state.employeeId.trim(),
                    firstName = state.firstName.trim(),
                    lastName = state.lastName.trim(),
                    phone = state.phone.trim()
                )
                
                val request = CreateAdministratorRequest(
                    email = state.email.trim(),
                    username = state.username.trim(),
                    password = state.password,
                    administrator = administrator
                )
                
                val response = apiService.createAdministrator(request)
                if (response.isSuccessful) {
                    val adminId = response.body()?.data?.adminId
                    _uiState.update { it.copy(isSaving = false, saveSuccess = true, createdAdminId = adminId) }
                } else {
                    val errorMsg = try {
                        val errorString = response.errorBody()?.string()
                        if (errorString != null) {
                            org.json.JSONObject(errorString).getString("message")
                        } else {
                            response.message()
                        }
                    } catch (e: Exception) {
                        "Failed to create administrator"
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
                return AdministratorCreateViewModel(apiService) as T
            }
        }
    }
}
