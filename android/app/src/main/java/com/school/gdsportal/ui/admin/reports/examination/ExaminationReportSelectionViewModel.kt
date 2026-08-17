package com.school.gdsportal.ui.admin.reports.examination

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.AcademicYear
import com.school.gdsportal.data.remote.Examination
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

data class ExaminationReportSelectionUiState(
    val isLoading: Boolean = true,
    val error: String? = null,

    val academicYears: List<AcademicYear> = emptyList(),
    val selectedAcademicYearId: Int? = null,

    val classes: List<SchoolClass> = emptyList(),
    val selectedClassId: Int? = null,

    val sections: List<Section> = emptyList(),
    val selectedSectionId: Int? = null,

    val subjects: List<Subject> = emptyList(),
    val selectedSubjectId: Int? = null,

    val allExaminations: List<Examination> = emptyList(),
    val filteredExaminations: List<Examination> = emptyList(),
    val selectedExaminationId: Long? = null
)

class ExaminationReportSelectionViewModel(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExaminationReportSelectionUiState())
    val uiState: StateFlow<ExaminationReportSelectionUiState> = _uiState.asStateFlow()

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

                    val yearsRes = yearsDef.await()
                    val classesRes = classesDef.await()

                    if (yearsRes.isSuccessful && classesRes.isSuccessful) {
                        val years = yearsRes.body()?.data ?: emptyList()
                        val classes = classesRes.body()?.data ?: emptyList()

                        val defaultYearId = years.firstOrNull { it.isCurrent }?.academicYearId
                            ?: years.firstOrNull()?.academicYearId

                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            academicYears = years,
                            classes = classes,
                            selectedAcademicYearId = defaultYearId
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to load initial data.")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Network error while loading data.")
            }
        }
    }

    fun selectAcademicYear(yearId: Int) {
        _uiState.value = _uiState.value.copy(
            selectedAcademicYearId = yearId,
            selectedClassId = null,
            selectedSectionId = null,
            sections = emptyList(),
            selectedSubjectId = null,
            subjects = emptyList(),
            selectedExaminationId = null,
            allExaminations = emptyList(),
            filteredExaminations = emptyList()
        )
    }

    fun selectClass(classId: Int?) {
        val id = if (classId == 0) null else classId
        _uiState.value = _uiState.value.copy(
            selectedClassId = id,
            selectedSectionId = null,
            sections = emptyList(),
            selectedSubjectId = null,
            subjects = emptyList(),
            selectedExaminationId = null,
            allExaminations = emptyList(),
            filteredExaminations = emptyList()
        )
        val yearId = _uiState.value.selectedAcademicYearId
        if (id != null && yearId != null) {
            loadSections(id, yearId)
        }
    }

    private fun loadSections(classId: Int, yearId: Int) {
        viewModelScope.launch {
            try {
                val res = apiService.getSections(classId, yearId)
                if (res.isSuccessful) {
                    val sections = res.body()?.data ?: emptyList()
                    _uiState.value = _uiState.value.copy(sections = sections)
                }
            } catch (_: Exception) { }
        }
    }

    fun selectSection(sectionId: Int?) {
        val id = if (sectionId == 0) null else sectionId
        _uiState.value = _uiState.value.copy(
            selectedSectionId = id,
            selectedSubjectId = null,
            subjects = emptyList(),
            selectedExaminationId = null,
            allExaminations = emptyList(),
            filteredExaminations = emptyList()
        )
        val yearId = _uiState.value.selectedAcademicYearId
        if (id != null && yearId != null) {
            loadSubjectsAndExaminations(id, yearId)
        }
    }

    private fun loadSubjectsAndExaminations(sectionId: Int, yearId: Int) {
        viewModelScope.launch {
            try {
                coroutineScope {
                    val subDef = async { apiService.getSubjectsBySection(sectionId, yearId) }
                    val examDef = async { apiService.getExaminationsBySection(sectionId, yearId) }

                    val subRes = subDef.await()
                    val examRes = examDef.await()

                    val subjects = if (subRes.isSuccessful) subRes.body()?.data ?: emptyList() else emptyList()
                    val exams = if (examRes.isSuccessful) examRes.body()?.data ?: emptyList() else emptyList()

                    _uiState.value = _uiState.value.copy(
                        subjects = subjects,
                        allExaminations = exams
                    )
                }
            } catch (_: Exception) { }
        }
    }

    fun selectSubject(subjectId: Int?) {
        val id = if (subjectId == 0) null else subjectId
        val filtered = if (id == null) {
            emptyList()
        } else {
            _uiState.value.allExaminations.filter { it.subjectId == id }
        }
        _uiState.value = _uiState.value.copy(
            selectedSubjectId = id,
            selectedExaminationId = null,
            filteredExaminations = filtered
        )
    }

    fun selectExamination(examId: Long?) {
        val id = if (examId == 0L) null else examId
        _uiState.value = _uiState.value.copy(selectedExaminationId = id)
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    class Factory(private val apiService: ApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ExaminationReportSelectionViewModel(apiService) as T
        }
    }
}
