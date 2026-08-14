package com.school.gdsportal.ui.admin.teachers.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.TeacherProfileFields
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TeacherEditUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false,
    
    val teacherId: Long = 0,
    
    // Teacher Form fields (Account fields omitted as they cannot be updated here)
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "",
    val employeeId: String = "",
    val hireDate: String = "",
    val dateOfBirth: String = "",
    val gender: String = "",
    val qualification: String = ""
) {
    val isFormValid: Boolean
        get() = firstName.isNotBlank() &&
                lastName.isNotBlank() &&
                phone.isNotBlank() &&
                employeeId.isNotBlank() &&
                hireDate.isNotBlank()
}

class TeacherEditViewModel(
    private val teacherId: Long,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherEditUiState(teacherId = teacherId))
    val uiState: StateFlow<TeacherEditUiState> = _uiState.asStateFlow()

    init {
        loadTeacher()
    }

    private fun loadTeacher() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = apiService.getTeacherById(teacherId)
                if (response.isSuccessful && response.body() != null && response.body()?.data != null) {
                    val teacher = response.body()!!.data!!
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            firstName = teacher.firstName,
                            lastName = teacher.lastName,
                            phone = teacher.phone ?: "",
                            employeeId = teacher.employeeId,
                            hireDate = teacher.hireDate,
                            dateOfBirth = teacher.dateOfBirth ?: "",
                            gender = teacher.gender,
                            qualification = teacher.qualification ?: ""
                        )
                    }
                } else {
                    _uiState.update { 
                        it.copy(isLoading = false, error = "Failed to load teacher details.")
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isLoading = false, error = "Network error while loading teacher.")
                }
            }
        }
    }

    fun updateField(field: String, value: String) {
        _uiState.update { state ->
            when (field) {
                "firstName" -> state.copy(firstName = value)
                "lastName" -> state.copy(lastName = value)
                "phone" -> state.copy(phone = value)
                "employeeId" -> state.copy(employeeId = value)
                "hireDate" -> state.copy(hireDate = value)
                "dateOfBirth" -> state.copy(dateOfBirth = value)
                "gender" -> state.copy(gender = value)
                "qualification" -> state.copy(qualification = value)
                else -> state
            }
        }
    }

    fun updateTeacher() {
        val state = _uiState.value
        
        if (!state.isFormValid) {
            _uiState.update { it.copy(error = "Please fill all required fields.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val request = TeacherProfileFields(
                    firstName = state.firstName.trim(),
                    lastName = state.lastName.trim(),
                    phone = state.phone.trim(),
                    employeeId = state.employeeId.trim(),
                    hireDate = state.hireDate.trim(),
                    gender = state.gender.trim().uppercase().takeIf { it.isNotEmpty() },
                    dateOfBirth = state.dateOfBirth.trim().takeIf { it.isNotEmpty() },
                    qualification = state.qualification.trim().takeIf { it.isNotEmpty() }
                )
                
                val response = apiService.updateTeacher(teacherId, request)
                if (response.isSuccessful) {
                    _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                    _uiState.update { 
                        it.copy(isSaving = false, error = "Failed to update teacher.\n$errorMsg")
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isSaving = false, error = "Network error while saving. Please try again.")
                }
            }
        }
    }
    
    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    companion object {
        fun provideFactory(
            teacherId: Long,
            apiService: ApiService
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TeacherEditViewModel(teacherId, apiService) as T
            }
        }
    }
}
