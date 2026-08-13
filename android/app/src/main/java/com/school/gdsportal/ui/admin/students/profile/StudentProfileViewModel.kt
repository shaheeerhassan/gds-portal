package com.school.gdsportal.ui.admin.students.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.AcademicYear
import com.school.gdsportal.data.remote.Enrollment
import com.school.gdsportal.data.remote.SchoolClass
import com.school.gdsportal.data.remote.Section
import com.school.gdsportal.data.remote.Student
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StudentProfileUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val student: Student? = null,
    val user: com.school.gdsportal.data.remote.User? = null,
    val enrollment: Enrollment? = null,
    val academicYear: AcademicYear? = null,
    val schoolClass: SchoolClass? = null,
    val section: Section? = null
)

class StudentProfileViewModel(
    private val studentId: Long,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentProfileUiState())
    val uiState: StateFlow<StudentProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfileData()
    }

    fun retry() {
        loadProfileData()
    }

    private fun loadProfileData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // 1. Fetch Student Info
                val studentResponse = apiService.getStudent(studentId)
                if (studentResponse.isSuccessful && studentResponse.body()?.data != null) {
                    val student = studentResponse.body()!!.data!!
                    
                    var user: com.school.gdsportal.data.remote.User? = null
                    try {
                        val userRes = apiService.getUser(student.userId)
                        if (userRes.isSuccessful) {
                            user = userRes.body()?.data
                        }
                    } catch (e: Exception) {
                        // ignore
                    }

                    _uiState.update { it.copy(student = student, user = user) }

                    // 2. Fetch Enrollment Info
                    val enrollRes = apiService.getCurrentEnrollment(studentId)
                    val enrollment = if (enrollRes.isSuccessful) enrollRes.body()?.data else null
                    
                    if (enrollment != null) {
                        _uiState.update { it.copy(enrollment = enrollment) }
                        
                        // 3. Resolve Academic Year Name
                        val yearsRes = apiService.getAcademicYears()
                        if (yearsRes.isSuccessful) {
                            val year = yearsRes.body()?.data?.find { it.academicYearId == enrollment.academicYearId }
                            _uiState.update { it.copy(academicYear = year) }
                        }

                        // 4. Resolve Class Name
                        val classesRes = apiService.getClasses()
                        if (classesRes.isSuccessful) {
                            val cls = classesRes.body()?.data?.find { it.classId == enrollment.classId }
                            _uiState.update { it.copy(schoolClass = cls) }
                        }

                        // 5. Resolve Section Name
                        val sectionRes = apiService.getSections(enrollment.classId, enrollment.academicYearId)
                        if (sectionRes.isSuccessful) {
                            val sec = sectionRes.body()?.data?.find { it.sectionId == enrollment.sectionId }
                            _uiState.update { it.copy(section = sec) }
                        }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load student profile.") }
                    return@launch
                }
                
                _uiState.update { it.copy(isLoading = false) }

            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = "Unable to connect. Please check your network."
                    )
                }
            }
        }
    }

    fun endEnrollment() {
        val currentEnrollment = _uiState.value.enrollment
        if (currentEnrollment == null || !currentEnrollment.active) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = apiService.endEnrollment(studentId, currentEnrollment.academicYearId)
                if (response.isSuccessful) {
                    // Reload data to reflect the unenrolled state
                    loadProfileData()
                } else {
                    _uiState.update { 
                        it.copy(isLoading = false, error = "Failed to end enrollment.")
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isLoading = false, error = "Network error while ending enrollment.")
                }
            }
        }
    }

    fun deleteStudent(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = apiService.deleteStudent(studentId)
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    _uiState.update { 
                        it.copy(isLoading = false, error = "Failed to delete student.")
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isLoading = false, error = "Network error while deleting student.")
                }
            }
        }
    }

    fun updateUserStatus(active: Boolean) {
        val user = _uiState.value.user ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val req = com.school.gdsportal.data.remote.UpdateUserStatusRequest(user.userId, active)
                val response = apiService.updateUserStatus(req)
                if (response.isSuccessful) {
                    loadProfileData() // reload to get new status
                } else {
                    _uiState.update { 
                        it.copy(isLoading = false, error = "Failed to update user login status.")
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isLoading = false, error = "Network error while updating user login status.")
                }
            }
        }
    }

    companion object {
        fun provideFactory(studentId: Long, apiService: ApiService): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return StudentProfileViewModel(studentId, apiService) as T
            }
        }
    }
}
