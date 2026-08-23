package com.school.gdsportal.ui.teacher.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.TeacherProfileFields
import com.school.gdsportal.data.remote.User
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TeacherProfileUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val userId: Long = 0L,
    val email: String = "",
    val roleId: Int = 0,
    val username: String = "",
    val teacherId: Long = 0L,
    val employeeId: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "",
    val gender: String = "",
    val dateOfBirth: String = "",
    val hireDate: String = "",
    val qualification: String = "",

    // Edit mode
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saveSuccess: Boolean = false,

    // Working copies
    val editFirstName: String = "",
    val editLastName: String = "",
    val editUsername: String = "",
    val editPhone: String = "",
    val editGender: String = "",
    val editDateOfBirth: String = "",
    val editQualification: String = ""
) {
    val fullName: String get() = "$firstName $lastName".trim()
    val isFormValid: Boolean
        get() = editFirstName.isNotBlank() && editLastName.isNotBlank() && editUsername.isNotBlank()
}

class TeacherProfileViewModel(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherProfileUiState())
    val uiState: StateFlow<TeacherProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val userResp = apiService.getCurrentUser()
                if (!userResp.isSuccessful || userResp.body()?.data == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load user info.") }
                    return@launch
                }
                val user = userResp.body()!!.data!!

                val teacherResp = apiService.getTeacherMe()
                if (!teacherResp.isSuccessful || teacherResp.body()?.data == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load teacher details.") }
                    return@launch
                }
                val teacher = teacherResp.body()!!.data!!

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        userId = user.userId,
                        email = user.email,
                        roleId = user.roleId ?: 1,
                        username = user.username ?: "",
                        teacherId = teacher.teacherId,
                        employeeId = teacher.employeeId,
                        firstName = teacher.firstName,
                        lastName = teacher.lastName,
                        phone = teacher.phone ?: "",
                        gender = teacher.gender,
                        dateOfBirth = teacher.dateOfBirth ?: "",
                        hireDate = teacher.hireDate,
                        qualification = teacher.qualification ?: ""
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Network error. Please try again.") }
            }
        }
    }

    fun startEditing() {
        val s = _uiState.value
        _uiState.update {
            it.copy(
                isEditing = true,
                saveError = null,
                editFirstName = s.firstName,
                editLastName = s.lastName,
                editUsername = s.username,
                editPhone = s.phone,
                editGender = s.gender,
                editDateOfBirth = s.dateOfBirth,
                editQualification = s.qualification
            )
        }
    }

    fun cancelEditing() {
        _uiState.update { it.copy(isEditing = false, saveError = null) }
    }

    fun updateEditField(field: String, value: String) {
        _uiState.update { s ->
            when (field) {
                "firstName" -> s.copy(editFirstName = value)
                "lastName"  -> s.copy(editLastName = value)
                "username"  -> s.copy(editUsername = value)
                "phone"     -> s.copy(editPhone = value)
                "gender"    -> s.copy(editGender = value)
                "dateOfBirth" -> s.copy(editDateOfBirth = value)
                "qualification" -> s.copy(editQualification = value)
                else        -> s
            }
        }
    }

    fun saveProfile() {
        val s = _uiState.value
        if (!s.isFormValid) {
            _uiState.update { it.copy(saveError = "First name, last name, and username are required.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null) }
            try {
                // 1. Update Teacher
                val teacherReq = TeacherProfileFields(
                    employeeId = s.employeeId,
                    firstName = s.editFirstName.trim(),
                    lastName = s.editLastName.trim(),
                    phone = s.editPhone.trim(),
                    hireDate = s.hireDate, // Not editable
                    dateOfBirth = s.editDateOfBirth.takeIf { it.isNotBlank() },
                    gender = s.editGender.takeIf { it.isNotBlank() },
                    qualification = s.editQualification.takeIf { it.isNotBlank() }
                )
                val tResp = apiService.updateTeacher(s.teacherId, teacherReq)
                if (!tResp.isSuccessful) {
                    _uiState.update { it.copy(isSaving = false, saveError = "Failed to update teacher profile.") }
                    return@launch
                }

                // 2. Update User
                if (s.editUsername.trim() != s.username || s.editFirstName.trim() != s.firstName || s.editLastName.trim() != s.lastName) {
                    val uReq = User(
                        userId = s.userId,
                        username = s.editUsername.trim(),
                        email = s.email,
                        firstName = s.editFirstName.trim(),
                        lastName = s.editLastName.trim()
                    )
                    val uResp = apiService.updateUser(s.userId, uReq)
                    if (!uResp.isSuccessful) {
                        _uiState.update { it.copy(isSaving = false, saveError = "Failed to update user account details.") }
                        return@launch
                    }
                }

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        isEditing = false,
                        saveSuccess = true,
                        firstName = it.editFirstName.trim(),
                        lastName = it.editLastName.trim(),
                        username = it.editUsername.trim(),
                        phone = it.editPhone.trim(),
                        gender = it.editGender,
                        dateOfBirth = it.editDateOfBirth,
                        qualification = it.editQualification
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = "Network error while saving.") }
            }
        }
    }

    fun dismissSaveError() {
        _uiState.update { it.copy(saveError = null) }
    }

    fun dismissSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    companion object {
        fun provideFactory(apiService: ApiService): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    TeacherProfileViewModel(apiService) as T
            }
    }
}
