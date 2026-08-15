package com.school.gdsportal.ui.admin.assessment.marks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.MarkDisplay
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MarkDetailUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val mark: MarkDisplay? = null,
    val subjectName: String = "",
    val maxMarks: String = ""
)

class MarkDetailViewModel(
    private val apiService: ApiService,
    private val markId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(MarkDetailUiState())
    val uiState: StateFlow<MarkDetailUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val markRes = apiService.getMarkById(markId)
                if (markRes.isSuccessful) {
                    val mark = markRes.body()?.data
                    if (mark != null) {
                        coroutineScope {
                            val studentDef = async { apiService.getStudent(mark.studentId) }
                            val examinationDef = async { apiService.getExaminationById(mark.examinationId) }

                            val student = studentDef.await().body()?.data
                            val examination = examinationDef.await().body()?.data

                            var subjectName = "Unknown Subject"
                            var maxMarks = ""

                            if (examination != null) {
                                maxMarks = examination.maxMarks.toString()
                                val subjectDef = async { apiService.getSubjectById(examination.subjectId) }
                                val subject = subjectDef.await().body()?.data
                                subjectName = subject?.subjectName ?: "Unknown Subject"
                            }

                            val marksText = if (mark.marksObtained != null) {
                                if (maxMarks.isNotEmpty()) "${mark.marksObtained} / $maxMarks" else mark.marksObtained.toString()
                            } else {
                                "—"
                            }

                            val display = MarkDisplay(
                                markId = mark.markId,
                                studentName = if (student != null) "${student.firstName} ${student.lastName}" else "Unknown Student",
                                registrationNumber = student?.registrationNumber ?: "",
                                examinationName = examination?.examName ?: "Examination #${mark.examinationId}",
                                marksObtained = marksText,
                                grade = mark.grade ?: "",
                                remarks = mark.remarks ?: ""
                            )

                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                mark = display,
                                subjectName = subjectName,
                                maxMarks = maxMarks
                            )
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false, error = "Mark not found")
                    }
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to load mark")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Network error: ${e.message}")
            }
        }
    }

    class Factory(private val apiService: ApiService, private val markId: Long) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MarkDetailViewModel(apiService, markId) as T
        }
    }
}
