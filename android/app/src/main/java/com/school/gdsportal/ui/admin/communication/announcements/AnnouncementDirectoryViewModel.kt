package com.school.gdsportal.ui.admin.communication.announcements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.Announcement
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AnnouncementDirectoryUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val announcements: List<Announcement> = emptyList(),
    val searchQuery: String = "",
    val filteredAnnouncements: List<Announcement> = emptyList()
)

class AnnouncementDirectoryViewModel(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnnouncementDirectoryUiState())
    val uiState: StateFlow<AnnouncementDirectoryUiState> = _uiState.asStateFlow()

    init {
        loadAnnouncements()
    }

    fun loadAnnouncements() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val res = apiService.getAnnouncements()
                if (res.isSuccessful) {
                    val list = res.body()?.data ?: emptyList()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        announcements = list,
                        filteredAnnouncements = filterList(list, _uiState.value.searchQuery)
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load announcements."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Network error while loading announcements."
                )
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredAnnouncements = filterList(_uiState.value.announcements, query)
        )
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
        _uiState.value = _uiState.value.copy(error = null)
    }

    class Factory(private val apiService: ApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AnnouncementDirectoryViewModel(apiService) as T
        }
    }
}
