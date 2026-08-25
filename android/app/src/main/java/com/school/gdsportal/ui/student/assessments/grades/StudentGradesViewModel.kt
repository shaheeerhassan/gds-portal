package com.school.gdsportal.ui.student.assessment.grades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.Mark
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SubjectGradeGroup(
    val subjectName: String,
    val marks: List<MarkItem>
)

data class MarkItem(
    val examName: String,
    val obtained: Double?,
    val maxMarks: Double,
    val grade: String?,
    val remarks: String?
)

data class StudentGradesUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val groupedGrades: List<SubjectGradeGroup> = emptyList()
)

class StudentGradesViewModel(private val apiService: ApiService) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentGradesUiState())
    val uiState: StateFlow<StudentGradesUiState> = _uiState.asStateFlow()

    init {
        loadGrades()
    }

    private fun loadGrades() {
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
                        // Fetch Marks, Exams, and Subjects
                        val marksRes = apiService.getMarksByStudentAndYear(student.studentId, year.academicYearId)
                        val examsRes = apiService.getExaminationsBySection(enrollment.sectionId, year.academicYearId)
                        val subjectsRes = apiService.getSubjectsBySection(enrollment.sectionId, year.academicYearId)

                        val rawMarks = marksRes.body()?.data ?: emptyList()
                        val exams = examsRes.body()?.data ?: emptyList()
                        val subjects = subjectsRes.body()?.data ?: emptyList()

                        // Group marks by Subject
                        val grouped = subjects.mapNotNull { subject ->
                            // Find all exams for this subject
                            val subjectExams = exams.filter { it.subjectId == subject.subjectId }

                            // Find marks for those exams
                            val subjectMarks = subjectExams.mapNotNull { exam ->
                                val mark = rawMarks.find { it.examinationId == exam.examinationId }
                                if (mark != null) {
                                    MarkItem(
                                        examName = exam.examName,
                                        obtained = mark.marksObtained,
                                        maxMarks = exam.maxMarks,
                                        grade = mark.grade,
                                        remarks = mark.remarks
                                    )
                                } else null
                            }

                            if (subjectMarks.isNotEmpty()) {
                                SubjectGradeGroup(subject.subjectName, subjectMarks)
                            } else null
                        }

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                groupedGrades = grouped
                            )
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = "No active enrollment found.") }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load profile data.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Network error while loading grades.") }
            }
        }
    }

    companion object {
        fun provideFactory(apiService: ApiService): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return StudentGradesViewModel(apiService) as T
                }
            }
    }
}