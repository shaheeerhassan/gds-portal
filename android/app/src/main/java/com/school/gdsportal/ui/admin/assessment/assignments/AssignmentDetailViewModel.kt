package com.school.gdsportal.ui.admin.assessment.assignments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.Assignment
import com.school.gdsportal.data.remote.AssignmentDisplay
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AssignmentDetailUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val assignment: AssignmentDisplay? = null
)

class AssignmentDetailViewModel(
    private val apiService: ApiService,
    private val assignmentId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssignmentDetailUiState())
    val uiState: StateFlow<AssignmentDetailUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                coroutineScope {
                    val assignmentRes = apiService.getAssignmentById(assignmentId)
                    if (assignmentRes.isSuccessful) {
                        val assignment = assignmentRes.body()?.data
                        if (assignment != null) {
                            
                            val subjectDef = async { apiService.getSubjectById(assignment.subjectId) }
                            val teacherDef = async { apiService.getTeacherById(assignment.teacherId) }
                            val sectionDef = async { apiService.getSectionById(assignment.sectionId) }
                            
                            val subject = subjectDef.await().body()?.data
                            val teacher = teacherDef.await().body()?.data
                            val section = sectionDef.await().body()?.data
                            
                            val classDef = if (section != null) async { apiService.getClassById(section.classId) } else null
                            val clazz = classDef?.await()?.body()?.data

                            val display = AssignmentDisplay(
                                assignmentId = assignment.assignmentId,
                                title = assignment.title,
                                subjectName = subject?.subjectName ?: "Unknown Subject",
                                sectionName = "${clazz?.className ?: "Unknown Class"} ${section?.sectionName ?: "Unknown Section"}",
                                teacherName = if (teacher != null) "${teacher.firstName} ${teacher.lastName}" else "Unknown Teacher",
                                deadline = assignment.deadline?.let { it.split("T").firstOrNull() ?: it } ?: "No Deadline",
                                status = assignment.status,
                                maxMarks = assignment.maxMarks.toString(),
                                description = assignment.description ?: ""
                            )
                            _uiState.value = _uiState.value.copy(isLoading = false, assignment = display)
                        } else {
                            _uiState.value = _uiState.value.copy(isLoading = false, error = "Assignment not found")
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to load assignment")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Network error: ${e.message}")
            }
        }
    }

    class Factory(private val apiService: ApiService, private val assignmentId: Long) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AssignmentDetailViewModel(apiService, assignmentId) as T
        }
    }
}
