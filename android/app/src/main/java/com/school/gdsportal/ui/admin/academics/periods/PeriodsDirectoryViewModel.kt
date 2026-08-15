package com.school.gdsportal.ui.admin.academics.periods

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.Period
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PeriodsDirectoryUiState(
    val isLoading: Boolean = false,
    val periods: List<Period> = emptyList(),
    val error: String? = null
)

class PeriodsDirectoryViewModel(private val apiService: ApiService) : ViewModel() {

    private val _uiState = MutableStateFlow(PeriodsDirectoryUiState())
    val uiState: StateFlow<PeriodsDirectoryUiState> = _uiState.asStateFlow()

    init {
        loadPeriods()
    }

    fun loadPeriods() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = apiService.getAllPeriods()
                if (response.isSuccessful) {
                    val list = response.body()?.data ?: emptyList()
                    // Sort by periodNumber ascending
                    val sortedList = list.sortedBy { it.periodNumber }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        periods = sortedList
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load periods"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Network error: ${e.message}"
                )
            }
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    class Factory(private val apiService: ApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PeriodsDirectoryViewModel(apiService) as T
        }
    }
}
