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

data class PeriodDetailUiState(
    val isLoading: Boolean = true,
    val period: Period? = null,
    val isDeleting: Boolean = false,
    val deleteSuccess: Boolean = false,
    val error: String? = null
)

class PeriodDetailViewModel(
    private val periodId: Int,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(PeriodDetailUiState())
    val uiState: StateFlow<PeriodDetailUiState> = _uiState.asStateFlow()

    init {
        loadPeriod()
    }

    private fun loadPeriod() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = apiService.getPeriodById(periodId)
                if (response.isSuccessful) {
                    val period = response.body()?.data
                    if (period != null) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            period = period
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false, error = "Period not found")
                    }
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to load period")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Network error: ${e.message}")
            }
        }
    }

    fun deletePeriod() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true, error = null)
            try {
                val response = apiService.deletePeriod(periodId)
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isDeleting = false, deleteSuccess = true)
                } else {
                    val errorMsg = try {
                        val errorString = response.errorBody()?.string()
                        if (errorString != null) {
                            org.json.JSONObject(errorString).getString("message")
                        } else {
                            response.message()
                        }
                    } catch (e: Exception) {
                        "Failed to delete period"
                    }
                    _uiState.value = _uiState.value.copy(isDeleting = false, error = errorMsg)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    error = "Network error: ${e.message}"
                )
            }
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    class Factory(private val periodId: Int, private val apiService: ApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PeriodDetailViewModel(periodId, apiService) as T
        }
    }
}
