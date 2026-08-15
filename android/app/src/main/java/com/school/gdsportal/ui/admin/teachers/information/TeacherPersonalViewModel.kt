package com.school.gdsportal.ui.admin.teachers.information

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.Teacher
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TeacherPersonalUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val teacher: Teacher? = null
)

class TeacherPersonalViewModel(
    private val teacherId: Long,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherPersonalUiState())
    val uiState: StateFlow<TeacherPersonalUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun retry() {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = apiService.getTeacherById(teacherId)
                if (response.isSuccessful && response.body()?.data != null) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            teacher = response.body()!!.data
                        )
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Failed to load personal information."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Network error. Please try again."
                    )
                }
            }
        }
    }

    companion object {
        fun provideFactory(teacherId: Long, apiService: ApiService): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TeacherPersonalViewModel(teacherId, apiService) as T
            }
        }
    }
}
