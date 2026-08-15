package com.school.gdsportal.ui.admin.administrators

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.Administrator
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdministratorEditUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false,
    
    val adminId: Long = 0,
    
    // Administrator Form fields (Account fields omitted as they cannot be updated here)
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

class AdministratorEditViewModel(
    private val adminId: Long,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdministratorEditUiState(adminId = adminId))
    val uiState: StateFlow<AdministratorEditUiState> = _uiState.asStateFlow()

    private var loadedAdministrator: Administrator? = null

    init {
        loadAdministrator()
    }

    private fun loadAdministrator() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = apiService.getAdministratorById(adminId)
                if (response.isSuccessful && response.body() != null && response.body()?.data != null) {
                    val administrator = response.body()!!.data!!
                    loadedAdministrator = administrator
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            firstName = administrator.firstName,
                            lastName = administrator.lastName,
                            phone = administrator.phone,
                            employeeId = administrator.employeeId
                        )
                    }
                } else {
                    _uiState.update { 
                        it.copy(isLoading = false, error = "Failed to load administrator details.")
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isLoading = false, error = "Network error while loading administrator.")
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

    fun updateAdministrator() {
        val state = _uiState.value
        val original = loadedAdministrator ?: return
        
        if (!state.isFormValid) {
            _uiState.update { it.copy(error = "Please fill all required fields.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val request = Administrator(
                    adminId = original.adminId,
                    userId = original.userId,
                    firstName = state.firstName.trim(),
                    lastName = state.lastName.trim(),
                    phone = state.phone.trim(),
                    employeeId = state.employeeId.trim()
                )
                
                val response = apiService.updateAdministrator(adminId, request)
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
                        "Failed to update administrator"
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
            adminId: Long,
            apiService: ApiService
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AdministratorEditViewModel(adminId, apiService) as T
            }
        }
    }
}
