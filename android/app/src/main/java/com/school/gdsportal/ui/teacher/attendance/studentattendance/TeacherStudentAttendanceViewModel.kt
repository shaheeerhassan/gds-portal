package com.school.gdsportal.ui.teacher.attendance.studentattendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.local.TokenManager
import com.school.gdsportal.data.remote.StudentAttendance
import com.school.gdsportal.data.remote.TeacherClassDTO
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

import com.school.gdsportal.data.remote.StudentAttendanceDisplay
import com.school.gdsportal.data.remote.StudentAttendanceStatus
import com.school.gdsportal.data.remote.Enrollment
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

data class TeacherStudentAttendanceUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccessMessage: String? = null,
    val assignedClasses: List<TeacherClassDTO> = emptyList(), // Now only contains Class Teacher sections!
    val selectedSection: TeacherClassDTO? = null,
    val selectedDate: String = "",
    val displayRecords: List<StudentAttendanceDisplay> = emptyList(),
    val enrollments: List<Enrollment> = emptyList()
)

class TeacherStudentAttendanceViewModel(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherStudentAttendanceUiState())
    val uiState: StateFlow<TeacherStudentAttendanceUiState> = _uiState.asStateFlow()

    private var currentAcademicYearId: Int = 0
    private var currentTeacherId: Long = 0L

    init {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
        _uiState.update { it.copy(selectedDate = today) }
        loadClassTeacherSections()
    }

    private fun loadClassTeacherSections() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val teacherId = apiService.getTeacherMe().body()?.data?.teacherId ?: 0L
                val yearResponse = apiService.getCurrentAcademicYear()
                val currentYear = yearResponse.body()?.data

                if (teacherId != 0L && currentYear != null) {
                    currentTeacherId = teacherId
                    currentAcademicYearId = currentYear.academicYearId

                    // 1. Fetch official Class Teacher History
                    val historyRes = apiService.getClassTeacherHistory(teacherId)
                    val history = historyRes.body()?.data ?: emptyList()

                    // 2. Filter for ACTIVE assignments in the current year
                    val activeSectionIds = history
                        .filter { it.isActive && it.academicYearId == currentAcademicYearId }
                        .map { it.sectionId }

                    if (activeSectionIds.isEmpty()) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                assignedClasses = emptyList(),
                                error = "You are not assigned as an active Class Teacher for any section."
                            )
                        }
                        return@launch
                    }

                    // 3. Fetch Classes & Sections to resolve names
                    val classesRes = apiService.getClasses()
                    val sectionsRes = apiService.getAllSections()

                    val allClasses = classesRes.body()?.data ?: emptyList()
                    val allSections = sectionsRes.body()?.data ?: emptyList()

                    // 4. Map the IDs to beautiful display DTOs
                    val classTeacherSections = activeSectionIds.mapNotNull { sectionId ->
                        val section = allSections.find { it.sectionId == sectionId }
                        val schoolClass = allClasses.find { it.classId == section?.classId }

                        if (section != null && schoolClass != null) {
                            TeacherClassDTO(
                                teacherClassId = 0L,
                                teacherId = teacherId,
                                classId = schoolClass.classId,
                                className = schoolClass.className,
                                sectionId = section.sectionId,
                                sectionName = section.sectionName,
                                academicYearId = currentAcademicYearId
                            )
                        } else null
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            assignedClasses = classTeacherSections,
                            selectedSection = classTeacherSections.firstOrNull()
                        )
                    }
                    classTeacherSections.firstOrNull()?.let { loadAttendanceRecords(it.sectionId, _uiState.value.selectedDate) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Authentication or Academic Year error.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Network error.") }
            }
        }
    }

    fun onSectionSelected(section: TeacherClassDTO) {
        _uiState.update { it.copy(selectedSection = section, saveSuccessMessage = null) }
        loadAttendanceRecords(section.sectionId, _uiState.value.selectedDate)
    }

    fun onDateSelected(date: String) {
        _uiState.update { it.copy(selectedDate = date, saveSuccessMessage = null) }
        _uiState.value.selectedSection?.let { loadAttendanceRecords(it.sectionId, date) }
    }

    private fun loadAttendanceRecords(sectionId: Int, date: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val studentsDeferred = async { apiService.getStudentsDirectory(null, null, null, sectionId, true, 0, 1000) }
                val enrollmentsDeferred = async { apiService.getEnrollmentsBySection(sectionId, currentAcademicYearId) }
                val attendanceDeferred = async { apiService.getStudentAttendanceBySectionAndDate(sectionId, date) }

                val studentsRes = studentsDeferred.await()
                val enrollmentsRes = enrollmentsDeferred.await()
                val attendanceRes = attendanceDeferred.await()

                if (studentsRes.isSuccessful && enrollmentsRes.isSuccessful) {
                    val students = studentsRes.body()?.data?.content ?: emptyList()
                    val enrollments = enrollmentsRes.body()?.data ?: emptyList()
                    val existingAttendance = attendanceRes.body()?.data ?: emptyList()

                    val displayRecords = students.mapNotNull { student ->
                        val enrollment = enrollments.find { it.studentId == student.studentId }
                        if (enrollment != null) {
                            val existing = existingAttendance.find { it.studentClassId == enrollment.studentClassId }
                            StudentAttendanceDisplay(
                                attendanceId = existing?.attendanceId,
                                studentId = student.studentId,
                                studentName = "${student.firstName} ${student.lastName}",
                                registrationNumber = student.registrationNumber,
                                status = existing?.status,
                                remarks = existing?.remarks ?: ""
                            )
                        } else null
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            displayRecords = displayRecords,
                            enrollments = enrollments
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load students and enrollments.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Network error while fetching attendance.") }
            }
        }
    }

    fun updateLocalAttendanceStatus(studentId: Long, newStatusString: String) {
        val currentRecords = _uiState.value.displayRecords.toMutableList()
        val index = currentRecords.indexOfFirst { it.studentId == studentId }
        if (index != -1) {
            val record = currentRecords[index]
            currentRecords[index] = record.copy(status = StudentAttendanceStatus.valueOf(newStatusString))
            _uiState.update { it.copy(displayRecords = currentRecords) }
        }
    }

    fun markAllPresent() {
        val updatedRecords = _uiState.value.displayRecords.map {
            if (it.status == null) it.copy(status = StudentAttendanceStatus.PRESENT) else it
        }
        _uiState.update { it.copy(displayRecords = updatedRecords) }
    }

    fun saveAttendance() {
        val state = _uiState.value
        val enrollments = state.enrollments

        _uiState.update { it.copy(isSaving = true, error = null, saveSuccessMessage = null) }
        viewModelScope.launch {
            try {
                val newRecords = mutableListOf<StudentAttendance>()
                val updateCalls = mutableListOf<kotlinx.coroutines.Deferred<*>>()

                state.displayRecords.forEach { display ->
                    val status = display.status ?: return@forEach
                    val enrollment = enrollments.find { it.studentId == display.studentId } ?: return@forEach

                    if (display.attendanceId == null) {
                        // Needs to be POSTed
                        newRecords.add(
                            StudentAttendance(
                                studentClassId = enrollment.studentClassId,
                                attendanceDate = state.selectedDate,
                                status = status,
                                periodId = 1,
                                markedBy = currentTeacherId,
                                markedAt = null,
                                isLocked = false,
                                remarks = display.remarks
                            )
                        )
                    } else {
                        // Existing record, needs a PUT status update
                        updateCalls.add(
                            async {
                                apiService.updateStudentAttendanceStatus(
                                    display.attendanceId,
                                    com.school.gdsportal.data.remote.StatusRequest(status.name)
                                )
                            }
                        )
                    }
                }

                var hasError = false

                // Execute batch insert for new records
                if (newRecords.isNotEmpty()) {
                    val request = com.school.gdsportal.data.remote.MarkStudentAttendanceRequest(records = newRecords)
                    val response = apiService.markStudentAttendance(request)
                    if (!response.isSuccessful) hasError = true
                }

                // Execute parallel updates for existing records
                if (updateCalls.isNotEmpty()) {
                    updateCalls.awaitAll()
                }

                if (hasError) {
                    _uiState.update { it.copy(isSaving = false, error = "Some records failed to save. Ensure no duplicate dates.") }
                } else {
                    _uiState.update { it.copy(isSaving = false, saveSuccessMessage = "Attendance saved successfully!") }
                    // Reload to bind the new database IDs to the UI models
                    state.selectedSection?.let { loadAttendanceRecords(it.sectionId, state.selectedDate) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = "Network error while saving attendance.") }
            }
        }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(saveSuccessMessage = null) }
    }

    companion object {
        fun provideFactory(apiService: ApiService, tokenManager: TokenManager): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TeacherStudentAttendanceViewModel(apiService, tokenManager) as T
                }
            }
    }
}