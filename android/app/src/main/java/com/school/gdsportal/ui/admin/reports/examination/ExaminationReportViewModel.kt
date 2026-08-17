package com.school.gdsportal.ui.admin.reports.examination

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.dto.ExaminationReportData
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ExaminationReportUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val reportData: ExaminationReportData? = null
)

class ExaminationReportViewModel(
    private val apiService: ApiService,
    private val examinationId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExaminationReportUiState())
    val uiState: StateFlow<ExaminationReportUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val res = apiService.getExaminationReport(examinationId)
                if (res.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        reportData = res.body()?.data
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load examination report."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Network error while loading examination report."
                )
            }
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    class Factory(
        private val apiService: ApiService,
        private val examinationId: Long
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ExaminationReportViewModel(apiService, examinationId) as T
        }
    }
}
