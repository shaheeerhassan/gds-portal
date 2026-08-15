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

data class ClassesDirectoryUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val classes: List<SchoolClass> = emptyList(),
    val searchQuery: String = ""
) {
    val filteredClasses: List<SchoolClass>
        get() = if (searchQuery.isBlank()) {
            classes
        } else {
            classes.filter {
                it.className.contains(searchQuery, ignoreCase = true) ||
                it.numericLevel.toString().contains(searchQuery)
            }
        }
}

class ClassesDirectoryViewModel(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClassesDirectoryUiState())
    val uiState: StateFlow<ClassesDirectoryUiState> = _uiState.asStateFlow()

    fun loadClasses() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = apiService.getClasses()
                if (response.isSuccessful && response.body()?.data != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        classes = response.body()!!.data!!
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
                        "Failed to load classes"
                    }
                    _uiState.value = _uiState.value.copy(isLoading = false, error = errorMsg)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Network error: ${e.message}")
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    class Factory(private val apiService: ApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ClassesDirectoryViewModel(apiService) as T
        }
    }
}
