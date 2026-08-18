package com.school.gdsportal.ui.admin.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.Administrator
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    // Loading / error
    val isLoading: Boolean = true,
    val error: String? = null,
    // Core user data
    val userId: Long = 0L,
    val email: String = "",
    val roleId: Int = 0,
    // Admin-specific data
    val adminId: Long = 0L,
    val employeeId: String = "",
    // Edit mode
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saveSuccess: Boolean = false,
    // Shared display / editable fields
    val firstName: String = "",
    val lastName: String = "",
    val username: String = "",
    val phone: String = "",
    // Edit form working copies
    val editFirstName: String = "",
    val editLastName: String = "",
    val editUsername: String = "",
    val editPhone: String = ""
) {
    val fullName: String get() = "$firstName $lastName".trim()
    val initials: String get() {
        val f = firstName.firstOrNull()?.uppercaseChar() ?: ""
        val l = lastName.firstOrNull()?.uppercaseChar() ?: ""
        return "$f$l".ifBlank { email.firstOrNull()?.uppercaseChar()?.toString() ?: "?" }
    }
    val isFormValid: Boolean
        get() = editFirstName.isNotBlank() && editLastName.isNotBlank() && editUsername.isNotBlank()
}

class ProfileViewModel(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // Step 1: get current user (userId, email, roleId, username)
                val userResp = apiService.getCurrentUser()
                if (!userResp.isSuccessful || userResp.body()?.data == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load user info.") }
                    return@launch
                }
                val user = userResp.body()!!.data!!

                // Step 2: get administrator record matching this userId
                val adminResp = apiService.getAdministrators()
                if (!adminResp.isSuccessful || adminResp.body()?.data == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load administrator details.") }
                    return@launch
                }
                val admin: Administrator? = adminResp.body()!!.data!!.find { it.userId == user.userId }
                if (admin == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Administrator record not found.") }
                    return@launch
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        userId = user.userId,
                        email = user.email,
                        roleId = user.roleId ?: 1,
                        username = user.username ?: "",
                        adminId = admin.adminId,
                        employeeId = admin.employeeId,
                        firstName = admin.firstName,
                        lastName = admin.lastName,
                        phone = admin.phone
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Network error. Please try again.") }
            }
        }
    }

    fun startEditing() {
        val s = _uiState.value
        _uiState.update {
            it.copy(
                isEditing = true,
                saveError = null,
                editFirstName = s.firstName,
                editLastName = s.lastName,
                editUsername = s.username,
                editPhone = s.phone
            )
        }
    }

    fun cancelEditing() {
        _uiState.update { it.copy(isEditing = false, saveError = null) }
    }

    fun updateEditField(field: String, value: String) {
        _uiState.update { s ->
            when (field) {
                "firstName" -> s.copy(editFirstName = value)
                "lastName"  -> s.copy(editLastName = value)
                "username"  -> s.copy(editUsername = value)
                "phone"     -> s.copy(editPhone = value)
                else        -> s
            }
        }
    }

    fun saveProfile() {
        val s = _uiState.value
        if (!s.isFormValid) {
            _uiState.update { it.copy(saveError = "First name, last name, and username are required.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null) }
            try {
                // Update administrator record (name, phone, employeeId stays same)
                val adminRequest = Administrator(
                    adminId = s.adminId,
                    userId = s.userId,
                    employeeId = s.employeeId,
                    firstName = s.editFirstName.trim(),
                    lastName = s.editLastName.trim(),
                    phone = s.editPhone.trim()
                )
                val adminResp = apiService.updateAdministrator(s.adminId, adminRequest)
                if (!adminResp.isSuccessful) {
                    val msg = parseError(adminResp.errorBody()?.string(), "Failed to save profile.")
                    _uiState.update { it.copy(isSaving = false, saveError = msg) }
                    return@launch
                }

                // Update username if it changed (PUT /api/users/{userId})
                if (s.editUsername.trim() != s.username) {
                    val userRequest = com.school.gdsportal.data.remote.User(
                        userId = s.userId,
                        username = s.editUsername.trim(),
                        email = s.email,
                        firstName = s.editFirstName.trim(),
                        lastName = s.editLastName.trim()
                    )
                    val userResp = apiService.updateUser(s.userId, userRequest)
                    if (!userResp.isSuccessful) {
                        val msg = parseError(userResp.errorBody()?.string(), "Failed to update username.")
                        _uiState.update { it.copy(isSaving = false, saveError = msg) }
                        return@launch
                    }
                }

                // Reflect changes locally
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        isEditing = false,
                        saveSuccess = true,
                        firstName = it.editFirstName.trim(),
                        lastName = it.editLastName.trim(),
                        username = it.editUsername.trim(),
                        phone = it.editPhone.trim()
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = "Network error. Please try again.") }
            }
        }
    }

    fun dismissSaveError() {
        _uiState.update { it.copy(saveError = null) }
    }

    fun dismissSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    private fun parseError(body: String?, fallback: String): String {
        return try {
            if (body != null) org.json.JSONObject(body).optString("message", fallback) else fallback
        } catch (e: Exception) {
            fallback
        }
    }

    companion object {
        fun provideFactory(apiService: ApiService): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    ProfileViewModel(apiService) as T
            }
    }
}
