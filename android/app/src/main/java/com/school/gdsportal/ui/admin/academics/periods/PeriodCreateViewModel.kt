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
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

data class PeriodCreateUiState(
    val isSaving: Boolean = false,
    val periodNumber: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val error: String? = null,
    val createdPeriodId: Int? = null
)

class PeriodCreateViewModel(private val apiService: ApiService) : ViewModel() {
    private val _uiState = MutableStateFlow(PeriodCreateUiState())
    val uiState: StateFlow<PeriodCreateUiState> = _uiState.asStateFlow()

    fun updateForm(
        periodNumber: String = _uiState.value.periodNumber,
        startTime: String = _uiState.value.startTime,
        endTime: String = _uiState.value.endTime
    ) {
        _uiState.value = _uiState.value.copy(
            periodNumber = periodNumber,
            startTime = startTime,
            endTime = endTime
        )
    }

    fun createPeriod() {
        val state = _uiState.value
        
        if (state.periodNumber.isBlank() || state.startTime.isBlank() || state.endTime.isBlank()) {
            _uiState.value = state.copy(error = "All fields are required")
            return
        }

        val pNum = state.periodNumber.toIntOrNull()
        if (pNum == null) {
            _uiState.value = state.copy(error = "Period number must be a valid number")
            return
        }

        // Validate time
        try {
            val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
            val startLocal = LocalTime.parse(state.startTime, formatter)
            val endLocal = LocalTime.parse(state.endTime, formatter)
            
            if (!endLocal.isAfter(startLocal)) {
                _uiState.value = state.copy(error = "End time must be after start time")
                return
            }
        } catch (e: DateTimeParseException) {
            _uiState.value = state.copy(error = "Invalid time format")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            try {
                val newPeriod = Period(
                    periodId = 0,
                    periodNumber = pNum,
                    startTime = state.startTime,
                    endTime = state.endTime
                )

                val response = apiService.createPeriod(newPeriod)
                if (response.isSuccessful) {
                    val createdId = response.body()?.data?.periodId
                    _uiState.value = _uiState.value.copy(isSaving = false, createdPeriodId = createdId ?: -1)
                } else {
                    val errorMsg = try {
                        val errorString = response.errorBody()?.string()
                        if (errorString != null) {
                            org.json.JSONObject(errorString).getString("message")
                        } else {
                            response.message()
                        }
                    } catch (e: Exception) {
                        "Failed to create period"
                    }
                    _uiState.value = _uiState.value.copy(isSaving = false, error = errorMsg)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = "Network error: ${e.message}"
                )
            }
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    companion object {
        fun provideFactory(apiService: ApiService): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PeriodCreateViewModel(apiService) as T
            }
        }
    }
}
