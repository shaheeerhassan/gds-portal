package com.school.gdsportal.ui.admin.academics.sections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.Section
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SectionDetailUiState(
    val isLoading: Boolean = true,
    val section: Section? = null,
    val className: String = "",
    val academicYearName: String = "",
    val isDeleting: Boolean = false,
    val deleteSuccess: Boolean = false,
    val error: String? = null
)

class SectionDetailViewModel(
    private val sectionId: Int,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SectionDetailUiState())
    val uiState: StateFlow<SectionDetailUiState> = _uiState.asStateFlow()

    init {
        loadSection()
    }

    private fun loadSection() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = apiService.getSectionById(sectionId)
                if (response.isSuccessful) {
                    val section = response.body()?.data
                    if (section != null) {
                        // Resolve names
                        var className = "Unknown Class"
                        var yearName = "Unknown Year"
                        
                        val classResponse = apiService.getClassById(section.classId)
                        if (classResponse.isSuccessful) {
                            className = classResponse.body()?.data?.className ?: className
                        }
                        
                        val yearResponse = apiService.getAcademicYearById(section.academicYearId)
                        if (yearResponse.isSuccessful) {
                            yearName = yearResponse.body()?.data?.yearName ?: yearName
                        }

                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            section = section,
                            className = className,
                            academicYearName = yearName
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false, error = "Section not found")
                    }
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to load section")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Network error: ${e.message}")
            }
        }
    }

    fun deleteSection() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true, error = null)
            try {
                val response = apiService.deleteSection(sectionId)
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
                        "Failed to delete section"
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

    class Factory(private val sectionId: Int, private val apiService: ApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SectionDetailViewModel(sectionId, apiService) as T
        }
    }
}
