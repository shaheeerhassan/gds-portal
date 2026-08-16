package com.school.gdsportal.ui.admin.reports.studentperformance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.network.ApiService
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Response

data class PerformanceEntry(
    val examName: String,
    val subjectName: String,
    val marksObtained: Double,
    val maxMarks: Double,
    val grade: String,
    val remarks: String
)

data class StudentPerformanceReportUiState(
    val isLoading: Boolean = true,
    val error: String? = null,

    val studentName: String = "",
    val registrationNumber: String = "",
    val academicYearName: String = "",

    val entries: List<PerformanceEntry> = emptyList(),
    val examGroups: Map<String, List<PerformanceEntry>> = emptyMap()
)

class StudentPerformanceReportViewModel(
    private val apiService: ApiService,
    private val studentId: Long,
    private val academicYearId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentPerformanceReportUiState())
    val uiState: StateFlow<StudentPerformanceReportUiState> = _uiState.asStateFlow()

    init {
        loadReport()
    }

    fun loadReport() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // Load academic year name
                var yearName = ""
                try {
                    val yearRes = apiService.getAcademicYearById(academicYearId)
                    if (yearRes.isSuccessful) {
                        yearName = yearRes.body()?.data?.yearName ?: ""
                    }
                } catch (_: Exception) {}

                val res = apiService.getStudentPerformanceReport(studentId, academicYearId)
                if (res.isSuccessful) {
                    val body = res.body()
                    val data = body?.data

                    if (data != null) {
                        val studentObj = data.get("student")?.asJsonObject
                        val resultsArr = data.get("results")?.asJsonArray

                        val studentName = studentObj?.get("student_name")?.asString ?: ""
                        val regNumber = studentObj?.get("registration_number")?.asString ?: ""

                        val entries = resultsArr?.map { elem ->
                            val obj = elem.asJsonObject
                            PerformanceEntry(
                                examName = obj.get("exam_name")?.asString ?: "",
                                subjectName = obj.get("subject_name")?.asString ?: "",
                                marksObtained = obj.get("marks_obtained")?.asDouble ?: 0.0,
                                maxMarks = obj.get("max_marks")?.asDouble ?: 0.0,
                                grade = obj.get("grade")?.asString ?: "",
                                remarks = obj.get("remarks")?.asString ?: ""
                            )
                        } ?: emptyList()

                        val examGroups = entries.groupBy { it.examName }

                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            studentName = studentName,
                            registrationNumber = regNumber,
                            academicYearName = yearName,
                            entries = entries,
                            examGroups = examGroups
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false, entries = emptyList(), examGroups = emptyMap())
                    }
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Could not load report.")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Could not load report.")
            }
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    class Factory(
        private val apiService: ApiService,
        private val studentId: Long,
        private val academicYearId: Int
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StudentPerformanceReportViewModel(apiService, studentId, academicYearId) as T
        }
    }
}
