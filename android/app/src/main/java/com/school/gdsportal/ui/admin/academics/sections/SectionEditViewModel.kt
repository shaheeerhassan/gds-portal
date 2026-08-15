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

data class SectionEditUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    
    val academicYears: List<AcademicYear> = emptyList(),
    val classes: List<SchoolClass> = emptyList(),
    
    val selectedAcademicYearId: Int? = null,
    val selectedClassId: Int? = null,
    val sectionName: String = "",
    val capacity: String = "",
    val roomNumber: String = "",
    
    val error: String? = null
)

class SectionEditViewModel(
    private val sectionId: Int,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SectionEditUiState())
    val uiState: StateFlow<SectionEditUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val yearsResponse = apiService.getAcademicYears()
                val classesResponse = apiService.getClasses()
                val sectionResponse = apiService.getSectionById(sectionId)

                val years = if (yearsResponse.isSuccessful) yearsResponse.body()?.data ?: emptyList() else emptyList()
                val classes = if (classesResponse.isSuccessful) classesResponse.body()?.data ?: emptyList() else emptyList()
                val section = if (sectionResponse.isSuccessful) sectionResponse.body()?.data else null

                if (section != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        academicYears = years,
                        classes = classes,
                        selectedAcademicYearId = section.academicYearId,
                        selectedClassId = section.classId,
                        sectionName = section.sectionName,
                        capacity = section.capacity?.toString() ?: "",
                        roomNumber = section.roomNumber ?: ""
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Section not found"
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

    fun updateSection() {
        val state = _uiState.value
        
        if (state.selectedAcademicYearId == null || state.selectedClassId == null || state.sectionName.isBlank()) {
            _uiState.value = state.copy(error = "Academic Year, Class, and Section Name are required")
            return
        }

        val cap = state.capacity.toIntOrNull()

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            try {
                val sectionToUpdate = Section(
                    sectionId = sectionId,
                    classId = state.selectedClassId,
                    academicYearId = state.selectedAcademicYearId,
                    sectionName = state.sectionName.trim(),
                    capacity = cap,
                    roomNumber = state.roomNumber.trim().takeIf { it.isNotEmpty() }
                )

                val response = apiService.updateSection(sectionId, sectionToUpdate)
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
                } else {
                    val errorMsg = try {
                        val errorString = response.errorBody()?.string()
                        if (errorString != null) {
                            org.json.JSONObject(errorString).getString("message")
                        } else {
                            response.message()
                        }
                    } catch (e: Exception) {
                        "Failed to update section"
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

    class Factory(private val sectionId: Int, private val apiService: ApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SectionEditViewModel(sectionId, apiService) as T
        }
    }
}
