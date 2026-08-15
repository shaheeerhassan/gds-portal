package com.school.gdsportal.ui.admin.principals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.Principal
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class PrincipalsDirectoryUiState {
    object Loading : PrincipalsDirectoryUiState()
    data class Success(val principals: List<Principal>) : PrincipalsDirectoryUiState()
    data class Error(val message: String) : PrincipalsDirectoryUiState()
}

class PrincipalsDirectoryViewModel(private val apiService: ApiService) : ViewModel() {

    private val _uiState = MutableStateFlow<PrincipalsDirectoryUiState>(PrincipalsDirectoryUiState.Loading)
    val uiState: StateFlow<PrincipalsDirectoryUiState> = _uiState

    private var allPrincipalsCache: List<Principal> = emptyList()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = PrincipalsDirectoryUiState.Loading
            try {
                val principalsResponse = apiService.getPrincipals()
                if (principalsResponse.isSuccessful) {
                    val principalsList = principalsResponse.body()?.data ?: emptyList()
                    allPrincipalsCache = principalsList
                    _uiState.value = PrincipalsDirectoryUiState.Success(principalsList)
                } else {
                    _uiState.value = PrincipalsDirectoryUiState.Error("Failed to load principals: ${principalsResponse.message()}")
                }
            } catch (e: Exception) {
                _uiState.value = PrincipalsDirectoryUiState.Error("Network error: ${e.message}")
            }
        }
    }

    fun searchPrincipals(term: String) {
        if (term.isBlank()) {
            _uiState.value = PrincipalsDirectoryUiState.Success(allPrincipalsCache)
            return
        }

        val filtered = allPrincipalsCache.filter { principal ->
            principal.firstName.contains(term, ignoreCase = true) ||
            principal.lastName.contains(term, ignoreCase = true) ||
            principal.employeeId.contains(term, ignoreCase = true)
        }
        
        _uiState.value = PrincipalsDirectoryUiState.Success(filtered)
    }

    class Factory(private val apiService: ApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PrincipalsDirectoryViewModel(apiService) as T
        }
    }
}
