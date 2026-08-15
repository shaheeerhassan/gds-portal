package com.school.gdsportal.ui.admin.assessment.submissions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.*
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SubmissionsDirectoryUiState(
    val isLoading: Boolean = true,
    val error: String? = null,

    val academicYears: List<AcademicYear> = emptyList(),
    val classes: List<SchoolClass> = emptyList(),
    val sections: List<Section> = emptyList(),
    val assignments: List<AssignmentDisplay> = emptyList(),

    val selectedAcademicYearId: Int? = null,
    val selectedClassId: Int? = null,
    val selectedSectionId: Int? = null,
    val selectedAssignmentId: Long? = null,

    val submissions: List<SubmissionDisplay> = emptyList()
)

class SubmissionsDirectoryViewModel(private val apiService: ApiService) : ViewModel() {

    private val _uiState = MutableStateFlow(SubmissionsDirectoryUiState())
    val uiState: StateFlow<SubmissionsDirectoryUiState> = _uiState.asStateFlow()

    private var allSubjects = emptyList<Subject>()
    private var allTeachers = emptyList<Teacher>()
    private var allClasses = emptyList<SchoolClass>()
    private var allSections = emptyList<Section>()

    // Cache students that have been resolved already to avoid re-fetching
    private val studentCache = mutableMapOf<Long, Student>()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                coroutineScope {
                    val yearsDef = async { apiService.getAcademicYears() }
                    val classesDef = async { apiService.getClasses() }
                    val subjectsDef = async { apiService.getSubjects() }
                    val teachersDef = async { apiService.getAllTeachers() }

                    val years = yearsDef.await().body()?.data ?: emptyList()
                    val classes = classesDef.await().body()?.data ?: emptyList()
                    val subjects = subjectsDef.await().body()?.data ?: emptyList()
                    val teachers = teachersDef.await().body()?.data ?: emptyList()

                    allClasses = classes
                    allSubjects = subjects
                    allTeachers = teachers

                    val defaultYearId = years.firstOrNull { it.isCurrent }?.academicYearId
                        ?: years.firstOrNull()?.academicYearId

                    _uiState.value = _uiState.value.copy(
                        academicYears = years,
                        classes = classes,
                        selectedAcademicYearId = defaultYearId,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to load initial data: ${e.message}")
            }
        }
    }

    fun selectAcademicYear(yearId: Int) {
        if (_uiState.value.selectedAcademicYearId == yearId) return
        _uiState.value = _uiState.value.copy(
            selectedAcademicYearId = yearId,
            selectedSectionId = null,
            selectedAssignmentId = null,
            sections = emptyList(),
            assignments = emptyList(),
            submissions = emptyList()
        )
        loadSectionsIfPossible()
    }

    fun selectClass(classId: Int) {
        if (_uiState.value.selectedClassId == classId) return
        _uiState.value = _uiState.value.copy(
            selectedClassId = classId,
            selectedSectionId = null,
            selectedAssignmentId = null,
            sections = emptyList(),
            assignments = emptyList(),
            submissions = emptyList()
        )
        loadSectionsIfPossible()
    }

    fun selectSection(sectionId: Int) {
        if (_uiState.value.selectedSectionId == sectionId) return
        _uiState.value = _uiState.value.copy(
            selectedSectionId = sectionId,
            selectedAssignmentId = null,
            assignments = emptyList(),
            submissions = emptyList()
        )
        loadAssignments()
    }

    fun selectAssignment(assignmentId: Long) {
        if (_uiState.value.selectedAssignmentId == assignmentId) return
        _uiState.value = _uiState.value.copy(
            selectedAssignmentId = assignmentId,
            submissions = emptyList()
        )
        loadSubmissions()
    }

    private fun loadSectionsIfPossible() {
        val classId = _uiState.value.selectedClassId
        val yearId = _uiState.value.selectedAcademicYearId
        if (classId != null && yearId != null) {
            viewModelScope.launch {
                try {
                    val res = apiService.getSections(classId, yearId)
                    if (res.isSuccessful) {
                        val sections = res.body()?.data ?: emptyList()
                        allSections = sections
                        _uiState.value = _uiState.value.copy(sections = sections)
                    }
                } catch (_: Exception) { }
            }
        }
    }

    private fun loadAssignments() {
        val sectionId = _uiState.value.selectedSectionId
        val yearId = _uiState.value.selectedAcademicYearId
        if (sectionId == null || yearId == null) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val res = apiService.getAssignmentsBySection(sectionId, yearId)
                if (res.isSuccessful) {
                    val rawAssignments = res.body()?.data ?: emptyList()
                    val displayList = rawAssignments.map { mapAssignmentToDisplay(it) }
                    _uiState.value = _uiState.value.copy(assignments = displayList, isLoading = false)
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to load assignments")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Network error: ${e.message}")
            }
        }
    }

    private fun loadSubmissions() {
        val assignmentId = _uiState.value.selectedAssignmentId ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val res = apiService.getSubmissionsByAssignment(assignmentId)
                if (res.isSuccessful) {
                    val rawSubmissions = res.body()?.data ?: emptyList()

                    // Resolve student names concurrently
                    val displays = coroutineScope {
                        rawSubmissions.map { submission ->
                            async { mapSubmissionToDisplay(submission) }
                        }.map { it.await() }
                    }

                    _uiState.value = _uiState.value.copy(submissions = displays, isLoading = false)
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to load submissions")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Network error: ${e.message}")
            }
        }
    }

    private suspend fun mapSubmissionToDisplay(submission: Submission): SubmissionDisplay {
        val student = resolveStudent(submission.studentId)
        val studentName = if (student != null) "${student.firstName} ${student.lastName}" else "Unknown Student"
        val regNo = student?.registrationNumber ?: ""

        val assignment = _uiState.value.assignments.find { it.assignmentId == submission.assignmentId }
        val assignmentTitle = assignment?.title ?: "Assignment #${submission.assignmentId}"

        return SubmissionDisplay(
            submissionId = submission.submissionId,
            studentName = studentName,
            registrationNumber = regNo,
            assignmentTitle = assignmentTitle,
            submittedAt = submission.submittedAt?.let {
                it.replace("T", " ").take(16)
            } ?: "Unknown",
            status = submission.status,
            marksAwarded = submission.marksAwarded?.toString() ?: "—",
            feedback = submission.feedback ?: "",
            fileUrl = submission.fileUrl ?: ""
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

    private fun mapAssignmentToDisplay(assignment: Assignment): AssignmentDisplay {
        val subjectName = allSubjects.find { it.subjectId == assignment.subjectId }?.subjectName ?: "Unknown Subject"
        val section = allSections.find { it.sectionId == assignment.sectionId }
        val sectionName = section?.sectionName ?: "Unknown Section"
        val className = section?.let { sec -> allClasses.find { it.classId == sec.classId }?.className } ?: "Unknown Class"
        val teacher = allTeachers.find { it.teacherId == assignment.teacherId }
        val teacherName = if (teacher != null) "${teacher.firstName} ${teacher.lastName}" else "Unknown Teacher"

        return AssignmentDisplay(
            assignmentId = assignment.assignmentId,
            title = assignment.title,
            subjectName = subjectName,
            sectionName = "$className $sectionName",
            teacherName = teacherName,
            deadline = assignment.deadline?.let { it.split("T").firstOrNull() ?: it } ?: "No Deadline",
            status = assignment.status,
            maxMarks = assignment.maxMarks.toString(),
            description = assignment.description ?: ""
        )
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    class Factory(private val apiService: ApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SubmissionsDirectoryViewModel(apiService) as T
        }
    }
}
