package com.school.gdsportal.ui.teacher.timetable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.local.TokenManager
import com.school.gdsportal.data.remote.Timetable
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TeacherTimetableUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    // Grouping the timetable by Day (e.g., "MONDAY" -> List of Periods)
    val weeklySchedule: Map<String, List<Timetable>> = emptyMap()
)

class TeacherTimetableViewModel(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherTimetableUiState())
    val uiState: StateFlow<TeacherTimetableUiState> = _uiState.asStateFlow()

    private val dayOrder = listOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY")

    init { loadTimetable() }

    fun loadTimetable() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                // 1. Identify the logged-in teacher
                val teacherId = tokenManager.getUserProfile()?.userId ?: 0L
                if (teacherId == 0L) {
                    _uiState.update { it.copy(isLoading = false, error = "Unable to identify teacher account.") }
                    return@launch
                }

                // 2. Fetch the current Academic Year
                val yearResponse = apiService.getCurrentAcademicYear()
                val currentYear = yearResponse.body()?.data
                if (currentYear == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Could not determine the current academic year.") }
                    return@launch
                }

                // 3. Fetch the timetable specifically for this teacher
                val timetableResponse = apiService.getTimetableByTeacher(teacherId, currentYear.academicYearId)
                if (timetableResponse.isSuccessful) {
                    val rawTimetable = timetableResponse.body()?.data ?: emptyList()

                    // 4. Group by day of the week and sort by start time
                    val grouped = rawTimetable
                        .groupBy { it.dayOfWeek.uppercase() }
                        .mapValues { entry ->
                            entry.value.sortedBy { it.startTime }
                        }
                        // Sort the days logically (Monday to Sunday)
                        .toSortedMap(compareBy { dayOrder.indexOf(it) })

                    _uiState.update {
                        it.copy(isLoading = false, weeklySchedule = grouped)
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load timetable.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Network error while fetching timetable.") }
            }
        }
    }

    companion object {
        fun provideFactory(apiService: ApiService, tokenManager: TokenManager): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TeacherTimetableViewModel(apiService, tokenManager) as T
                }
            }
    }
}