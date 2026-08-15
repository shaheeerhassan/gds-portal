package com.school.gdsportal.ui.admin.administrators

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.Administrator
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdministratorProfileUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val administrator: Administrator? = null
)

class AdministratorProfileViewModel(
    private val adminId: Long,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdministratorProfileUiState())
    val uiState: StateFlow<AdministratorProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = apiService.getAdministratorById(adminId)
                if (response.isSuccessful && response.body()?.data != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        administrator = response.body()!!.data
                    )
                } else {
                    val errorMsg = try {
                        val errorString = response.errorBody()?.string()
                        if (errorString != null) {
                            org.json.JSONObject(errorString).getString("message")
                        } else {
                            response.message()
                        }
                    } catch (e: Exception) {
                        "Failed to load administrator profile"
                    }
                    _uiState.value = _uiState.value.copy(isLoading = false, error = errorMsg)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Network error: ${e.message}"
                )
            }
        }
    }

    class Factory(
        private val adminId: Long,
        private val apiService: ApiService
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AdministratorProfileViewModel(adminId, apiService) as T
        }
    }
}
