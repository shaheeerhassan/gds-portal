package com.school.gdsportal.ui.admin.assessment.examinations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.Examination
import com.school.gdsportal.data.remote.ExaminationStatus
import com.school.gdsportal.data.remote.StatusRequest
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ExaminationDetailUiState(
    val isLoading: Boolean = true,
    val isUpdatingStatus: Boolean = false,
    val error: String? = null,
    val examination: Examination? = null,
    
    // Lookups for names
    val academicYearName: String = "",
    val className: String = "",
    val sectionName: String = "",
    val subjectName: String = ""
)

class ExaminationDetailViewModel(private val apiService: ApiService) : ViewModel() {

    private val _uiState = MutableStateFlow(ExaminationDetailUiState())
    val uiState: StateFlow<ExaminationDetailUiState> = _uiState.asStateFlow()

    fun loadExamination(examinationId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val res = apiService.getExaminationById(examinationId)
                if (res.isSuccessful) {
                    val exam = res.body()?.data
                    if (exam != null) {
                        _uiState.value = _uiState.value.copy(examination = exam)
                        loadLookups(exam.academicYearId, exam.sectionId, exam.subjectId)
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false, error = "Examination not found")
                    }
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to load examination")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Network error: ${e.message}")
            }
        }
    }

    private suspend fun loadLookups(yearId: Int, sectionId: Int, subjectId: Int) {
        try {
            coroutineScope {
                val yearsDef = async { apiService.getAcademicYears() }
                val subjectsDef = async { apiService.getSubjects() }
                
                val years = yearsDef.await().body()?.data ?: emptyList()
                val subjects = subjectsDef.await().body()?.data ?: emptyList()
                
                val yearName = years.find { it.academicYearId == yearId }?.yearName ?: "Unknown Year"
                val subjectName = subjects.find { it.subjectId == subjectId }?.subjectName ?: "Unknown Subject"
                
                var className = "Unknown Class"
                var sectionName = "Unknown Section"
                
                // We need to resolve Class from Section
                // We fetch all classes, and for each we check its sections, or we just fetch classes then get sections for all classes?
                // The easiest is to fetch classes, and for each class fetch sections until we find the section.
                // Or we can just use the classes API and loop, or just fetch section if an endpoint exists.
                // Wait, there is no getSectionById endpoint. We will have to fetch all classes.
                val classesRes = apiService.getClasses()
                if (classesRes.isSuccessful) {
                    val classes = classesRes.body()?.data ?: emptyList()
                    for (c in classes) {
                        try {
                            val secRes = apiService.getSections(c.classId, yearId)
                            if (secRes.isSuccessful) {
                                val section = secRes.body()?.data?.find { it.sectionId == sectionId }
                                if (section != null) {
                                    className = c.className
                                    sectionName = section.sectionName
                                    break
                                }
                            }
                        } catch (e: Exception) {
                            // ignore inner exception
                        }
                    }
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    academicYearName = yearName,
                    className = className,
                    sectionName = sectionName,
                    subjectName = subjectName
                )
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun updateStatus(newStatus: ExaminationStatus) {
        val examId = _uiState.value.examination?.examinationId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdatingStatus = true, error = null)
            try {
                val res = apiService.updateExaminationStatus(examId, StatusRequest(status = newStatus.name))
                if (res.isSuccessful) {
                    val updatedExam = _uiState.value.examination?.copy(status = newStatus)
                    _uiState.value = _uiState.value.copy(isUpdatingStatus = false, examination = updatedExam)
                } else {
                    val errString = res.errorBody()?.string()
                    val errMsg = try {
                        org.json.JSONObject(errString!!).getString("message")
                    } catch (e: Exception) {
                        "Failed to update status"
                    }
                    _uiState.value = _uiState.value.copy(isUpdatingStatus = false, error = errMsg)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isUpdatingStatus = false, error = "Network error: ${e.message}")
            }
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    class Factory(private val apiService: ApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ExaminationDetailViewModel(apiService) as T
        }
    }
}
