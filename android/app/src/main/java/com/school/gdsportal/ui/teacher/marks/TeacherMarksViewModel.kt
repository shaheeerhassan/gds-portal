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

    private var currentUserId: Long = 0L
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
                val teacherRes = apiService.getTeacherMe()
                if (teacherRes.isSuccessful && teacherRes.body()?.data != null) {
                    val teacher = teacherRes.body()!!.data!!
                    currentUserId = teacher.teacherId
                    val classesRes = apiService.getTeacherClasses(teacherId, currentYear!!.academicYearId)
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
                    val filteredExams = rawExams.filter { it.createdBy == currentUserId }
                    _uiState.value = _uiState.value.copy(examinations = filteredExams, isLoading = false)
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
        val sectionId = _uiState.value.selectedSectionId ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // Fetch students in section and existing marks in parallel
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

                    _uiState.value = _uiState.value.copy(marks = displays, isLoading = false)
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to load marks or students")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Network error: ${e.message}")
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

    // A separate class to hold the raw mark data so we can update it
    data class StudentMarkData(val student: Student, val mark: Mark?)

    fun updateMark(markId: Long, studentId: Long, examinationId: Long, obtained: Double, grade: String, remarks: String) {
        viewModelScope.launch {
            try {
                val mark = Mark(
                    markId = markId,
                    studentId = studentId,
                    examinationId = examinationId,
                    marksObtained = obtained,
                    grade = grade,
                    remarks = remarks,
                    enteredBy = currentUserId,
                    enteredAt = null
                )
                val res = if (markId == 0L) {
                    apiService.enterMarks(com.school.gdsportal.data.remote.EnterMarksRequest(listOf(mark)))
                } else {
                    apiService.updateMark(markId, mark)
                }
                
                if (res.isSuccessful) {
                    // Reload marks to refresh UI
                    loadMarks()
                } else {
                    val errorBody = res.errorBody()?.string() ?: "Failed to save mark"
                    _uiState.value = _uiState.value.copy(error = "Error: $errorBody")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Network error while saving mark")
            }
        }
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
