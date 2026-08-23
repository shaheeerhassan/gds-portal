package com.school.gdsportal.ui.teacher.students

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.Enrollment
import com.school.gdsportal.data.remote.Student
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TeacherStudentProfileUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val student: Student? = null,
    val enrollment: Enrollment? = null,
    val className: String? = null,
    val sectionName: String? = null
)

class TeacherStudentProfileViewModel(
    private val studentId: Long,
    private val apiService: ApiService
) : ViewModel() {
    private val _uiState = MutableStateFlow(TeacherStudentProfileUiState())
    val uiState: StateFlow<TeacherStudentProfileUiState> = _uiState.asStateFlow()

    init { loadProfile() }

    fun loadProfile() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                // Fetch student and their current class assignment at the same time
                val studentDef = async { apiService.getStudent(studentId) }
                val enrollmentDef = async { apiService.getCurrentEnrollment(studentId) }

                val studentRes = studentDef.await()
                val enrollmentRes = enrollmentDef.await()

                if (studentRes.isSuccessful) {
                    val enrollmentData = if (enrollmentRes.isSuccessful) enrollmentRes.body()?.data else null
                    var className: String? = null
                    var sectionName: String? = null
                    
                    if (enrollmentData != null) {
                        try {
                            val classRes = apiService.getClassById(enrollmentData.classId)
                            val sectionRes = apiService.getSectionById(enrollmentData.sectionId)
                            className = classRes.body()?.data?.className
                            sectionName = sectionRes.body()?.data?.sectionName
                        } catch (e: Exception) {
                            // Ignore if we can't fetch names
                        }
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            student = studentRes.body()?.data,
                            enrollment = enrollmentData,
                            className = className ?: enrollmentData?.classId?.toString(),
                            sectionName = sectionName ?: enrollmentData?.sectionId?.toString()
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load student profile.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Network error.") }
            }
        }
    }

    companion object {
        fun provideFactory(studentId: Long, apiService: ApiService): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TeacherStudentProfileViewModel(studentId, apiService) as T
                }
            }
    }
}
