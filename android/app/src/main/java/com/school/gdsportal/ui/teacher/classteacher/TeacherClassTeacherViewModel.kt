package com.school.gdsportal.ui.teacher.classteacher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.local.TokenManager
import com.school.gdsportal.data.remote.ClassTeacherAssignment
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TeacherClassTeacherUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val currentAssignment: ClassTeacherAssignment? = null,
    val pastAssignments: List<ClassTeacherAssignment> = emptyList(),
    val sectionNames: Map<Int, String> = emptyMap(),
    val yearNames: Map<Int, String> = emptyMap()
)

class TeacherClassTeacherViewModel(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherClassTeacherUiState())
    val uiState: StateFlow<TeacherClassTeacherUiState> = _uiState.asStateFlow()

    init { loadClassTeacherHistory() }

    fun loadClassTeacherHistory() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                // 1. Get Teacher ID from local token
                val teacherId = apiService.getTeacherMe().body()?.data?.teacherId ?: 0L
                if (teacherId == 0L) {
                    _uiState.update { it.copy(isLoading = false, error = "Unable to identify teacher account.") }
                    return@launch
                }

                // 2. Fetch the current Academic Year and the Teacher's Assignment History concurrently
                val yearDef = async { apiService.getCurrentAcademicYear() }
                val historyDef = async { apiService.getClassTeacherHistory(teacherId) } // Uses the history endpoint

                val yearRes = yearDef.await()
                val historyRes = historyDef.await()

                val currentYearId = yearRes.body()?.data?.academicYearId

                if (historyRes.isSuccessful) {
                    val allAssignments = historyRes.body()?.data ?: emptyList()

                    // Fetch lookups
                    val sectionIds = allAssignments.map { it.sectionId }.distinct()
                    val sNames = mutableMapOf<Int, String>()
                    val yNames = mutableMapOf<Int, String>()
                    
                    try {
                        val years = apiService.getAcademicYears().body()?.data ?: emptyList()
                        years.forEach { yNames[it.academicYearId] = it.yearName }
                        
                        sectionIds.forEach { sid ->
                            val sec = apiService.getSectionById(sid).body()?.data
                            if (sec != null) {
                                val cId = sec.classId
                                val cName = apiService.getClassById(cId).body()?.data?.className
                                sNames[sid] = "${cName ?: "Class $cId"} - ${sec.sectionName}"
                            }
                        }
                    } catch (e: Exception) {}

                    // 3. Separate current year from past years
                    val current = allAssignments.find { it.academicYearId == currentYearId }
                    val past = allAssignments.filter { it.academicYearId != currentYearId }
                        .sortedByDescending { it.academicYearId } // Sort newest past assignments first

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentAssignment = current,
                            pastAssignments = past,
                            sectionNames = sNames,
                            yearNames = yNames
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load class teacher assignments.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Network error while fetching assignments.") }
            }
        }
    }

    companion object {
        fun provideFactory(apiService: ApiService, tokenManager: TokenManager): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TeacherClassTeacherViewModel(apiService, tokenManager) as T
                }
            }
    }
}
