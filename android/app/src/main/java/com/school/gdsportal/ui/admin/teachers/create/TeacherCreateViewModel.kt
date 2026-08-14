package com.school.gdsportal.ui.admin.teachers.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.TeacherCreateRequest
import com.school.gdsportal.data.remote.TeacherProfileFields
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TeacherCreateUiState(
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false,
    val createdTeacherId: Long? = null,
    
    // User Form fields
    val email: String = "",
    val password: String = "",
    val username: String = "",
    
    // Teacher Form fields
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
        get() = email.isNotBlank() &&
                password.isNotBlank() &&
                firstName.isNotBlank() &&
                lastName.isNotBlank() &&
                phone.isNotBlank() &&
                employeeId.isNotBlank() &&
                hireDate.isNotBlank()
}

class TeacherCreateViewModel(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherCreateUiState())
    val uiState: StateFlow<TeacherCreateUiState> = _uiState.asStateFlow()

    fun updateField(field: String, value: String) {
        _uiState.update { state ->
            when (field) {
                "email" -> state.copy(email = value)
                "password" -> state.copy(password = value)
                "username" -> state.copy(username = value)
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

    fun createTeacher() {
        val state = _uiState.value
        
        if (!state.isFormValid) {
            _uiState.update { it.copy(error = "Please fill all required fields.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val request = TeacherCreateRequest(
                    email = state.email.trim(),
                    password = state.password.trim(),
                    username = state.username.trim().takeIf { it.isNotEmpty() },
                    teacher = TeacherProfileFields(
                        firstName = state.firstName.trim(),
                        lastName = state.lastName.trim(),
                        phone = state.phone.trim(),
                        employeeId = state.employeeId.trim(),
                        hireDate = state.hireDate.trim(),
                        gender = state.gender.trim().uppercase().takeIf { it.isNotEmpty() },
                        dateOfBirth = state.dateOfBirth.trim().takeIf { it.isNotEmpty() },
                        qualification = state.qualification.trim().takeIf { it.isNotEmpty() }
                    )
                )
                
                val response = apiService.createTeacher(request)
                if (response.isSuccessful && response.body() != null && response.body()?.data != null) {
                    val teacherId = response.body()?.data?.teacherId
                    _uiState.update { it.copy(isSaving = false, saveSuccess = true, createdTeacherId = teacherId) }
                } else {
                    _uiState.update { 
                        it.copy(isSaving = false, error = "Failed to create teacher. Please check details and try again.")
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
                return TeacherCreateViewModel(apiService) as T
            }
        }
    }
}
