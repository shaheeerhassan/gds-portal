package com.school.gdsportal.ui.parent.announcements

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

data class ParentAnnouncementsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val announcements: List<Announcement> = emptyList()
)

class ParentAnnouncementsViewModel(private val apiService: ApiService) : ViewModel() {
    private val _uiState = MutableStateFlow(ParentAnnouncementsUiState())
    val uiState: StateFlow<ParentAnnouncementsUiState> = _uiState.asStateFlow()

    init { loadAnnouncements() }

    fun loadAnnouncements() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                // 1. Fetch ALL active announcements using the base "/" endpoint
                val response = apiService.getAnnouncements()

                if (response.isSuccessful) {
                    val allAnnouncements = response.body()?.data ?: emptyList()

                    // 2. Filter locally: Keep only Global (null/0) or Parent-specific announcements
                    val parentAnnouncements = allAnnouncements.filter { announcement ->

                        announcement.targetRoleId == null ||
                                announcement.targetRoleId == 5
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            announcements = parentAnnouncements.sortedByDescending { a -> a.createdAt }
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
                    return ParentAnnouncementsViewModel(apiService) as T
                }
            }
    }
}