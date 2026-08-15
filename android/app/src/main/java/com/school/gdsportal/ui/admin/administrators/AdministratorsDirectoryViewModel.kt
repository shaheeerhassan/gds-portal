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

sealed class AdministratorsDirectoryUiState {
    object Loading : AdministratorsDirectoryUiState()
    data class Success(val administrators: List<Administrator>) : AdministratorsDirectoryUiState()
    data class Error(val message: String) : AdministratorsDirectoryUiState()
}

class AdministratorsDirectoryViewModel(private val apiService: ApiService) : ViewModel() {

    private val _uiState = MutableStateFlow<AdministratorsDirectoryUiState>(AdministratorsDirectoryUiState.Loading)
    val uiState: StateFlow<AdministratorsDirectoryUiState> = _uiState.asStateFlow()

    private var allAdministrators = listOf<Administrator>()
    private var currentSearchQuery = ""

    init {
        loadAdministrators()
    }

    fun loadAdministrators() {
        viewModelScope.launch {
            _uiState.value = AdministratorsDirectoryUiState.Loading
            try {
                val response = apiService.getAdministrators()
                if (response.isSuccessful && response.body() != null) {
                    val list = response.body()?.data ?: emptyList()
                    allAdministrators = list
                    applySearchFilter(currentSearchQuery)
                } else {
                    val errorMsg = try {
                        val errorString = response.errorBody()?.string()
                        if (errorString != null) {
                            org.json.JSONObject(errorString).getString("message")
                        } else {
                            response.message()
                        }
                    } catch (e: Exception) {
                        "Failed to load administrators"
                    }
                    _uiState.value = AdministratorsDirectoryUiState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _uiState.value = AdministratorsDirectoryUiState.Error("Network error: ${e.message}")
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        currentSearchQuery = query
        applySearchFilter(query)
    }

    private fun applySearchFilter(query: String) {
        val filtered = if (query.isBlank()) {
            allAdministrators
        } else {
            allAdministrators.filter { admin ->
                admin.firstName.contains(query, ignoreCase = true) ||
                admin.lastName.contains(query, ignoreCase = true) ||
                admin.employeeId.contains(query, ignoreCase = true) ||
                (admin.phone?.contains(query, ignoreCase = true) ?: false)
            }
        }
        _uiState.value = AdministratorsDirectoryUiState.Success(filtered)
    }

    class Factory(private val apiService: ApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AdministratorsDirectoryViewModel(apiService) as T
        }
    }
}
