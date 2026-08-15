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

sealed class AcademicYearsDirectoryUiState {
    object Loading : AcademicYearsDirectoryUiState()
    data class Success(val academicYears: List<AcademicYear>) : AcademicYearsDirectoryUiState()
    data class Error(val message: String) : AcademicYearsDirectoryUiState()
}

class AcademicYearsDirectoryViewModel(private val apiService: ApiService) : ViewModel() {

    private val _uiState = MutableStateFlow<AcademicYearsDirectoryUiState>(AcademicYearsDirectoryUiState.Loading)
    val uiState: StateFlow<AcademicYearsDirectoryUiState> = _uiState.asStateFlow()

    init {
        loadAcademicYears()
    }

    fun loadAcademicYears() {
        viewModelScope.launch {
            _uiState.value = AcademicYearsDirectoryUiState.Loading
            try {
                val response = apiService.getAcademicYears()
                if (response.isSuccessful && response.body() != null) {
                    val list = response.body()?.data ?: emptyList()
                    // Optional: Sort descending by start date, or leave as backend provides
                    val sortedList = list.sortedByDescending { it.startDate }
                    _uiState.value = AcademicYearsDirectoryUiState.Success(sortedList)
                } else {
                    val errorMsg = try {
                        val errorString = response.errorBody()?.string()
                        if (errorString != null) {
                            org.json.JSONObject(errorString).getString("message")
                        } else {
                            response.message()
                        }
                    } catch (e: Exception) {
                        "Failed to load academic years"
                    }
                    _uiState.value = AcademicYearsDirectoryUiState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _uiState.value = AcademicYearsDirectoryUiState.Error("Network error: ${e.message}")
            }
        }
    }

    class Factory(private val apiService: ApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AcademicYearsDirectoryViewModel(apiService) as T
        }
    }
}
