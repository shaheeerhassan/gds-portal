package com.school.gdsportal.ui.parent.children

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.local.TokenManager
import com.school.gdsportal.data.remote.Student
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyChildrenUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val children: List<Student> = emptyList()
)

class MyChildrenViewModel(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyChildrenUiState())
    val uiState: StateFlow<MyChildrenUiState> = _uiState.asStateFlow()

    init {
        loadChildren()
    }

    fun loadChildren() {
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val parentResponse = apiService.getCurrentParent()
                val parentId = parentResponse.body()?.data?.parentId

                if (parentId == null || parentId == 0L) {
                    _uiState.update { it.copy(isLoading = false, error = "Parent profile not found.") }
                    return@launch
                }

                val response = apiService.getStudentsByParentId(parentId)

                if (response.isSuccessful) {
                    val students = response.body()?.data ?: emptyList()
                    _uiState.update { it.copy(isLoading = false, children = students) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load children.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Network error. Please try again.") }
            }
        }
    }

    companion object {
        fun provideFactory(apiService: ApiService, tokenManager: TokenManager): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MyChildrenViewModel(apiService, tokenManager) as T
                }
            }
    }
}