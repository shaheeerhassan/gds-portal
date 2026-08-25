package com.school.gdsportal.ui.student.academics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.*
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StudentAcademicsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,

    val currentYear: AcademicYear? = null,
    val className: String = "",
    val sectionName: String = "",

    val subjects: List<Subject> = emptyList(),
    val teachers: List<Teacher> = emptyList(),
    val timetable: List<Timetable> = emptyList()
)

class StudentAcademicsViewModel(private val apiService: ApiService) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentAcademicsUiState())
    val uiState: StateFlow<StudentAcademicsUiState> = _uiState.asStateFlow()

    init {
        loadAcademicData()
    }

    fun loadAcademicData() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val studentRes = apiService.getStudentMe()
                val yearRes = apiService.getCurrentAcademicYear()

                val student = studentRes.body()?.data
                val year = yearRes.body()?.data

                if (student != null && year != null) {
                    val enrollRes = apiService.getCurrentEnrollment(student.studentId)
                    val enrollment = enrollRes.body()?.data

                    if (enrollment != null) {
                        // Resolve Class/Section Name
                        var cName = "Unknown Class"
                        var sName = "Unknown Section"
                        val secRes = apiService.getSectionById(enrollment.sectionId)
                        val section = secRes.body()?.data
                        if (section != null) {
                            sName = section.sectionName
                            val clsRes = apiService.getClassById(section.classId)
                            cName = clsRes.body()?.data?.className ?: "Class"
                        }

                        // Concurrently fetch Subjects, Teachers, and Timetable
                        val subjectsDef = async { apiService.getSubjectsBySection(enrollment.sectionId, year.academicYearId) }
                        val teachersDef = async { apiService.getTeachersBySection(enrollment.sectionId, year.academicYearId) }
                        val timetableDef = async { apiService.getTimetableBySection(enrollment.sectionId, year.academicYearId) }

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                currentYear = year,
                                className = cName,
                                sectionName = sName,
                                subjects = subjectsDef.await().body()?.data ?: emptyList(),
                                teachers = teachersDef.await().body()?.data ?: emptyList(),
                                timetable = timetableDef.await().body()?.data ?: emptyList()
                            )
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = "No active enrollment found.") }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load profile data.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Network error: ${e.localizedMessage}") }
            }
        }
    }

    companion object {
        fun provideFactory(apiService: ApiService): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return StudentAcademicsViewModel(apiService) as T
                }
            }
    }
}