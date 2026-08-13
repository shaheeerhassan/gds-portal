package com.school.gdsportal.ui.admin.parents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.Parent
import com.school.gdsportal.data.remote.Student
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ParentProfileUiState(
    val parent: Parent? = null,
    val linkedStudents: List<Student> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class ParentProfileViewModel(
    private val parentId: Long,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ParentProfileUiState())
    val uiState: StateFlow<ParentProfileUiState> = _uiState.asStateFlow()

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
                // Fetch Parent Details
                val parentResponse = apiService.getParent(parentId)
                if (!parentResponse.isSuccessful) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Failed to load parent details: ${parentResponse.code()}"
                        )
                    }
                    return@launch
                }
                
                val parentData = parentResponse.body()?.data
                if (parentData == null) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Parent data is null"
                        )
                    }
                    return@launch
                }

                // Fetch Linked Students
                val studentsResponse = apiService.getStudentsByParentId(parentId)
                val studentsData = if (studentsResponse.isSuccessful) {
                    studentsResponse.body()?.data ?: emptyList()
                } else {
                    emptyList() // Fallback to empty if students fail to load
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        parent = parentData,
                        linkedStudents = studentsData,
                        error = null
                    )
                }

            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "An unexpected error occurred"
                    )
                }
            }
        }
    }

    companion object {
        fun provideFactory(parentId: Long, apiService: ApiService): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(ParentProfileViewModel::class.java)) {
                        return ParentProfileViewModel(parentId, apiService) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}
