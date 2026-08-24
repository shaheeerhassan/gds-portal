package com.school.gdsportal.ui.teacher.announcements

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

data class TeacherAnnouncementsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val announcements: List<Announcement> = emptyList(),
    val searchQuery: String = "",
    val filteredAnnouncements: List<Announcement> = emptyList()
)

class TeacherAnnouncementsViewModel(private val apiService: ApiService) : ViewModel() {
    private val _uiState = MutableStateFlow(TeacherAnnouncementsUiState())
    val uiState: StateFlow<TeacherAnnouncementsUiState> = _uiState.asStateFlow()

    init { loadAnnouncements() }

    fun loadAnnouncements() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val response = apiService.getAnnouncements()
                if (response.isSuccessful) {
                    val allAnnouncements = response.body()?.data ?: emptyList()
                    val teacherAnnouncements = allAnnouncements.filter { announcement ->
                        announcement.targetRoleId == null || announcement.targetRoleId == 0 || announcement.targetRoleId == 4 || announcement.targetRoleId == 3
                    }.sortedByDescending { a -> a.createdAt }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            announcements = teacherAnnouncements,
                            filteredAnnouncements = filterList(teacherAnnouncements, it.searchQuery)
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

    fun updateSearchQuery(query: String) {
        _uiState.update {
            it.copy(
                searchQuery = query,
                filteredAnnouncements = filterList(it.announcements, query)
            )
        }
    }

    private fun filterList(list: List<Announcement>, query: String): List<Announcement> {
        if (query.isBlank()) return list
        val lowerQuery = query.lowercase()
        return list.filter {
            it.title.lowercase().contains(lowerQuery) ||
                    it.content.lowercase().contains(lowerQuery)
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    companion object {
        fun provideFactory(apiService: ApiService): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TeacherAnnouncementsViewModel(apiService) as T
                }
            }
    }
}