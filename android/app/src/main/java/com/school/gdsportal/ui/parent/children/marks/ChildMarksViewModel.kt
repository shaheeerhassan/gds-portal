package com.school.gdsportal.ui.parent.children.marks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.Mark
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChildMarksUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val groupedMarks: Map<String, List<Mark>> = emptyMap() // Changed to map Exam Name -> Marks
)

class ChildMarksViewModel(
    private val studentId: Long,
    private val apiService: ApiService
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChildMarksUiState())
    val uiState: StateFlow<ChildMarksUiState> = _uiState.asStateFlow()

    init { loadMarks() }

    fun loadMarks() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                // 1. Get Enrollment to find the Section and Year
                val enrollmentRes = apiService.getCurrentEnrollment(studentId)
                val enrollment = enrollmentRes.body()?.data

                if (enrollment == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Active enrollment not found.") }
                    return@launch
                }

                // 2. Fetch Marks and Exams concurrently
                val marksDef = async { apiService.getMarksByStudentAndYear(studentId, enrollment.academicYearId) }
                val examsDef = async { apiService.getExaminationsBySection(enrollment.sectionId, enrollment.academicYearId) }

                val marksRes = marksDef.await()
                val examsRes = examsDef.await()

                val marks = marksRes.body()?.data ?: emptyList()
                val exams = examsRes.body()?.data ?: emptyList()

                // 3. Create a dictionary/map of Exam ID -> Exam Name
                val examIdToName = exams.associate { it.examinationId to it.examName }

                // 4. Group the marks by the Exam Name (fallback to ID if missing)
                val grouped = marks.groupBy { mark ->
                    examIdToName[mark.examinationId] ?: "Examination ID: ${mark.examinationId}"
                }

                _uiState.update { it.copy(isLoading = false, groupedMarks = grouped) }

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Network error.") }
            }
        }
    }

    companion object {
        fun provideFactory(studentId: Long, apiService: ApiService): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ChildMarksViewModel(studentId, apiService) as T
                }
            }
    }
}