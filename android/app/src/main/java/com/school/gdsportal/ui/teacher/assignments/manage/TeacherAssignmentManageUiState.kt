package com.school.gdsportal.ui.teacher.assignments.manage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.local.TokenManager
import com.school.gdsportal.data.remote.Assignment
import com.school.gdsportal.data.remote.AssignmentStatus
import com.school.gdsportal.data.remote.TeacherClassDTO
import com.school.gdsportal.data.remote.TeacherSubjectDTO
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TeacherAssignmentManageUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val isEditMode: Boolean = false, // Tracks if we are updating an existing record
    val assignedClasses: List<TeacherClassDTO> = emptyList(),
    val assignedSubjects: List<TeacherSubjectDTO> = emptyList(),

    val selectedSectionId: Int? = null,
    val selectedSubjectId: Int? = null,
    val title: String = "",
    val description: String = "",
    val maxMarks: String = "",
    val deadline: String = "" // format YYYY-MM-DD
)

class TeacherAssignmentManageViewModel(
    private val assignmentId: Long?, // Nullable for Create mode
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherAssignmentManageUiState(isEditMode = assignmentId != null && assignmentId != 0L))
    val uiState: StateFlow<TeacherAssignmentManageUiState> = _uiState.asStateFlow()

    private var currentTeacherId: Long = 0L

    init {
        loadData()
    }

    private fun loadData() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val teacherRes = apiService.getTeacherMe()
                val currentYearRes = apiService.getCurrentAcademicYear()

                val teacherId = teacherRes.body()?.data?.teacherId ?: 0L
                val currentYear = currentYearRes.body()?.data

                if (teacherId == 0L || currentYear == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Unable to identify teacher or academic year.") }
                    return@launch
                }

                currentTeacherId = teacherId

                // Fetch classes and subjects concurrently
                val classesDef = async { apiService.getTeacherClasses(teacherId, currentYear.academicYearId) }
                val subjectsDef = async { apiService.getTeacherSubjects(teacherId, currentYear.academicYearId) }

                val classesRes = classesDef.await()
                val subjectsRes = subjectsDef.await()

                val classes = if (classesRes.isSuccessful) classesRes.body()?.data ?: emptyList() else emptyList()
                val subjects = if (subjectsRes.isSuccessful) subjectsRes.body()?.data ?: emptyList() else emptyList()

                // PRE-FILL FORM FOR EDIT MODE
                if (_uiState.value.isEditMode) {
                    val assignRes = apiService.getAssignmentById(assignmentId!!)
                    if (assignRes.isSuccessful) {
                        val assign = assignRes.body()?.data
                        if (assign != null) {
                            val formattedDeadline = assign.deadline?.substringBefore("T") ?: ""
                            _uiState.update {
                                it.copy(
                                    selectedSectionId = assign.sectionId,
                                    selectedSubjectId = assign.subjectId,
                                    title = assign.title,
                                    description = assign.description ?: "",
                                    maxMarks = assign.maxMarks.toString(),
                                    deadline = formattedDeadline
                                )
                            }
                        }
                    }
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        assignedClasses = classes,
                        assignedSubjects = subjects
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Failed to load data.") }
            }
        }
    }

    fun onSectionSelected(sectionId: Int) {
        _uiState.update { it.copy(selectedSectionId = sectionId, selectedSubjectId = null) }
    }

    fun onSubjectSelected(subjectId: Int) {
        _uiState.update { it.copy(selectedSubjectId = subjectId) }
    }

    fun onTitleChanged(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun onDescriptionChanged(desc: String) {
        _uiState.update { it.copy(description = desc) }
    }

    fun onMaxMarksChanged(marks: String) {
        _uiState.update { it.copy(maxMarks = marks) }
    }

    fun onDeadlineChanged(deadline: String) {
        _uiState.update { it.copy(deadline = deadline) }
    }

    fun submitAssignment(status: AssignmentStatus) {
        val state = _uiState.value
        if (state.selectedSectionId == null || state.selectedSubjectId == null || state.title.isBlank() || state.maxMarks.isBlank()) {
            _uiState.update { it.copy(error = "Please fill all required fields.") }
            return
        }

        _uiState.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            try {
                val marks = state.maxMarks.toDoubleOrNull() ?: 100.0

                val assignment = Assignment(
                    assignmentId = assignmentId ?: 0L,
                    teacherId = currentTeacherId,
                    subjectId = state.selectedSubjectId,
                    sectionId = state.selectedSectionId,
                    title = state.title,
                    description = state.description.takeIf { it.isNotBlank() },
                    maxMarks = marks,
                    deadline = state.deadline.takeIf { it.isNotBlank() }?.let { "${it}T23:59:59" },
                    status = status
                )

                val response = if (state.isEditMode) {
                    apiService.updateAssignment(assignmentId!!, assignment)
                } else {
                    apiService.createAssignment(assignment)
                }

                if (response.isSuccessful) {
                    _uiState.update { it.copy(isSaving = false, success = true) }
                } else {
                    _uiState.update { it.copy(isSaving = false, error = "Failed to save assignment.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = "Network error while saving.") }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    companion object {
        fun provideFactory(assignmentId: Long?, apiService: ApiService, tokenManager: TokenManager): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TeacherAssignmentManageViewModel(assignmentId, apiService, tokenManager) as T
                }
            }
    }
}