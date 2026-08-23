package com.school.gdsportal.ui.admin.assessment.examinations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.AcademicYear
import com.school.gdsportal.data.remote.Examination
import com.school.gdsportal.data.remote.ExaminationStatus
import com.school.gdsportal.data.remote.SchoolClass
import com.school.gdsportal.data.remote.Section
import com.school.gdsportal.data.remote.Subject
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ExaminationDisplay(
    val examinationId: Long,
    val examName: String,
    val subjectId: Int,
    val subjectName: String,
    val sectionId: Int,
    val academicYearId: Int,
    val examDate: String,
    val startTime: String,
    val endTime: String,
    val maxMarks: Double,
    val passingMarks: Double?,
    val status: ExaminationStatus
)

data class ExaminationsDirectoryUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    
    val academicYears: List<AcademicYear> = emptyList(),
    val classes: List<SchoolClass> = emptyList(),
    val sections: List<Section> = emptyList(),
    
    val selectedAcademicYearId: Int? = null,
    val selectedClassId: Int? = null,
    val selectedSectionId: Int? = null,
    
    val examinations: List<ExaminationDisplay> = emptyList()
)

class ExaminationsDirectoryViewModel(private val apiService: ApiService) : ViewModel() {

    private val _uiState = MutableStateFlow(ExaminationsDirectoryUiState())
    val uiState: StateFlow<ExaminationsDirectoryUiState> = _uiState.asStateFlow()

    private var subjectsMap: Map<Int, Subject> = emptyMap()

    init {
        loadInitialData()
    }

    fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                coroutineScope {
                    val yearsDef = async { apiService.getAcademicYears() }
                    val classesDef = async { apiService.getClasses() }
                    val subjectsDef = async { apiService.getSubjects() }
                    
                    val years = yearsDef.await().body()?.data ?: emptyList()
                    val classes = classesDef.await().body()?.data ?: emptyList()
                    val subjects = subjectsDef.await().body()?.data ?: emptyList()
                    
                    subjectsMap = subjects.associateBy { it.subjectId }
                    
                    val activeYearId = years.firstOrNull { it.isCurrent }?.academicYearId 
                                       ?: years.firstOrNull()?.academicYearId

                    _uiState.value = _uiState.value.copy(
                        academicYears = years,
                        classes = classes,
                        selectedAcademicYearId = activeYearId,
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
            sections = emptyList(),
            examinations = emptyList()
        )
        loadSectionsIfPossible()
    }

    fun selectClass(classId: Int) {
        if (_uiState.value.selectedClassId == classId) return
        _uiState.value = _uiState.value.copy(
            selectedClassId = classId,
            selectedSectionId = null,
            sections = emptyList(),
            examinations = emptyList()
        )
        loadSectionsIfPossible()
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
                        _uiState.value = _uiState.value.copy(sections = sections)
                    }
                } catch (e: Exception) {
                    // Ignore errors silently for cascading dropdowns
                }
            }
        }
    }

    fun selectSection(sectionId: Int) {
        if (_uiState.value.selectedSectionId == sectionId) return
        _uiState.value = _uiState.value.copy(selectedSectionId = sectionId)
        loadExaminations()
    }

    private fun loadExaminations() {
        val sectionId = _uiState.value.selectedSectionId ?: return
        val yearId = _uiState.value.selectedAcademicYearId ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val res = apiService.getExaminationsBySection(sectionId, yearId)
                if (res.isSuccessful) {
                    val rawExams = res.body()?.data ?: emptyList()
                    val displayExams = rawExams.map { exam ->
                        ExaminationDisplay(
                            examinationId = exam.examinationId,
                            examName = exam.examName,
                            subjectId = exam.subjectId,
                            subjectName = subjectsMap[exam.subjectId]?.subjectName ?: "Unknown Subject",
                            sectionId = exam.sectionId,
                            academicYearId = exam.academicYearId,
                            examDate = exam.examDate.toString(),
                            startTime = exam.startTime.toString(),
                            endTime = exam.endTime.toString(),
                            maxMarks = exam.maxMarks,
                            passingMarks = exam.passingMarks,
                            status = exam.status
                        )
                    }
                    _uiState.value = _uiState.value.copy(isLoading = false, examinations = displayExams)
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to load examinations")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Network error: ${e.message}")
            }
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    class Factory(private val apiService: ApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ExaminationsDirectoryViewModel(apiService) as T
        }
    }
}
