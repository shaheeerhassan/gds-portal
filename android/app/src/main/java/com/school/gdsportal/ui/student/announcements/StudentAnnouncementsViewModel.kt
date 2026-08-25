package com.school.gdsportal.ui.student.announcements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.Announcement
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StudentAnnouncementsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val announcements: List<Announcement> = emptyList()
)

class StudentAnnouncementsViewModel(private val apiService: ApiService) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentAnnouncementsUiState())
    val uiState: StateFlow<StudentAnnouncementsUiState> = _uiState.asStateFlow()

    init {
        loadAnnouncements()
    }

    private fun loadAnnouncements() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                // Fetch all announcements
                val response = apiService.getAnnouncements()
                if (response.isSuccessful) {
                    val all = response.body()?.data ?: emptyList()

                    // Filter: Keep only Global (null/0) or Student Role (4)
                    val studentAnnouncements = all.filter {
                        it.isActive && (it.targetRoleId == null || it.targetRoleId == 0 || it.targetRoleId == 4)
                    }.sortedByDescending { it.createdAt }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            announcements = studentAnnouncements
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load announcements.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Network error.") }
            }
        }
    }

    companion object {
        fun provideFactory(apiService: ApiService): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return StudentAnnouncementsViewModel(apiService) as T
                }
            }
    }
}