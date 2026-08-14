package com.school.gdsportal.ui.admin.principals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.Principal
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PrincipalEditUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false,
    
    val principalId: Long = 0,
    
    // Principal Form fields (Account fields omitted as they cannot be updated here)
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "",
    val employeeId: String = ""
) {
    val isFormValid: Boolean
        get() = firstName.isNotBlank() &&
                lastName.isNotBlank() &&
                phone.isNotBlank() &&
                employeeId.isNotBlank()
}

class PrincipalEditViewModel(
    private val principalId: Long,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrincipalEditUiState(principalId = principalId))
    val uiState: StateFlow<PrincipalEditUiState> = _uiState.asStateFlow()

    private var loadedPrincipal: Principal? = null

    init {
        loadPrincipal()
    }

    private fun loadPrincipal() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = apiService.getPrincipalById(principalId)
                if (response.isSuccessful && response.body() != null && response.body()?.data != null) {
                    val principal = response.body()!!.data!!
                    loadedPrincipal = principal
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            firstName = principal.firstName,
                            lastName = principal.lastName,
                            phone = principal.phone,
                            employeeId = principal.employeeId
                        )
                    }
                } else {
                    _uiState.update { 
                        it.copy(isLoading = false, error = "Failed to load principal details.")
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isLoading = false, error = "Network error while loading principal.")
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
                "employeeId" -> state.copy(employeeId = value)
                else -> state
            }
        }
    }

    fun updatePrincipal() {
        val state = _uiState.value
        val original = loadedPrincipal ?: return
        
        if (!state.isFormValid) {
            _uiState.update { it.copy(error = "Please fill all required fields.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val request = Principal(
                    principalId = original.principalId,
                    userId = original.userId,
                    firstName = state.firstName.trim(),
                    lastName = state.lastName.trim(),
                    phone = state.phone.trim(),
                    employeeId = state.employeeId.trim(),
                    isActive = original.isActive
                )
                
                val response = apiService.updatePrincipal(principalId, request)
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
                        "Failed to update principal"
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
        fun provideFactory(
            principalId: Long,
            apiService: ApiService
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PrincipalEditViewModel(principalId, apiService) as T
            }
        }
    }
}
