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

data class AnnouncementDetailUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val announcement: Announcement? = null,
    val targetName: String = "Global",
    val isDisabling: Boolean = false,
    val showDisableDialog: Boolean = false,
    val disableSuccess: Boolean = false
)

class AnnouncementDetailViewModel(
    private val apiService: ApiService,
    private val announcementId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnnouncementDetailUiState())
    val uiState: StateFlow<AnnouncementDetailUiState> = _uiState.asStateFlow()

    init {
        loadAnnouncement()
    }

    fun loadAnnouncement() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val res = apiService.getAnnouncementById(announcementId)
                if (res.isSuccessful) {
                    val announcement = res.body()?.data
                    if (announcement != null) {
                        val target = resolveTargetName(announcement)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            announcement = announcement,
                            targetName = target
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Announcement data is empty."
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load announcement."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Network error while loading announcement."
                )
            }
        }
    }

    private suspend fun resolveTargetName(announcement: Announcement): String {
        return when {
            announcement.targetRoleId != null -> {
                // To keep it simple without N+1 requests, we can just say "Role ID: ${announcement.targetRoleId}"
                // Or if we have a getRoles API we could call it. Since there isn't a direct API for one role,
                // we'll just format it. A real system might have a roles endpoint.
                "Role ID: ${announcement.targetRoleId}"
            }
            announcement.sectionId != null -> {
                try {
                    val res = apiService.getSectionById(announcement.sectionId)
                    if (res.isSuccessful) {
                        res.body()?.data?.sectionName ?: "Section ID: ${announcement.sectionId}"
                    } else {
                        "Section ID: ${announcement.sectionId}"
                    }
                } catch (e: Exception) {
                    "Section ID: ${announcement.sectionId}"
                }
            }
            announcement.classId != null -> {
                try {
                    val res = apiService.getClassById(announcement.classId)
                    if (res.isSuccessful) {
                        res.body()?.data?.className ?: "Class ID: ${announcement.classId}"
                    } else {
                        "Class ID: ${announcement.classId}"
                    }
                } catch (e: Exception) {
                    "Class ID: ${announcement.classId}"
                }
            }
            else -> "Global"
        }
    }

    fun showDisableDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showDisableDialog = show)
    }

    fun disableAnnouncement() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDisabling = true, showDisableDialog = false)
            try {
                val res = apiService.disableAnnouncement(announcementId)
                if (res.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isDisabling = false, disableSuccess = true)
                    loadAnnouncement()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isDisabling = false,
                        error = "Failed to disable announcement."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDisabling = false,
                    error = "Network error while disabling announcement."
                )
            }
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    class Factory(private val apiService: ApiService, private val announcementId: Long) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AnnouncementDetailViewModel(apiService, announcementId) as T
        }
    }
}
