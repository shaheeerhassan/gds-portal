package com.school.gdsportal.ui.student.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.User
import com.school.gdsportal.data.remote.Student
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StudentProfileUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,

    // Original Objects
    val user: User? = null,
    val student: Student? = null,

    // Editable Personal Info
    val firstName: String = "",
    val lastName: String = "",
    val gender: String = "",
    val dateOfBirth: String = "", // e.g., "YYYY-MM-DD"

    // Read-only Academic Info
    val registrationNumber: String = "",
    val className: String = "",
    val sectionName: String = "",
    val rollNumber: String = ""
)

class StudentProfileViewModel(private val apiService: ApiService) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentProfileUiState())
    val uiState: StateFlow<StudentProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
        viewModelScope.launch {
            try {
                val userRes = apiService.getCurrentUser()
                val studentRes = apiService.getStudentMe()

                val user = userRes.body()?.data
                val student = studentRes.body()?.data

                if (user != null && student != null) {
                    // Fetch Academic details safely
                    var cName = "N/A"
                    var sName = "N/A"
                    var rNum = "N/A"

                    try {
                        val enrollRes = apiService.getCurrentEnrollment(student.studentId)
                        val enrollment = enrollRes.body()?.data
                        if (enrollment != null) {
                            rNum = enrollment.rollNumber?.toString() ?: "N/A"
                            val secRes = apiService.getSectionById(enrollment.sectionId)
                            val section = secRes.body()?.data
                            if (section != null) {
                                sName = section.sectionName
                                val clsRes = apiService.getClassById(section.classId)
                                cName = clsRes.body()?.data?.className ?: "N/A"
                            }
                        }
                    } catch (e: Exception) {
                        // Enrollment might not exist, ignore and keep N/A
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            user = user,
                            student = student,
                            // FIX: Extract personal info from Student, not User
                            firstName = student.firstName ?: "",
                            lastName = student.lastName ?: "",
                            gender = student.gender ?: "",
                            dateOfBirth = student.dateOfBirth ?: "",
                            registrationNumber = student.registrationNumber ?: "N/A",
                            className = cName,
                            sectionName = sName,
                            rollNumber = rNum
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load profile data.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Network error: ${e.localizedMessage}") }
            }
        }
    }

    // Input Handlers
    fun onFirstNameChange(name: String) { _uiState.update { it.copy(firstName = name) } }
    fun onLastNameChange(name: String) { _uiState.update { it.copy(lastName = name) } }
    fun onGenderChange(gender: String) { _uiState.update { it.copy(gender = gender) } }
    fun onDateOfBirthChange(dob: String) { _uiState.update { it.copy(dateOfBirth = dob) } }

    fun saveProfile() {
        val state = _uiState.value
        val currentStudent = state.student ?: return

        if (state.firstName.isBlank() || state.lastName.isBlank()) {
            _uiState.update { it.copy(error = "First and Last names are required.") }
            return
        }

        _uiState.update { it.copy(isSaving = true, error = null, successMessage = null) }

        viewModelScope.launch {
            try {
                // FIX: Update Student Object, not User
                val updatedStudent = currentStudent.copy(
                    firstName = state.firstName.trim(),
                    lastName = state.lastName.trim(),
                    gender = state.gender.ifBlank { null },
                    dateOfBirth = state.dateOfBirth.ifBlank { null }
                )

                val response = apiService.updateStudent(currentStudent.studentId, updatedStudent)
                if (response.isSuccessful) {
                    _uiState.update { it.copy(isSaving = false, successMessage = "Profile updated successfully!", student = updatedStudent) }
                } else {
                    _uiState.update { it.copy(isSaving = false, error = "Failed to update profile.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = "Network error during update.") }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }

    companion object {
        fun provideFactory(apiService: ApiService): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return StudentProfileViewModel(apiService) as T
                }
            }
    }
}