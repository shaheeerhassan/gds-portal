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

data class SectionCreateUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val academicYears: List<AcademicYear> = emptyList(),
    val classes: List<SchoolClass> = emptyList(),
    
    val selectedAcademicYearId: Int? = null,
    val selectedClassId: Int? = null,
    val sectionName: String = "",
    val capacity: String = "",
    val roomNumber: String = "",
    
    val error: String? = null,
    val createdSectionId: Int? = null
)

class SectionCreateViewModel(private val apiService: ApiService) : ViewModel() {
    private val _uiState = MutableStateFlow(SectionCreateUiState())
    val uiState: StateFlow<SectionCreateUiState> = _uiState.asStateFlow()

    init {
        loadDropdownData()
    }

    private fun loadDropdownData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val yearsResponse = apiService.getAcademicYears()
                val classesResponse = apiService.getClasses()

                val years = if (yearsResponse.isSuccessful) yearsResponse.body()?.data ?: emptyList() else emptyList()
                val classes = if (classesResponse.isSuccessful) classesResponse.body()?.data ?: emptyList() else emptyList()

                _uiState.value = _uiState.value.copy(
                    academicYears = years,
                    classes = classes,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load dropdown data: ${e.message}"
                )
            }
        }
    }

    fun updateForm(
        academicYearId: Int? = _uiState.value.selectedAcademicYearId,
        classId: Int? = _uiState.value.selectedClassId,
        sectionName: String = _uiState.value.sectionName,
        capacity: String = _uiState.value.capacity,
        roomNumber: String = _uiState.value.roomNumber
    ) {
        _uiState.value = _uiState.value.copy(
            selectedAcademicYearId = academicYearId,
            selectedClassId = classId,
            sectionName = sectionName,
            capacity = capacity,
            roomNumber = roomNumber
        )
    }

    fun createSection() {
        val state = _uiState.value
        
        if (state.selectedAcademicYearId == null || state.selectedClassId == null || state.sectionName.isBlank()) {
            _uiState.value = state.copy(error = "Academic Year, Class, and Section Name are required")
            return
        }

        val cap = state.capacity.toIntOrNull()

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            try {
                val newSection = Section(
                    sectionId = 0,
                    classId = state.selectedClassId,
                    academicYearId = state.selectedAcademicYearId,
                    sectionName = state.sectionName.trim(),
                    capacity = cap,
                    roomNumber = state.roomNumber.trim().takeIf { it.isNotEmpty() }
                )

                val response = apiService.createSection(newSection)
                if (response.isSuccessful) {
                    val createdId = response.body()?.data?.sectionId
                    _uiState.value = _uiState.value.copy(isSaving = false, createdSectionId = createdId ?: -1)
                } else {
                    val errorMsg = try {
                        val errorString = response.errorBody()?.string()
                        if (errorString != null) {
                            org.json.JSONObject(errorString).getString("message")
                        } else {
                            response.message()
                        }
                    } catch (e: Exception) {
                        "Failed to create section"
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

    companion object {
        fun provideFactory(apiService: ApiService): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SectionCreateViewModel(apiService) as T
            }
        }
    }
}
