package com.school.gdsportal.ui.admin.teaching.timetable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TimetableDetailUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val entry: TimetableDisplay? = null,
    val deleteSuccess: Boolean = false
)

class TimetableDetailViewModel(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimetableDetailUiState())
    val uiState: StateFlow<TimetableDetailUiState> = _uiState.asStateFlow()

    fun setEntry(entry: TimetableDisplay) {
        _uiState.value = _uiState.value.copy(entry = entry)
    }

    fun deleteEntry() {
        val id = _uiState.value.entry?.timetableId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val res = apiService.deleteTimetable(id)
                if (res.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isLoading = false, deleteSuccess = true)
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to delete timetable entry")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Network error: ${e.message}")
            }
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    class Factory(private val apiService: ApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TimetableDetailViewModel(apiService) as T
        }
    }
}
