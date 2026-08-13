package com.school.gdsportal.ui.admin.students.enrollment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.AcademicYear
import com.school.gdsportal.data.remote.Enrollment
import com.school.gdsportal.data.remote.SchoolClass
import com.school.gdsportal.data.remote.Section
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EnrollmentHistoryItem(
    val enrollment: Enrollment,
    val academicYearName: String,
    val className: String,
    val sectionName: String
)

data class StudentEnrollmentUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val enrollmentHistory: List<EnrollmentHistoryItem> = emptyList()
)

class StudentEnrollmentViewModel(
    private val studentId: Long,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentEnrollmentUiState())
    val uiState: StateFlow<StudentEnrollmentUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun retry() {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // Fetch basic filter options so we can map IDs to names
                val yearsRes = apiService.getAcademicYears()
                val classesRes = apiService.getClasses()
                
                val yearsMap = if (yearsRes.isSuccessful) yearsRes.body()?.data?.associateBy { it.academicYearId } ?: emptyMap() else emptyMap()
                val classesMap = if (classesRes.isSuccessful) classesRes.body()?.data?.associateBy { it.classId } ?: emptyMap() else emptyMap()

                // Cache for sections so we don't fetch the same class/year combo repeatedly
                val sectionMapCache = mutableMapOf<Pair<Int, Int>, Map<Int, String>>()

                val response = apiService.getEnrollmentHistory(studentId)
                if (response.isSuccessful && response.body()?.data != null) {
                    val enrollments = response.body()?.data ?: emptyList()
                    
                    val historyItems = enrollments.map { enrollment ->
                        // Determine section name
                        val classId = enrollment.classId
                        val yearId = enrollment.academicYearId
                        val cacheKey = Pair(classId, yearId)
                        
                        if (!sectionMapCache.containsKey(cacheKey)) {
                            val sectionRes = apiService.getSections(classId, yearId)
                            val map = if (sectionRes.isSuccessful) {
                                sectionRes.body()?.data?.associateBy({ it.sectionId }, { it.sectionName }) ?: emptyMap()
                            } else {
                                emptyMap()
                            }
                            sectionMapCache[cacheKey] = map
                        }
                        
                        val sectionName = sectionMapCache[cacheKey]?.get(enrollment.sectionId) ?: "-"
                        
                        EnrollmentHistoryItem(
                            enrollment = enrollment,
                            academicYearName = yearsMap[yearId]?.yearName ?: "-",
                            className = classesMap[classId]?.className ?: "-",
                            sectionName = sectionName
                        )
                    }

                    // Sort history so current/newest is at the top
                    val sortedHistory = historyItems.sortedByDescending { it.enrollment.enrollmentDate }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            enrollmentHistory = sortedHistory
                        )
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Failed to load enrollment history."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Network error. Please try again."
                    )
                }
            }
        }
    }

    companion object {
        fun provideFactory(studentId: Long, apiService: ApiService): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return StudentEnrollmentViewModel(studentId, apiService) as T
            }
        }
    }
}
