package com.school.gdsportal.ui.teacher.marks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.local.TokenManager
import com.school.gdsportal.data.remote.*
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TeacherMarksUiState(
    val isLoading: Boolean = true,
    val error: String? = null,

    val currentYearId: Int? = null,
    val teacherClasses: List<TeacherClassDTO> = emptyList(),
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

    private val studentCache = mutableMapOf<Long, Student>()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val teacherId = apiService.getTeacherMe().body()?.data?.teacherId ?: 0L
                val currentYear = apiService.getCurrentAcademicYear().body()?.data

                if (teacherId != 0L && currentYear != null) {
                    val classesRes = apiService.getTeacherClasses(teacherId, currentYear.academicYearId)
                    val classes = classesRes.body()?.data ?: emptyList()
                    _uiState.value = _uiState.value.copy(
                        currentYearId = currentYear.academicYearId,
                        teacherClasses = classes,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to load teacher context.")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Network error: ${e.message}")
            }
        }
    }

    fun selectSection(sectionId: Int) {
        if (_uiState.value.selectedSectionId == sectionId) return
        _uiState.value = _uiState.value.copy(
            selectedSectionId = sectionId,
            selectedExaminationId = null,
            examinations = emptyList(),
            marks = emptyList()
        )
        loadExaminations()
    }

    fun selectExamination(examinationId: Long) {
        if (_uiState.value.selectedExaminationId == examinationId) return
        _uiState.value = _uiState.value.copy(
            selectedExaminationId = examinationId,
            marks = emptyList()
        )
        loadMarks()
    }

    private fun loadExaminations() {
        val sectionId = _uiState.value.selectedSectionId
        val yearId = _uiState.value.currentYearId
        if (sectionId == null || yearId == null) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val res = apiService.getExaminationsBySection(sectionId, yearId)
                if (res.isSuccessful) {
                    val rawExams = res.body()?.data ?: emptyList()
                    _uiState.value = _uiState.value.copy(examinations = rawExams, isLoading = false)
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to load examinations")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Network error.")
            }
        }
    }

    private fun loadMarks() {
        val examinationId = _uiState.value.selectedExaminationId ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val res = apiService.getMarksByExamination(examinationId)
                if (res.isSuccessful) {
                    val rawMarks = res.body()?.data ?: emptyList()

                    val displays = coroutineScope {
                        rawMarks.map { mark ->
                            async { mapMarkToDisplay(mark) }
                        }.map { it.await() }
                    }

                    _uiState.value = _uiState.value.copy(marks = displays, isLoading = false)
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to load marks")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Network error.")
            }
        }
    }

    private suspend fun mapMarkToDisplay(mark: Mark): MarkDisplay {
        val student = resolveStudent(mark.studentId)
        val studentName = if (student != null) "${student.firstName} ${student.lastName}" else "Unknown Student"
        val regNo = student?.registrationNumber ?: ""

        val examination = _uiState.value.examinations.find { it.examinationId == mark.examinationId }
        val examName = examination?.examName ?: "Examination #${mark.examinationId}"
        val maxMarks = examination?.maxMarks?.toString() ?: ""

        val marksText = if (mark.marksObtained != null) {
            if (maxMarks.isNotEmpty()) "${mark.marksObtained} / $maxMarks" else mark.marksObtained.toString()
        } else {
            "—"
        }

        return MarkDisplay(
            markId = mark.markId,
            studentName = studentName,
            registrationNumber = regNo,
            examinationName = examName,
            marksObtained = marksText,
            grade = mark.grade ?: "",
            remarks = mark.remarks ?: ""
        )
    }

    private suspend fun resolveStudent(studentId: Long): Student? {
        studentCache[studentId]?.let { return it }
        return try {
            val res = apiService.getStudent(studentId)
            if (res.isSuccessful) {
                val student = res.body()?.data
                if (student != null) studentCache[studentId] = student
                student
            } else null
        } catch (_: Exception) { null }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
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
