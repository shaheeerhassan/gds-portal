package com.school.gdsportal.ui.admin.students

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.AcademicYear
import com.school.gdsportal.data.remote.SchoolClass
import com.school.gdsportal.data.remote.Section
import com.school.gdsportal.data.remote.dto.StudentDirectoryDTO
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StudentsDirectoryUiState(
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val error: String? = null,
    val students: List<StudentDirectoryDTO> = emptyList(),
    val totalStudents: Int = 0,

    // Filter options
    val academicYears: List<AcademicYear> = emptyList(),
    val classes: List<SchoolClass> = emptyList(),
    val sections: List<Section> = emptyList(),

    // Draft Filters (in bottom sheet)
    val draftAcademicYear: AcademicYear? = null,
    val draftClass: SchoolClass? = null,
    val draftSection: Section? = null,
    val draftEnrollmentStatus: Boolean? = null, // null = All, true = Enrolled, false = Not Enrolled

    // Applied Filters (used for actual API calls)
    val appliedAcademicYear: AcademicYear? = null,
    val appliedClass: SchoolClass? = null,
    val appliedSection: Section? = null,
    val appliedEnrollmentStatus: Boolean? = null
)

class StudentsDirectoryViewModel(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentsDirectoryUiState())
    val uiState: StateFlow<StudentsDirectoryUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadFilterOptions()
        loadData(isDebounced = false)
    }

    private fun loadFilterOptions() {
        viewModelScope.launch {
            try {
                val yearsRes = apiService.getAcademicYears()
                if (yearsRes.isSuccessful) {
                    val years = yearsRes.body()?.data ?: emptyList()
                    val currentYear = years.find { it.isCurrent }
                    _uiState.update { 
                        it.copy(
                            academicYears = years,
                            // Optionally we could pre-select current year, but user said "Restore defaults -> clear filters".
                            // I will just leave them null initially to match "All"
                        ) 
                    }
                }
                
                val classesRes = apiService.getClasses()
                if (classesRes.isSuccessful) {
                    _uiState.update { it.copy(classes = classesRes.body()?.data ?: emptyList()) }
                }
            } catch (e: Exception) {
                // Ignore silent failures for filter loading for now
            }
        }
    }

    private fun loadSectionsForClass(classId: Int, academicYearId: Int) {
        viewModelScope.launch {
            try {
                val res = apiService.getSections(classId, academicYearId)
                if (res.isSuccessful) {
                    _uiState.update { it.copy(sections = res.body()?.data ?: emptyList()) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(sections = emptyList()) }
            }
        }
    }

    // --- Filter Handlers ---

    fun onDraftAcademicYearChanged(year: AcademicYear?) {
        _uiState.update { 
            it.copy(
                draftAcademicYear = year,
                draftClass = null,
                draftSection = null,
                sections = emptyList() // clear sections as they depend on year+class
            ) 
        }
    }

    fun onDraftClassChanged(schoolClass: SchoolClass?) {
        val state = _uiState.value
        val yearId = state.draftAcademicYear?.academicYearId ?: state.academicYears.find { it.isCurrent }?.academicYearId
        
        _uiState.update { 
            it.copy(
                draftClass = schoolClass,
                draftSection = null,
                sections = emptyList()
            ) 
        }
        
        if (schoolClass != null && yearId != null) {
            loadSectionsForClass(schoolClass.classId, yearId)
        }
    }

    fun onDraftSectionChanged(section: Section?) {
        _uiState.update { it.copy(draftSection = section) }
    }

    fun onDraftEnrollmentStatusChanged(status: Boolean?) {
        _uiState.update { it.copy(draftEnrollmentStatus = status) }
    }

    fun applyFilters() {
        val state = _uiState.value
        _uiState.update {
            it.copy(
                appliedAcademicYear = state.draftAcademicYear,
                appliedClass = state.draftClass,
                appliedSection = state.draftSection,
                appliedEnrollmentStatus = state.draftEnrollmentStatus
            )
        }
        loadData(isDebounced = false)
    }

    fun resetFilters() {
        _uiState.update {
            it.copy(
                draftAcademicYear = null,
                draftClass = null,
                draftSection = null,
                draftEnrollmentStatus = null,
                appliedAcademicYear = null,
                appliedClass = null,
                appliedSection = null,
                appliedEnrollmentStatus = null,
                sections = emptyList()
            )
        }
        loadData(isDebounced = false)
    }

    // Sync draft filters with applied filters when bottom sheet opens
    fun syncDraftFilters() {
        val state = _uiState.value
        _uiState.update {
            it.copy(
                draftAcademicYear = state.appliedAcademicYear,
                draftClass = state.appliedClass,
                draftSection = state.appliedSection,
                draftEnrollmentStatus = state.appliedEnrollmentStatus
            )
        }
        
        // Reload sections if needed
        val classId = state.appliedClass?.classId
        val yearId = state.appliedAcademicYear?.academicYearId ?: state.academicYears.find { it.isCurrent }?.academicYearId
        if (classId != null && yearId != null) {
            loadSectionsForClass(classId, yearId)
        }
    }

    // --- Search & Data Loading ---

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        loadData(isDebounced = true)
    }

    fun retry() {
        loadData(isDebounced = false)
    }

    private fun loadData(isDebounced: Boolean) {
        searchJob?.cancel()
        
        searchJob = viewModelScope.launch {
            if (isDebounced) {
                delay(500)
            }
            
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val state = _uiState.value
                val currentQuery = state.searchQuery.takeIf { it.isNotBlank() }
                
                val response = apiService.getStudentsDirectory(
                    query = currentQuery,
                    academicYearId = state.appliedAcademicYear?.academicYearId,
                    classId = state.appliedClass?.classId,
                    sectionId = state.appliedSection?.sectionId,
                    enrolled = state.appliedEnrollmentStatus,
                    page = 0,
                    size = 20
                )
                
                if (response.isSuccessful) {
                    val paginatedResponse = response.body()?.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            students = paginatedResponse?.content ?: emptyList(),
                            totalStudents = paginatedResponse?.totalElements ?: 0
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to load students. Please try again."
                        )
                    }
                }
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Unable to load students. Please check your connection."
                        )
                    }
                }
            }
        }
    }

    companion object {
        fun provideFactory(apiService: ApiService): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return StudentsDirectoryViewModel(apiService) as T
            }
        }
    }
}
