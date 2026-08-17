package com.school.gdsportal.ui.admin.communication.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.BulkNotificationsRequest
import com.school.gdsportal.data.remote.Notification
import com.school.gdsportal.data.remote.SchoolClass
import com.school.gdsportal.data.remote.Section
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class NotificationRecipientMode {
    SINGLE,
    BROADCAST
}

// Role IDs as per backend
private const val ROLE_ADMIN = 1
private const val ROLE_PRINCIPAL = 2
private const val ROLE_TEACHER = 3
private const val ROLE_STUDENT = 4
private const val ROLE_PARENT = 5

data class NotificationCreateUiState(
    val recipientMode: NotificationRecipientMode = NotificationRecipientMode.SINGLE,

    // --- SINGLE mode ---
    val selectedRoleId: Int? = null,

    // For Admins (1) & Principals (2): show a plain dropdown
    val listedUsers: List<Pair<Long, String>> = emptyList(), // userId -> display name
    val isLoadingListedUsers: Boolean = false,
    val selectedListedUserId: Long? = null,

    // For Teachers (3): search autocomplete
    val searchQuery: String = "",
    val searchResults: List<Pair<Long, String>> = emptyList(), // userId -> display name
    val isSearching: Boolean = false,
    val selectedUserId: Long? = null,
    val selectedUserName: String? = null,

    // For Students (4) & Parents (5): class -> section -> student search
    val classes: List<SchoolClass> = emptyList(),
    val isLoadingClasses: Boolean = false,
    val selectedClassId: Int? = null,
    val sections: List<Section> = emptyList(),
    val isLoadingSections: Boolean = false,
    val selectedSectionId: Int? = null,
    // studentId stored separately so parent lookup can reference it
    val selectedStudentId: Long? = null,

    // Notification content
    val selectedType: String = "NEW_ANNOUNCEMENT",
    val title: String = "",
    val message: String = "",

    // Submission
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,

    // --- BROADCAST mode ---
    val broadcastGlobal: Boolean = false,
    val broadcastSelectedRoleIds: Set<Int> = emptySet()
)

