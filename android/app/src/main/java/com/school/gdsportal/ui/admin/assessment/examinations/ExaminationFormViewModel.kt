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

data class ExaminationFormUiState(
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false,
    val error: String? = null,
    
    val isEditMode: Boolean = false,
    val examinationId: Long? = null,
    
    val academicYears: List<AcademicYear> = emptyList(),
    val classes: List<SchoolClass> = emptyList(),
    val sections: List<Section> = emptyList(),
    val subjects: List<Subject> = emptyList(),
    
    val examName: String = "",
    val maxMarks: String = "",
    val passingMarks: String = "",
    val examDate: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val status: ExaminationStatus = ExaminationStatus.SCHEDULED,
    
    val selectedAcademicYearId: Int? = null,
    val selectedClassId: Int? = null,
    val selectedSectionId: Int? = null,
    val selectedSubjectId: Int? = null
) {
    val isFormValid: Boolean
        get() = examName.isNotBlank() &&
                selectedAcademicYearId != null &&
                selectedSectionId != null &&
                selectedSubjectId != null &&
                examDate.isNotBlank() &&
                startTime.isNotBlank() &&
                endTime.isNotBlank() &&
                maxMarks.toDoubleOrNull() != null
}

class ExaminationFormViewModel(private val apiService: ApiService) : ViewModel() {

    private val _uiState = MutableStateFlow(ExaminationFormUiState())
    val uiState: StateFlow<ExaminationFormUiState> = _uiState.asStateFlow()
    
    private var allSubjects = emptyList<Subject>()

