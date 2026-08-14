package com.school.gdsportal.ui.admin.principals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.Principal
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PrincipalProfileUiState(
    val isLoading: Boolean = true,
    val principal: Principal? = null,
    val error: String? = null,
    val isDeactivating: Boolean = false,
    val deactivateSuccess: Boolean = false,
    val deactivateError: String? = null
)

class PrincipalProfileViewModel(
    private val principalId: Long,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrincipalProfileUiState())
    val uiState: StateFlow<PrincipalProfileUiState> = _uiState

    init {
        loadPrincipalProfile()
    }

    fun loadPrincipalProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = apiService.getPrincipalById(principalId)
                if (response.isSuccessful) {
                    val principal = response.body()?.data
                    if (principal != null) {
                        _uiState.value = _uiState.value.copy(isLoading = false, principal = principal)
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false, error = "Principal not found")
                    }
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to load principal: ${response.message()}")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Network error: ${e.message}")
            }
        }
    }

    fun deactivatePrincipal() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeactivating = true, deactivateError = null)
            try {
                val response = apiService.deactivatePrincipal(principalId)
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isDeactivating = false, deactivateSuccess = true)
                } else {
                    _uiState.value = _uiState.value.copy(isDeactivating = false, deactivateError = "Failed to deactivate principal")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isDeactivating = false, deactivateError = "Network error: ${e.message}")
            }
        }
    }

    fun dismissDeactivateError() {
        _uiState.value = _uiState.value.copy(deactivateError = null)
    }

    class Factory(
        private val principalId: Long,
        private val apiService: ApiService
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PrincipalProfileViewModel(principalId, apiService) as T
        }
    }
}
