package com.school.gdsportal.ui.admin.students.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.Student
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StudentEditUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false,
    
    // Original student to hold onto IDs and untouched fields
    val originalStudent: Student? = null,
    
    // Form fields
    val registrationNumber: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val admissionDate: String = "",
    val dateOfBirth: String = "",
    val gender: String = ""
) {
    val isFormValid: Boolean
        get() = registrationNumber.isNotBlank() &&
                firstName.isNotBlank() &&
                lastName.isNotBlank() &&
                admissionDate.isNotBlank()
}

class StudentEditViewModel(
    private val studentId: Long,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentEditUiState())
    val uiState: StateFlow<StudentEditUiState> = _uiState.asStateFlow()

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
                val response = apiService.getStudent(studentId)
                if (response.isSuccessful && response.body()?.data != null) {
                    val student = response.body()!!.data!!
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            originalStudent = student,
                            registrationNumber = student.registrationNumber,
                            firstName = student.firstName,
                            lastName = student.lastName,
                            admissionDate = student.admissionDate ?: "",
                            dateOfBirth = student.dateOfBirth ?: "",
                            gender = student.gender ?: ""
                        )
                    }
                } else {
                    _uiState.update { 
                        it.copy(isLoading = false, error = "Failed to load student for editing.")
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isLoading = false, error = "Network error. Please try again.")
                }
            }
        }
    }

    fun updateField(field: String, value: String) {
        _uiState.update { state ->
            when (field) {
                "registrationNumber" -> state.copy(registrationNumber = value)
                "firstName" -> state.copy(firstName = value)
                "lastName" -> state.copy(lastName = value)
                "admissionDate" -> state.copy(admissionDate = value)
                "dateOfBirth" -> state.copy(dateOfBirth = value)
                "gender" -> state.copy(gender = value)
                else -> state
            }
        }
    }

    fun saveChanges() {
        val state = _uiState.value
        val original = state.originalStudent ?: return
        
        if (!state.isFormValid) {
            _uiState.update { it.copy(error = "Please fill all required fields.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val updatedStudent = original.copy(
                    registrationNumber = state.registrationNumber.trim(),
                    firstName = state.firstName.trim(),
                    lastName = state.lastName.trim(),
                    admissionDate = state.admissionDate.trim(),
                    dateOfBirth = state.dateOfBirth.trim().takeIf { it.isNotEmpty() },
                    gender = state.gender.trim().takeIf { it.isNotEmpty() }?.uppercase()
                )
                
                val response = apiService.updateStudent(studentId, updatedStudent)
                if (response.isSuccessful) {
                    _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                } else {
                    _uiState.update { 
                        it.copy(isSaving = false, error = "Failed to save changes. Please try again.")
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
        fun provideFactory(studentId: Long, apiService: ApiService): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return StudentEditViewModel(studentId, apiService) as T
            }
        }
    }
}
