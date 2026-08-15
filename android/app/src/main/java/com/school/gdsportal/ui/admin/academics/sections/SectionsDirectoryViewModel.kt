package com.school.gdsportal.ui.admin.academics.sections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.AcademicYear
import com.school.gdsportal.data.remote.SchoolClass
import com.school.gdsportal.data.remote.Section
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SectionsDirectoryUiState(
    val isLoading: Boolean = false,
    val sections: List<Section> = emptyList(),
    val filteredSections: List<Section> = emptyList(),
    val academicYears: List<AcademicYear> = emptyList(),
    val classes: List<SchoolClass> = emptyList(),
    val selectedAcademicYearId: Int? = null,
    val selectedClassId: Int? = null,
    val searchQuery: String = "",
    val error: String? = null
)

class SectionsDirectoryViewModel(private val apiService: ApiService) : ViewModel() {

    private val _uiState = MutableStateFlow(SectionsDirectoryUiState())
    val uiState: StateFlow<SectionsDirectoryUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // Preload all academic years and classes to build in-memory maps
                val yearsResponse = apiService.getAcademicYears()
                val classesResponse = apiService.getClasses()

                val years = if (yearsResponse.isSuccessful) {
                    yearsResponse.body()?.data ?: emptyList()
                } else {
                    emptyList()
                }

                val classes = if (classesResponse.isSuccessful) {
                    classesResponse.body()?.data ?: emptyList()
                } else {
                    emptyList()
                }

                _uiState.value = _uiState.value.copy(
                    academicYears = years,
                    classes = classes,
                    isLoading = false
                )

                // By default, fetch all sections if no filter is active
                loadSections(null, null)

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load directory data: ${e.message}"
                )
            }
        }
    }

    fun applyFilter(academicYearId: Int, classId: Int) {
        _uiState.value = _uiState.value.copy(
            selectedAcademicYearId = academicYearId,
            selectedClassId = classId
        )
        loadSections(academicYearId, classId)
    }

    fun clearFilter() {
        _uiState.value = _uiState.value.copy(
            selectedAcademicYearId = null,
            selectedClassId = null
        )
        loadSections(null, null)
    }

    private fun loadSections(academicYearId: Int?, classId: Int?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = if (academicYearId != null && classId != null) {
                    apiService.getSections(classId, academicYearId)
                } else {
                    apiService.getAllSections()
                }

                if (response.isSuccessful) {
                    val list = response.body()?.data ?: emptyList()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        sections = list
                    )
                    applySearch(_uiState.value.searchQuery, list)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load sections"
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

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applySearch(query, _uiState.value.sections)
    }

    private fun applySearch(query: String, allSections: List<Section>) {
        val lowerQuery = query.lowercase().trim()
        val filtered = if (lowerQuery.isEmpty()) {
            allSections
        } else {
            allSections.filter { section ->
                section.sectionName.lowercase().contains(lowerQuery)
            }
        }
        _uiState.value = _uiState.value.copy(filteredSections = filtered)
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    class Factory(private val apiService: ApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SectionsDirectoryViewModel(apiService) as T
        }
    }
}
