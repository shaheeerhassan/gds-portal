package com.school.gdsportal.ui.admin.communication.announcements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.AcademicYear
import com.school.gdsportal.data.remote.Announcement
import com.school.gdsportal.data.remote.SchoolClass
import com.school.gdsportal.data.remote.Section
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AnnouncementTargetType {
    GLOBAL, ROLE, CLASS, SECTION
}

data class AnnouncementCreateEditUiState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val createdAnnouncementId: Long? = null,
    val error: String? = null,

    val title: String = "",
    val content: String = "",
    val notify: Boolean = false,
    
    val targetType: AnnouncementTargetType = AnnouncementTargetType.GLOBAL,

    // Selections
    val selectedRoleId: Int? = null,

    val academicYears: List<AcademicYear> = emptyList(),
    val selectedAcademicYearId: Int? = null,

    val classes: List<SchoolClass> = emptyList(),
    val selectedClassId: Int? = null,

    val sections: List<Section> = emptyList(),
    val selectedSectionId: Int? = null,

    // Is Edit Mode?
    val isEditMode: Boolean = false,
    val existingAnnouncement: Announcement? = null
)

class AnnouncementCreateEditViewModel(
    private val apiService: ApiService,
    private val announcementId: Long?
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnnouncementCreateEditUiState())
    val uiState: StateFlow<AnnouncementCreateEditUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                coroutineScope {
                    val yearsDef = async { apiService.getAcademicYears() }
                    val classesDef = async { apiService.getClasses() }
                    val announcementDef = if (announcementId != null) async { apiService.getAnnouncementById(announcementId) } else null

                    val yearsRes = yearsDef.await()
                    val classesRes = classesDef.await()
                    val annRes = announcementDef?.await()

                    if (yearsRes.isSuccessful && classesRes.isSuccessful) {
                        val years = yearsRes.body()?.data ?: emptyList()
                        val classes = classesRes.body()?.data ?: emptyList()
                        val defaultYearId = years.firstOrNull { it.isCurrent }?.academicYearId ?: years.firstOrNull()?.academicYearId

                        var state = _uiState.value.copy(
                            isLoading = false,
                            academicYears = years,
                            classes = classes,
                            selectedAcademicYearId = defaultYearId
                        )

                        if (annRes != null && annRes.isSuccessful) {
                            val ann = annRes.body()?.data
                            if (ann != null) {
                                val tType = when {
                                    ann.targetRoleId != null -> AnnouncementTargetType.ROLE
                                    ann.sectionId != null -> AnnouncementTargetType.SECTION
                                    ann.classId != null -> AnnouncementTargetType.CLASS
                                    else -> AnnouncementTargetType.GLOBAL
                                }

                                state = state.copy(
                                    isEditMode = true,
                                    existingAnnouncement = ann,
                                    title = ann.title,
                                    content = ann.content,
                                    targetType = tType,
                                    selectedRoleId = ann.targetRoleId,
                                    selectedClassId = ann.classId,
                                    selectedSectionId = ann.sectionId
                                )

                                if (tType == AnnouncementTargetType.SECTION && ann.classId != null && defaultYearId != null) {
                                    // Load sections if necessary
                                    val secRes = apiService.getSections(ann.classId, defaultYearId)
                                    if (secRes.isSuccessful) {
                                        state = state.copy(sections = secRes.body()?.data ?: emptyList())
                                    }
                                }
                            }
                        }
                        _uiState.value = state
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to load initial data.")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Network error.")
            }
        }
    }

    fun onTitleChanged(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun onContentChanged(content: String) {
        _uiState.value = _uiState.value.copy(content = content)
    }

    fun onNotifyChanged(notify: Boolean) {
        _uiState.value = _uiState.value.copy(notify = notify)
    }

    fun onTargetTypeChanged(type: AnnouncementTargetType) {
        _uiState.value = _uiState.value.copy(targetType = type)
    }

    fun onRoleSelected(roleId: Int?) {
        _uiState.value = _uiState.value.copy(selectedRoleId = roleId)
    }

    fun onClassSelected(classId: Int?) {
        val id = if (classId == 0) null else classId
        _uiState.value = _uiState.value.copy(
            selectedClassId = id,
            selectedSectionId = null,
            sections = emptyList()
        )
        val yearId = _uiState.value.selectedAcademicYearId
        if (id != null && yearId != null && _uiState.value.targetType == AnnouncementTargetType.SECTION) {
            viewModelScope.launch {
                try {
                    val res = apiService.getSections(id, yearId)
                    if (res.isSuccessful) {
                        _uiState.value = _uiState.value.copy(sections = res.body()?.data ?: emptyList())
                    }
                } catch (_: Exception) {}
            }
        }
    }

    fun onSectionSelected(sectionId: Int?) {
        val id = if (sectionId == 0) null else sectionId
        _uiState.value = _uiState.value.copy(selectedSectionId = id)
    }

    fun submit() {
        val state = _uiState.value
        if (state.title.isBlank() || state.content.isBlank()) {
            _uiState.value = state.copy(error = "Title and Content are required.")
            return
        }

        val request = Announcement(
            announcementId = state.existingAnnouncement?.announcementId ?: 0L,
            title = state.title.trim(),
            content = state.content.trim(),
            isActive = state.existingAnnouncement?.isActive ?: true,
            targetRoleId = if (state.targetType == AnnouncementTargetType.ROLE) state.selectedRoleId else null,
            classId = if (state.targetType == AnnouncementTargetType.CLASS || state.targetType == AnnouncementTargetType.SECTION) state.selectedClassId else null,
            sectionId = if (state.targetType == AnnouncementTargetType.SECTION) state.selectedSectionId else null
        )

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, error = null)
            try {
                if (state.isEditMode) {
                    val res = apiService.updateAnnouncement(request.announcementId, request)
                    if (res.isSuccessful) {
                        _uiState.value = _uiState.value.copy(isSubmitting = false, isSuccess = true, createdAnnouncementId = request.announcementId)
                    } else {
                        _uiState.value = _uiState.value.copy(isSubmitting = false, error = "Failed to update announcement.")
                    }
                } else {
                    val res = apiService.createAnnouncement(request, notify = state.notify)
                    if (res.isSuccessful) {
                        val createdId = res.body()?.data?.announcementId
                        _uiState.value = _uiState.value.copy(isSubmitting = false, isSuccess = true, createdAnnouncementId = createdId)
                    } else {
                        _uiState.value = _uiState.value.copy(isSubmitting = false, error = "Failed to create announcement.")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSubmitting = false, error = "Network error during submission.")
            }
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun disableAnnouncement() {
        if (!_uiState.value.isEditMode || announcementId == null) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, error = null)
            try {
                val res = apiService.disableAnnouncement(announcementId)
                if (res.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isSubmitting = false, isSuccess = true)
                } else {
                    _uiState.value = _uiState.value.copy(isSubmitting = false, error = "Failed to disable announcement.")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSubmitting = false, error = "Network error.")
            }
        }
    }

    fun resetSuccess() {
        _uiState.value = _uiState.value.copy(isSuccess = false)
    }

    class Factory(private val apiService: ApiService, private val announcementId: Long?) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AnnouncementCreateEditViewModel(apiService, announcementId) as T
        }
    }
}
