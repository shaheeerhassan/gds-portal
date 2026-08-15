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

data class PeriodEditUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val periodNumber: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val error: String? = null
)

class PeriodEditViewModel(
    private val periodId: Int,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(PeriodEditUiState())
    val uiState: StateFlow<PeriodEditUiState> = _uiState.asStateFlow()

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
                            periodNumber = period.periodNumber.toString(),
                            startTime = period.startTime,
                            endTime = period.endTime
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Period not found"
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load period"
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

    fun updatePeriod() {
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
            try {
                // Check if they came in short format HH:mm and pad them
                val formatterShort = DateTimeFormatter.ofPattern("HH:mm")
                val startLocal = LocalTime.parse(state.startTime, formatterShort)
                val endLocal = LocalTime.parse(state.endTime, formatterShort)
                if (!endLocal.isAfter(startLocal)) {
                    _uiState.value = state.copy(error = "End time must be after start time")
                    return
                }
            } catch (e2: Exception) {
                 _uiState.value = state.copy(error = "Invalid time format")
                 return
            }
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            try {
                // Ensure time is in HH:mm:ss
                var startFormatted = state.startTime
                var endFormatted = state.endTime
                if (startFormatted.length == 5) startFormatted = "$startFormatted:00"
                if (endFormatted.length == 5) endFormatted = "$endFormatted:00"

                val periodToUpdate = Period(
                    periodId = periodId,
                    periodNumber = pNum,
                    startTime = startFormatted,
                    endTime = endFormatted
                )

                val response = apiService.updatePeriod(periodId, periodToUpdate)
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
                } else {
                    val errorMsg = try {
                        val errorString = response.errorBody()?.string()
                        if (errorString != null) {
                            org.json.JSONObject(errorString).getString("message")
                        } else {
                            response.message()
                        }
                    } catch (e: Exception) {
                        "Failed to update period"
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

    class Factory(private val periodId: Int, private val apiService: ApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PeriodEditViewModel(periodId, apiService) as T
        }
    }
}
