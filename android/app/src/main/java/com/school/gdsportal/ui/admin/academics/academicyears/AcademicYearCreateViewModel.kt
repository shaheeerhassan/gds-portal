package com.school.gdsportal.ui.admin.academics.academicyears

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.AcademicYear
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AcademicYearCreateUiState(
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false,
    val createdAcademicYearId: Int? = null,
    
    val yearName: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val isCurrent: Boolean = false
) {
    val isFormValid: Boolean
        get() = yearName.isNotBlank() && startDate.isNotBlank() && endDate.isNotBlank()
}

class AcademicYearCreateViewModel(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(AcademicYearCreateUiState())
    val uiState: StateFlow<AcademicYearCreateUiState> = _uiState.asStateFlow()

    fun updateField(field: String, value: String) {
        _uiState.update { state ->
            when (field) {
                "yearName" -> state.copy(yearName = value)
                "startDate" -> state.copy(startDate = value)
                "endDate" -> state.copy(endDate = value)
                else -> state
            }
        }
    }
    
    fun updateIsCurrent(isCurrent: Boolean) {
        _uiState.update { it.copy(isCurrent = isCurrent) }
    }

    fun createAcademicYear() {
        val state = _uiState.value
        
        if (!state.isFormValid) {
            _uiState.update { it.copy(error = "Please fill all required fields.") }
            return
        }
        
        try {
            val start = java.time.LocalDate.parse(state.startDate)
            val end = java.time.LocalDate.parse(state.endDate)
            if (end.isBefore(start) || end.isEqual(start)) {
                _uiState.update { it.copy(error = "End Date must be after Start Date.") }
                return
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Invalid date format.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val request = AcademicYear(
                    academicYearId = 0,
                    yearName = state.yearName.trim(),
                    startDate = state.startDate.trim(), // YYYY-MM-DD
                    endDate = state.endDate.trim(),     // YYYY-MM-DD
                    isCurrent = state.isCurrent
                )
                
                val response = apiService.createAcademicYear(request)
                if (response.isSuccessful && response.body()?.data != null) {
                    val id = response.body()?.data?.academicYearId
                    _uiState.update { it.copy(isSaving = false, saveSuccess = true, createdAcademicYearId = id) }
                } else {
                    val errorMsg = try {
                        val errorString = response.errorBody()?.string()
                        if (errorString != null) {
                            org.json.JSONObject(errorString).getString("message")
                        } else {
                            response.message()
                        }
                    } catch (e: Exception) {
                        "Failed to create academic year"
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
        fun provideFactory(apiService: ApiService): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AcademicYearCreateViewModel(apiService) as T
            }
        }
    }
}