class NotificationCreateViewModel(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationCreateUiState())
    val uiState: StateFlow<NotificationCreateUiState> = _uiState.asStateFlow()

    init {
        loadClasses()
    }

    private fun loadClasses() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingClasses = true)
            try {
                val res = apiService.getClasses()
                if (res.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isLoadingClasses = false,
                        classes = res.body()?.data ?: emptyList()
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoadingClasses = false)
                }
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isLoadingClasses = false)
            }
        }
    }

    // ─── Mode ────────────────────────────────────────────────────────────────

    fun onRecipientModeChanged(mode: NotificationRecipientMode) {
        _uiState.value = _uiState.value.copy(
            recipientMode = mode,
            selectedRoleId = null,
            listedUsers = emptyList(),
            isLoadingListedUsers = false,
            selectedListedUserId = null,
            searchQuery = "",
            searchResults = emptyList(),
            isSearching = false,
            selectedUserId = null,
            selectedUserName = null,
            selectedClassId = null,
            sections = emptyList(),
            selectedSectionId = null,
            selectedStudentId = null,
            broadcastGlobal = false,
            broadcastSelectedRoleIds = emptySet()
        )
    }

    // ─── Role Selection ──────────────────────────────────────────────────────

    fun onRoleSelected(roleId: Int?) {
        _uiState.value = _uiState.value.copy(
            selectedRoleId = roleId,
            listedUsers = emptyList(),
            isLoadingListedUsers = false,
            selectedListedUserId = null,
            searchQuery = "",
            searchResults = emptyList(),
            isSearching = false,
            selectedUserId = null,
            selectedUserName = null,
            selectedClassId = null,
            sections = emptyList(),
            selectedSectionId = null,
            selectedStudentId = null
        )
        when (roleId) {
            ROLE_ADMIN -> loadAdmins()
            ROLE_PRINCIPAL -> loadPrincipals()
            else -> Unit // Teachers, Students, Parents handle their own flow
        }
    }

    private fun loadAdmins() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingListedUsers = true)
            try {
                val res = apiService.getAdministrators()
                if (res.isSuccessful) {
                    val admins = res.body()?.data ?: emptyList()
                    _uiState.value = _uiState.value.copy(
                        isLoadingListedUsers = false,
                        listedUsers = admins.map { Pair(it.userId, "${it.firstName} ${it.lastName}") }
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoadingListedUsers = false, error = "Failed to load administrators.")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoadingListedUsers = false, error = "Network error.")
            }
        }
    }

    private fun loadPrincipals() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingListedUsers = true)
            try {
                val res = apiService.getPrincipals()
                if (res.isSuccessful) {
                    val principals = res.body()?.data ?: emptyList()
                    _uiState.value = _uiState.value.copy(
                        isLoadingListedUsers = false,
                        listedUsers = principals.map { Pair(it.userId, "${it.firstName} ${it.lastName}") }
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoadingListedUsers = false, error = "Failed to load principals.")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoadingListedUsers = false, error = "Network error.")
            }
        }
    }

    fun onListedUserSelected(userId: Long?) {
        _uiState.value = _uiState.value.copy(selectedListedUserId = userId)
    }

    // ─── Class / Section / Student (for Roles 4 & 5) ────────────────────────

    fun onClassSelected(classId: Int?) {
        val id = if (classId == 0) null else classId
        _uiState.value = _uiState.value.copy(
            selectedClassId = id,
            sections = emptyList(),
            selectedSectionId = null,
            selectedStudentId = null,
            selectedUserId = null,
            selectedUserName = null,
            searchQuery = "",
            searchResults = emptyList()
        )
        if (id != null) loadSections(id)
    }

    private fun loadSections(classId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingSections = true)
            try {
                val res = apiService.getAllSections()
                if (res.isSuccessful) {
                    val all = res.body()?.data ?: emptyList()
                    _uiState.value = _uiState.value.copy(
                        isLoadingSections = false,
                        sections = all.filter { it.classId == classId }
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoadingSections = false)
                }
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isLoadingSections = false)
            }
        }
    }

    fun onSectionSelected(sectionId: Int?) {
        val id = if (sectionId == 0) null else sectionId
        _uiState.value = _uiState.value.copy(
            selectedSectionId = id,
            selectedStudentId = null,
            selectedUserId = null,
            selectedUserName = null,
            searchQuery = "",
            searchResults = emptyList()
        )
    }

    // ─── Teacher name search ──────────────────────────────────────────────────

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            selectedUserId = null,
            selectedUserName = null,
            selectedStudentId = null
        )
        if (query.length >= 2) {
            when (_uiState.value.selectedRoleId) {
                ROLE_TEACHER -> searchTeachers(query)
                ROLE_STUDENT -> searchStudents(query)
                ROLE_PARENT -> searchStudents(query) // Search student, then get parents
                else -> _uiState.value = _uiState.value.copy(searchResults = emptyList())
            }
        } else {
            _uiState.value = _uiState.value.copy(searchResults = emptyList())
        }
    }

    private fun searchTeachers(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true)
            try {
                val res = apiService.searchTeachers(query)
                if (res.isSuccessful) {
                    val teachers = res.body()?.data ?: emptyList()
                    _uiState.value = _uiState.value.copy(
                        isSearching = false,
                        searchResults = teachers.map { Pair(it.userId, "${it.firstName} ${it.lastName}") }
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isSearching = false, searchResults = emptyList())
                }
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isSearching = false, searchResults = emptyList())
            }
        }
    }

    private fun searchStudents(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true)
            try {
                // getStudentsDirectory supports class/section filtering; returns studentId (no userId)
                // We store studentId as the ID key and resolve userId on submit
                val res = apiService.getStudentsDirectory(
                    query = query,
                    academicYearId = null,
                    classId = _uiState.value.selectedClassId,
                    sectionId = _uiState.value.selectedSectionId,
                    enrolled = true,
                    page = 0,
                    size = 20
                )
                if (res.isSuccessful) {
                    val students = res.body()?.data?.content ?: emptyList()
                    // Pair: studentId (Long) to display name
                    _uiState.value = _uiState.value.copy(
                        isSearching = false,
                        searchResults = students.map { Pair(it.studentId, "${it.firstName} ${it.lastName} (${it.registrationNumber})") }
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isSearching = false, searchResults = emptyList())
                }
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isSearching = false, searchResults = emptyList())
            }
        }
    }

    // When user picks from search results:
    // - For Teacher: selectedUserId = teacher's userId (from searchTeachers response)
    // - For Student/Parent: selectedUserId = studentId (from getStudentsDirectory response)
    //   selectedStudentId is the same value; userId is resolved on submit from getStudent(studentId)
    fun onUserSelected(id: Long?, userName: String?) {
        val roleId = _uiState.value.selectedRoleId
        _uiState.value = _uiState.value.copy(
            selectedUserId = id,
            selectedStudentId = if (roleId == ROLE_STUDENT || roleId == ROLE_PARENT) id else _uiState.value.selectedStudentId,
            selectedUserName = userName,
            searchQuery = userName ?: "",
            searchResults = emptyList()
        )
    }

    // ─── Broadcast ───────────────────────────────────────────────────────────

    fun onBroadcastGlobalChanged(isGlobal: Boolean) {
        _uiState.value = _uiState.value.copy(
            broadcastGlobal = isGlobal,
            broadcastSelectedRoleIds = if (isGlobal) emptySet() else _uiState.value.broadcastSelectedRoleIds
        )
    }

    fun onBroadcastRoleToggled(roleId: Int) {
        val current = _uiState.value.broadcastSelectedRoleIds.toMutableSet()
        if (current.contains(roleId)) current.remove(roleId) else current.add(roleId)
        _uiState.value = _uiState.value.copy(
            broadcastSelectedRoleIds = current,
            broadcastGlobal = false
        )
    }

    // ─── Content ─────────────────────────────────────────────────────────────

    fun onTypeSelected(type: String) { _uiState.value = _uiState.value.copy(selectedType = type) }
    fun onTitleChanged(title: String) { _uiState.value = _uiState.value.copy(title = title) }
    fun onMessageChanged(message: String) { _uiState.value = _uiState.value.copy(message = message) }

    // ─── Submit ──────────────────────────────────────────────────────────────

    fun submit() {
        val state = _uiState.value
        if (state.title.isBlank()) { _uiState.value = state.copy(error = "Title is required."); return }
        if (state.message.isBlank()) { _uiState.value = state.copy(error = "Message is required."); return }

        when (state.recipientMode) {
            NotificationRecipientMode.SINGLE -> submitSingle(state)
            NotificationRecipientMode.BROADCAST -> {
                if (!state.broadcastGlobal && state.broadcastSelectedRoleIds.isEmpty()) {
                    _uiState.value = state.copy(error = "Please select 'Global' or at least one target role.")
                    return
                }
                sendBroadcastNotification(
                    state.broadcastGlobal,
                    state.broadcastSelectedRoleIds.toList(),
                    state.selectedType, state.title.trim(), state.message.trim()
                )
            }
        }
    }

    private fun submitSingle(state: NotificationCreateUiState) {
        val type = state.selectedType
        val title = state.title.trim()
        val message = state.message.trim()

        when (state.selectedRoleId) {
            ROLE_ADMIN, ROLE_PRINCIPAL -> {
                val userId = state.selectedListedUserId
                if (userId == null) { _uiState.value = state.copy(error = "Please select a recipient."); return }
                sendSingleNotification(userId, type, title, message)
            }
            ROLE_TEACHER -> {
                // selectedUserId = teacher's userId
                val userId = state.selectedUserId
                if (userId == null) { _uiState.value = state.copy(error = "Please search and select a teacher."); return }
                sendSingleNotification(userId, type, title, message)
            }
            ROLE_STUDENT -> {
                // selectedStudentId = studentId — resolve userId then notify
                val studentId = state.selectedStudentId
                if (studentId == null) { _uiState.value = state.copy(error = "Please search and select a student."); return }
                sendNotificationToStudent(studentId, type, title, message)
            }
            ROLE_PARENT -> {
                val studentId = state.selectedStudentId
                if (studentId == null) { _uiState.value = state.copy(error = "Please search and select a student."); return }
                sendNotificationToStudentParents(studentId, type, title, message)
            }
            else -> { _uiState.value = state.copy(error = "Please select a role.") }
        }
    }

    private fun sendSingleNotification(userId: Long, type: String, title: String, message: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, error = null)
            try {
                val notification = Notification(userId = userId, notificationType = type, title = title, message = message)
                val res = apiService.createNotification(notification)
                if (res.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isSubmitting = false, isSuccess = true)
                } else {
                    _uiState.value = _uiState.value.copy(isSubmitting = false, error = "Failed to send notification.")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSubmitting = false, error = "Network error: ${e.localizedMessage ?: "Unable to connect."}")
            }
        }
    }

    private fun sendNotificationToStudent(studentId: Long, type: String, title: String, message: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, error = null)
            try {
                val studentRes = apiService.getStudent(studentId)
                if (!studentRes.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isSubmitting = false, error = "Failed to resolve student details.")
                    return@launch
                }
                val userId = studentRes.body()?.data?.userId
                if (userId == null) {
                    _uiState.value = _uiState.value.copy(isSubmitting = false, error = "Student has no linked user account.")
                    return@launch
                }
                sendSingleNotification(userId, type, title, message)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSubmitting = false, error = "Network error: ${e.localizedMessage ?: "Unable to connect."}")
            }
        }
    }

    private fun sendNotificationToStudentParents(studentId: Long, type: String, title: String, message: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, error = null)
            try {
                val parentsRes = apiService.getParentsByStudentId(studentId)
                if (!parentsRes.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isSubmitting = false, error = "Failed to fetch parents for selected student.")
                    return@launch
                }
                val parents = parentsRes.body()?.data ?: emptyList()
                if (parents.isEmpty()) {
                    _uiState.value = _uiState.value.copy(isSubmitting = false, error = "No parents linked to this student.")
                    return@launch
                }
                val notifications = parents.map { parent ->
                    Notification(userId = parent.userId, notificationType = type, title = title, message = message)
                }
                val res = apiService.createBulkNotifications(BulkNotificationsRequest(notifications = notifications))
                if (res.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isSubmitting = false, isSuccess = true)
                } else {
                    _uiState.value = _uiState.value.copy(isSubmitting = false, error = "Failed to send notifications to parents.")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSubmitting = false, error = "Network error: ${e.localizedMessage ?: "Unable to connect."}")
            }
        }
    }

    private fun sendBroadcastNotification(isGlobal: Boolean, roleIds: List<Int>, type: String, title: String, message: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, error = null)
            try {
                val notification = Notification(notificationType = type, title = title, message = message)
                val request = com.school.gdsportal.data.remote.BroadcastNotificationRequest(
                    global = isGlobal,
                    targetRoleIds = roleIds,
                    notification = notification
                )
                val res = apiService.broadcastNotification(request)
                if (res.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isSubmitting = false, isSuccess = true)
                } else {
                    _uiState.value = _uiState.value.copy(isSubmitting = false, error = "Failed to send broadcast notifications.")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSubmitting = false, error = "Network error: ${e.localizedMessage ?: "Unable to connect."}")
            }
        }
    }

    // ─── Util ─────────────────────────────────────────────────────────────────

    fun dismissError() { _uiState.value = _uiState.value.copy(error = null) }
    fun resetSuccess() { _uiState.value = _uiState.value.copy(isSuccess = false) }

    class Factory(private val apiService: ApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NotificationCreateViewModel(apiService) as T
        }
    }
}