    fun loadInitialData(examinationId: Long?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                coroutineScope {
                    val yearsDef = async { apiService.getAcademicYears() }
                    val classesDef = async { apiService.getClasses() }
                    val subjectsDef = async { apiService.getSubjects() }
                    
                    val years = yearsDef.await().body()?.data ?: emptyList()
                    val classes = classesDef.await().body()?.data ?: emptyList()
                    allSubjects = subjectsDef.await().body()?.data ?: emptyList()
                    
                    if (examinationId != null) {
                        val examRes = apiService.getExaminationById(examinationId)
                        if (examRes.isSuccessful) {
                            val exam = examRes.body()?.data
                            if (exam != null) {
                                // Find classId from sectionId
                                var classIdForExam: Int? = null
                                var examSections: List<Section> = emptyList()
                                for (c in classes) {
                                    try {
                                        val secRes = apiService.getSections(c.classId, exam.academicYearId)
                                        if (secRes.isSuccessful) {
                                            val sections = secRes.body()?.data ?: emptyList()
                                            if (sections.any { it.sectionId == exam.sectionId }) {
                                                classIdForExam = c.classId
                                                examSections = sections
                                                break
                                            }
                                        }
                                    } catch (e: Exception) {
                                        // Ignore
                                    }
                                }

                                _uiState.value = _uiState.value.copy(
                                    academicYears = years,
                                    classes = classes,
                                    sections = examSections,
                                    
                                    isEditMode = true,
                                    examinationId = exam.examinationId,
                                    examName = exam.examName,
                                    maxMarks = exam.maxMarks.toString(),
                                    passingMarks = exam.passingMarks?.toString() ?: "",
                                    examDate = exam.examDate,
                                    startTime = exam.startTime ?: "",
                                    endTime = exam.endTime ?: "",
                                    status = exam.status,
                                    selectedAcademicYearId = exam.academicYearId,
                                    selectedClassId = classIdForExam,
                                    selectedSectionId = exam.sectionId,
                                    selectedSubjectId = exam.subjectId,
                                    isLoading = false
                                )
                                loadSubjectsForSectionIfPossible()
                            }
                        } else {
                            _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to load examination")
                        }
                    } else {
                        val defaultYearId = years.firstOrNull { it.isCurrent }?.academicYearId 
                                            ?: years.firstOrNull()?.academicYearId
                        _uiState.value = _uiState.value.copy(
                            academicYears = years,
                            classes = classes,
                            subjects = emptyList(),
                            selectedAcademicYearId = defaultYearId,
                            isEditMode = false,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Network error: ${e.message}")
            }
        }
    }

    fun onExamNameChange(value: String) { _uiState.value = _uiState.value.copy(examName = value) }
    fun onMaxMarksChange(value: String) { _uiState.value = _uiState.value.copy(maxMarks = value) }
    fun onPassingMarksChange(value: String) { _uiState.value = _uiState.value.copy(passingMarks = value) }
    fun onExamDateChange(value: String) { _uiState.value = _uiState.value.copy(examDate = value) }
    fun onStartTimeChange(value: String) { _uiState.value = _uiState.value.copy(startTime = value) }
    fun onEndTimeChange(value: String) {
        _uiState.value = _uiState.value.copy(endTime = value)
    }

    fun onStatusChange(value: ExaminationStatus) {
        _uiState.value = _uiState.value.copy(status = value)
    }
    
    fun selectSubject(subjectId: Int) { _uiState.value = _uiState.value.copy(selectedSubjectId = subjectId) }

    fun selectAcademicYear(yearId: Int) {
        if (_uiState.value.selectedAcademicYearId == yearId) return
        _uiState.value = _uiState.value.copy(
            selectedAcademicYearId = yearId,
            selectedSectionId = null,
            sections = emptyList()
        )
        loadSectionsIfPossible()
    }

    fun selectClass(classId: Int) {
        if (_uiState.value.selectedClassId == classId) return
        _uiState.value = _uiState.value.copy(
            selectedClassId = classId,
            selectedSectionId = null,
            sections = emptyList()
        )
        loadSectionsIfPossible()
    }
    
    fun selectSection(sectionId: Int) {
        if (_uiState.value.selectedSectionId == sectionId) return
        _uiState.value = _uiState.value.copy(selectedSectionId = sectionId, selectedSubjectId = null, subjects = emptyList())
        loadSubjectsForSectionIfPossible()
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
                    // Ignore
                }
            }
        }
    }

    private fun loadSubjectsForSectionIfPossible() {
        val sectionId = _uiState.value.selectedSectionId
        val yearId = _uiState.value.selectedAcademicYearId
        if (sectionId != null && yearId != null) {
            viewModelScope.launch {
                try {
                    val res = apiService.getSubjectsBySection(sectionId, yearId)
                    if (res.isSuccessful) {
                        val sectionSubjects = res.body()?.data ?: emptyList()
                        _uiState.value = _uiState.value.copy(subjects = sectionSubjects)
                    }
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
    }

    fun submit() {
        val state = _uiState.value
        if (!state.isFormValid) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, error = null)
            
            val examination = Examination(
                examinationId = state.examinationId ?: 0,
                examName = state.examName,
                subjectId = state.selectedSubjectId!!,
                sectionId = state.selectedSectionId!!,
                academicYearId = state.selectedAcademicYearId!!,
                examDate = state.examDate,
                startTime = state.startTime,
                endTime = state.endTime,
                maxMarks = state.maxMarks.toDouble(),
                passingMarks = state.passingMarks.toDoubleOrNull(),
                status = state.status
            )
            
            try {
                val res = if (state.isEditMode) {
                    apiService.updateExamination(examination.examinationId, examination)
                } else {
                    apiService.createExamination(examination)
                }
                
                if (res.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isSubmitting = false, submitSuccess = true)
                } else {
                    val errString = res.errorBody()?.string()
                    val errMsg = try {
                        org.json.JSONObject(errString!!).getString("message")
                    } catch (e: Exception) {
                        "Submission failed"
                    }
                    _uiState.value = _uiState.value.copy(isSubmitting = false, error = errMsg)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSubmitting = false, error = "Network error: ${e.message}")
            }
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    class Factory(private val apiService: ApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ExaminationFormViewModel(apiService) as T
        }
    }
}
