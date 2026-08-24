package com.school.gdsportal.ui.teacher.marks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.local.TokenManager
import com.school.gdsportal.data.remote.*
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TeacherMarksUiState(
    val isLoading: Boolean = true,
    val error: String? = null,

    val currentYearId: Int? = null,
    val teacherClasses: List<TeacherClassDTO> = emptyList(),
    val teacherSubjects: List<TeacherSubjectDTO> = emptyList(),
    val examinations: List<Examination> = emptyList(),

    val selectedSectionId: Int? = null,
    val selectedExaminationId: Long? = null,

    val marks: List<MarkDisplay> = emptyList()
)

class TeacherMarksViewModel(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherMarksUiState())
    val uiState: StateFlow<TeacherMarksUiState> = _uiState.asStateFlow()

    private var currentTeacherId: Long = 0L
    private var currentUserId: Long = 0L

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // We run these calls in parallel for speed
                val teacherDef = async { apiService.getTeacherMe() }
                val userDef = async { apiService.getCurrentUser() }
                val currentYearDef = async { apiService.getAcademicYears() }
                
                val teacherRes = teacherDef.await()
                val userRes = userDef.await()
                val currentYearRes = currentYearDef.await()

                if (teacherRes.isSuccessful && userRes.isSuccessful && currentYearRes.isSuccessful) {
                    val teacher = teacherRes.body()?.data
                    val user = userRes.body()?.data
                    val currentYear = currentYearRes.body()?.data?.firstOrNull { it.isCurrent }
                        ?: currentYearRes.body()?.data?.lastOrNull() // Fallback if no current year

                    if (teacher != null && user != null && currentYear != null) {
                        currentTeacherId = teacher.teacherId
                        currentUserId = user.userId

                        val classesRes = apiService.getTeacherClasses(teacher.teacherId, currentYear.academicYearId)
                        val subjectsRes = apiService.getTeacherSubjects(teacher.teacherId, currentYear.academicYearId)

                        val classes = classesRes.body()?.data ?: emptyList()
                        val subjects = subjectsRes.body()?.data ?: emptyList()

                        _uiState.update {
                            it.copy(
                                currentYearId = currentYear.academicYearId,
                                teacherClasses = classes,
                                teacherSubjects = subjects,
                                isLoading = false
                            )
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = "Failed to load user or teacher context.") }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load initial data.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Network error: ${e.message}") }
            }
        }
    }

    fun selectSection(sectionId: Int) {
        if (_uiState.value.selectedSectionId == sectionId) return
        _uiState.update {
            it.copy(
                selectedSectionId = sectionId,
                selectedExaminationId = null,
                examinations = emptyList(),
                marks = emptyList()
            )
        }
        loadExaminations()
    }

    fun selectExamination(examinationId: Long) {
        if (_uiState.value.selectedExaminationId == examinationId) return
        _uiState.update {
            it.copy(
                selectedExaminationId = examinationId,
                marks = emptyList()
            )
        }
        loadMarks()
    }

    private fun loadExaminations() {
        val sectionId = _uiState.value.selectedSectionId
        val yearId = _uiState.value.currentYearId
        if (sectionId == null || yearId == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val res = apiService.getExaminationsBySection(sectionId, yearId)
                if (res.isSuccessful) {
                    val rawExams = res.body()?.data ?: emptyList()

                    val teacherSubjectsForSection = _uiState.value.teacherSubjects
                        .filter { it.sectionId == sectionId }
                        .map { it.subjectId }

                    val filteredExams = rawExams.filter { exam ->
                        exam.createdBy == currentUserId || teacherSubjectsForSection.contains(exam.subjectId)
                    }

                    _uiState.update { it.copy(examinations = filteredExams, isLoading = false) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load examinations") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Network error.") }
            }
        }
    }

    private fun loadMarks() {
        val examinationId = _uiState.value.selectedExaminationId ?: return
        val sectionId = _uiState.value.selectedSectionId ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val studentsDeferred = async { apiService.getStudentsDirectory(null, null, null, sectionId, true, 0, 1000) }
                val marksDeferred = async { apiService.getMarksByExamination(examinationId) }

                val studentsRes = studentsDeferred.await()
                val marksRes = marksDeferred.await()

                if (studentsRes.isSuccessful && marksRes.isSuccessful) {
                    val students = studentsRes.body()?.data?.content ?: emptyList()
                    val rawMarks = marksRes.body()?.data ?: emptyList()

                    val displays = students.map { student ->
                        val mark = rawMarks.find { it.studentId == student.studentId }
                        mapStudentAndMarkToDisplay(student, mark, examinationId)
                    }

                    _uiState.update { it.copy(marks = displays, isLoading = false) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load marks or students") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Network error: ${e.message}") }
            }
        }
    }

    private fun mapStudentAndMarkToDisplay(student: com.school.gdsportal.data.remote.dto.StudentDirectoryDTO, mark: Mark?, examinationId: Long): MarkDisplay {
        val studentName = "${student.firstName} ${student.lastName}"
        val regNo = student.registrationNumber ?: ""

        val examination = _uiState.value.examinations.find { it.examinationId == examinationId }
        val examName = examination?.examName ?: "Examination #$examinationId"
        val maxMarks = examination?.maxMarks?.toString() ?: ""

        val marksText = if (mark?.marksObtained != null) {
            if (maxMarks.isNotEmpty()) "${mark.marksObtained} / $maxMarks" else mark.marksObtained.toString()
        } else {
            "—"
        }

        return MarkDisplay(
            markId = mark?.markId ?: 0L,
            studentId = student.studentId,
            studentName = studentName,
            registrationNumber = regNo,
            examinationName = examName,
            marksObtained = marksText,
            grade = mark?.grade ?: "",
            remarks = mark?.remarks ?: ""
        )
    }

    fun updateMark(markId: Long, studentId: Long, examinationId: Long, obtained: Double?, grade: String, remarks: String) {
        viewModelScope.launch {
            try {
                val mark = Mark(
                    markId = markId,
                    studentId = studentId,
                    examinationId = examinationId,
                    marksObtained = obtained,
                    grade = grade.takeIf { it.isNotBlank() },
                    remarks = remarks.takeIf { it.isNotBlank() },
                    enteredBy = currentTeacherId,
                    enteredAt = null
                )

                // FIX: Now uses the clean single-insert API endpoint!
                val res = if (markId == 0L) {
                    apiService.createMark(mark)
                } else {
                    apiService.updateMark(markId, mark)
                }

                if (res.isSuccessful) {
                    loadMarks()
                } else {
                    val errorBody = res.errorBody()?.string() ?: "Failed to save mark"
                    _uiState.update { it.copy(error = "Error: $errorBody") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Network error while saving mark") }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    companion object {
        fun provideFactory(apiService: ApiService, tokenManager: TokenManager): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TeacherMarksViewModel(apiService, tokenManager) as T
                }
            }
    }
}