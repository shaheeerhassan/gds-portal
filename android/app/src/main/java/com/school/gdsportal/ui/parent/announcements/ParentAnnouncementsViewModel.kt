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
                // FETCH ONLY GLOBAL ANNOUNCEMENTS
                val response = apiService.getGlobalAnnouncements()
                if (response.isSuccessful) {
                    val data = response.body()?.data ?: emptyList()
                    _uiState.update { it.copy(isLoading = false, announcements = data.sortedByDescending { a -> a.createdAt }) }
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