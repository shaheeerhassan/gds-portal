package com.school.gdsportal.ui.teacher.examinations.manage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.local.TokenManager
import com.school.gdsportal.data.remote.Examination
import com.school.gdsportal.data.remote.TeacherSubjectDTO
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

data class TeacherExaminationManageUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val availableSubjects: List<TeacherSubjectDTO> = emptyList(),
    val isEditMode: Boolean = false, // Track if editing existing

    // DB Fields
    val examName: String = "",
    val subjectId: Int? = null,
    val examDate: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val maxMarks: String = "",
    val passingMarks: String = "",
    val status: String = "SCHEDULED"
)

class TeacherExaminationManageViewModel(
    private val sectionId: Int,
    private val examId: Long?, // Pass ID for editing
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherExaminationManageUiState(isEditMode = examId != null && examId != 0L))
    val uiState: StateFlow<TeacherExaminationManageUiState> = _uiState.asStateFlow()

    private var currentAcademicYearId: Int = 0
    private var currentUserId: Long = 0L

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val teacherId = apiService.getTeacherMe().body()?.data?.teacherId ?: 0L
                val currentYear = apiService.getCurrentAcademicYear().body()?.data
                val user = apiService.getCurrentUser().body()?.data

                if (teacherId != 0L && currentYear != null) {
                    currentAcademicYearId = currentYear.academicYearId
                    if (user != null) currentUserId = user.userId
                    val subjectsRes = apiService.getTeacherSubjects(teacherId, currentAcademicYearId)

                    if (subjectsRes.isSuccessful) {
                        val filteredSubjects = subjectsRes.body()?.data?.filter { it.sectionId == sectionId } ?: emptyList()
                        _uiState.update { it.copy(availableSubjects = filteredSubjects) }
                    }
                }

                // If editing, load the existing exam data
                if (_uiState.value.isEditMode) {
                    val examRes = apiService.getExaminationById(examId!!)
                    if (examRes.isSuccessful) {
                        val exam = examRes.body()?.data
                        if (exam != null) {
                            _uiState.update {
                                it.copy(
                                    examName = exam.examName,
                                    subjectId = exam.subjectId,
                                    examDate = exam.examDate,
                                    startTime = exam.startTime ?: "",
                                    endTime = exam.endTime ?: "",
                                    maxMarks = exam.maxMarks.toString(),
                                    passingMarks = exam.passingMarks?.toString() ?: "",
                                    status = exam.status?.name ?: "SCHEDULED"
                                )
                            }
                        }
                    }
                }

                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Failed to load examination data.") }
            }
        }
    }

    fun updateField(
        name: String? = null, date: String? = null, start: String? = null,
        end: String? = null, max: String? = null, pass: String? = null,
        subId: Int? = null, stat: String? = null
    ) {
        _uiState.update {
            it.copy(
                examName = name ?: it.examName,
                examDate = date ?: it.examDate,
                startTime = start ?: it.startTime,
                endTime = end ?: it.endTime,
                maxMarks = max ?: it.maxMarks,
                passingMarks = pass ?: it.passingMarks,
                subjectId = subId ?: it.subjectId,
                status = stat ?: it.status
            )
        }
    }

    fun saveExamination() {
        val state = _uiState.value
        if (state.examName.isBlank() || state.subjectId == null || state.examDate.isBlank() || state.maxMarks.isBlank()) {
            _uiState.update { it.copy(error = "Please fill in all required fields.") }
            return
        }

        _uiState.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            try {
                // examDate is already a string format yyyy-MM-dd
                val exam = Examination(
                    examinationId = examId ?: 0L,
                    examName = state.examName,
                    subjectId = state.subjectId,
                    sectionId = sectionId,
                    academicYearId = currentAcademicYearId,
                    examDate = state.examDate,
                    startTime = state.startTime.takeIf { it.isNotBlank() },
                    endTime = state.endTime.takeIf { it.isNotBlank() },
                    maxMarks = state.maxMarks.toDoubleOrNull() ?: 100.0,
                    passingMarks = state.passingMarks.toDoubleOrNull(),
                    status = com.school.gdsportal.data.remote.ExaminationStatus.valueOf(state.status),
                    createdBy = currentUserId
                )

                val response = if (state.isEditMode) {
                    apiService.updateExamination(examId!!, exam)
                } else {
                    apiService.createExamination(exam)
                }

                if (response.isSuccessful) {
                    _uiState.update { it.copy(isSaving = false, success = true) }
                } else {
                    val errorStr = response.errorBody()?.string()
                    _uiState.update { it.copy(isSaving = false, error = "Failed to save examination: $errorStr") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = "Network error: ${e.message}") }
            }
        }
    }

    companion object {
        fun provideFactory(sectionId: Int, examId: Long?, apiService: ApiService, tokenManager: TokenManager): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TeacherExaminationManageViewModel(sectionId, examId, apiService, tokenManager) as T
                }
            }
    }
}