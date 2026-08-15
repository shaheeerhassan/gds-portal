package com.school.gdsportal.ui.admin.students.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.CreateStudentData
import com.school.gdsportal.data.remote.CreateStudentRequest
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StudentCreateUiState(
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false,
    
    // User Form fields
    val email: String = "",
    val password: String = "",
    val username: String = "",
    
    // Student Form fields
    val registrationNumber: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val admissionDate: String = "",
    val dateOfBirth: String = "",
    val gender: String = ""
) {
    val isFormValid: Boolean
        get() = email.isNotBlank() &&
                password.isNotBlank() &&
                registrationNumber.isNotBlank() &&
                firstName.isNotBlank() &&
                lastName.isNotBlank() &&
                admissionDate.isNotBlank() &&
                gender.isNotBlank()
}

class StudentCreateViewModel(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentCreateUiState())
    val uiState: StateFlow<StudentCreateUiState> = _uiState.asStateFlow()

    fun updateField(field: String, value: String) {
        _uiState.update { state ->
            when (field) {
                "email" -> state.copy(email = value)
                "password" -> state.copy(password = value)
                "username" -> state.copy(username = value)
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

    fun createStudent() {
        val state = _uiState.value
        
        if (!state.isFormValid) {
            _uiState.update { it.copy(error = "Please fill all required fields.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val request = CreateStudentRequest(
                    email = state.email.trim(),
                    password = state.password.trim(),
                    username = state.username.trim().takeIf { it.isNotEmpty() },
                    student = CreateStudentData(
                        firstName = state.firstName.trim(),
                        lastName = state.lastName.trim(),
                        registrationNumber = state.registrationNumber.trim(),
                        admissionDate = state.admissionDate.trim(),
                        gender = state.gender.trim().uppercase(),
                        dateOfBirth = state.dateOfBirth.trim().takeIf { it.isNotEmpty() }
                    )
                )
                
                val response = apiService.createStudent(request)
                if (response.isSuccessful) {
                    _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                } else {
                    _uiState.update { 
                        it.copy(isSaving = false, error = "Failed to create student. Please check details and try again.")
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
        fun provideFactory(apiService: ApiService): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return StudentCreateViewModel(apiService) as T
            }
        }
    }
}
