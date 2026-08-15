package com.school.gdsportal.ui.admin.academics.academicyears

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.AcademicYear
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AcademicYearDetailUiState(
    val isLoading: Boolean = true,
    val isSettingCurrent: Boolean = false,
    val error: String? = null,
    val academicYear: AcademicYear? = null
)

class AcademicYearDetailViewModel(
    private val academicYearId: Int,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(AcademicYearDetailUiState())
    val uiState: StateFlow<AcademicYearDetailUiState> = _uiState.asStateFlow()

    init {
        loadAcademicYear()
    }

    fun loadAcademicYear() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = apiService.getAcademicYearById(academicYearId)
                if (response.isSuccessful && response.body()?.data != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSettingCurrent = false,
                        academicYear = response.body()!!.data
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
                        "Failed to load academic year"
                    }
                    _uiState.value = _uiState.value.copy(isLoading = false, isSettingCurrent = false, error = errorMsg)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSettingCurrent = false,
                    error = "Network error: ${e.message}"
                )
            }
        }
    }

    fun setAsCurrent() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSettingCurrent = true, error = null)
            try {
                val response = apiService.setCurrentAcademicYear(academicYearId)
                if (response.isSuccessful) {
                    loadAcademicYear()
                } else {
                    val errorMsg = try {
                        val errorString = response.errorBody()?.string()
                        if (errorString != null) {
                            org.json.JSONObject(errorString).getString("message")
                        } else {
                            response.message()
                        }
                    } catch (e: Exception) {
                        "Failed to set as current academic year"
                    }
                    _uiState.value = _uiState.value.copy(isSettingCurrent = false, error = errorMsg)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSettingCurrent = false,
                    error = "Network error: ${e.message}"
                )
            }
        }
    }
    
    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    class Factory(
        private val academicYearId: Int,
        private val apiService: ApiService
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AcademicYearDetailViewModel(academicYearId, apiService) as T
        }
    }
}
