package com.school.gdsportal.ui.admin.academics.classes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.SchoolClass
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ClassDetailUiState(
    val isLoading: Boolean = true,
    val isDeleting: Boolean = false,
    val deleteSuccess: Boolean = false,
    val error: String? = null,
    val schoolClass: SchoolClass? = null
)

class ClassDetailViewModel(
    private val classId: Int,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClassDetailUiState())
    val uiState: StateFlow<ClassDetailUiState> = _uiState.asStateFlow()

    init {
        loadClass()
    }

    fun loadClass() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = apiService.getClassById(classId)
                if (response.isSuccessful && response.body()?.data != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        schoolClass = response.body()!!.data
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
                        "Failed to load class details"
                    }
                    _uiState.value = _uiState.value.copy(isLoading = false, error = errorMsg)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Network error: ${e.message}"
                )
            }
        }
    }

    fun deleteClass() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true, error = null)
            try {
                val response = apiService.deleteClass(classId)
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
                        "Failed to delete class"
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

    class Factory(
        private val classId: Int,
        private val apiService: ApiService
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ClassDetailViewModel(classId, apiService) as T
        }
    }
}
