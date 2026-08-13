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
                val studentRes = apiService.getStudent(studentId)
                if (!studentRes.isSuccessful || studentRes.body()?.data == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load student profile.") }
                    return@launch
                }
                val student = studentRes.body()!!.data
                _uiState.update { it.copy(student = student) }

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

    companion object {
        fun provideFactory(studentId: Long, apiService: ApiService): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return StudentProfileViewModel(studentId, apiService) as T
            }
        }
    }
}
