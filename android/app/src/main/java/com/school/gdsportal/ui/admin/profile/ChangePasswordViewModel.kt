package com.school.gdsportal.ui.admin.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.ChangePasswordRequest
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ── Password rule constants ───────────────────────────────────────────────────
private val SPECIAL_CHARS = Regex("[^A-Za-z0-9]")

fun validatePassword(password: String): List<String> {
    val errors = mutableListOf<String>()
    if (password.length < 8)              errors += "At least 8 characters"
    if (!password.any { it.isLowerCase() }) errors += "At least 1 lowercase letter"
    if (!password.any { it.isUpperCase() }) errors += "At least 1 uppercase letter"
    if (!SPECIAL_CHARS.containsMatchIn(password)) errors += "At least 1 special character"
    return errors
}

// ── UI State ──────────────────────────────────────────────────────────────────
data class ChangePasswordUiState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    // Live validation feedback
    val newPasswordErrors: List<String> = emptyList(),
    val confirmMismatch: Boolean = false,
    // Form state
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
) {
    val isFormValid: Boolean
        get() = currentPassword.isNotBlank()
                && newPasswordErrors.isEmpty()
                && newPassword.isNotEmpty()
                && !confirmMismatch
                && confirmPassword.isNotEmpty()
}

// ── ViewModel ─────────────────────────────────────────────────────────────────
class ChangePasswordViewModel(private val apiService: ApiService) : ViewModel() {

    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()

    fun updateCurrentPassword(value: String) {
        _uiState.update { it.copy(currentPassword = value, error = null) }
    }

    fun updateNewPassword(value: String) {
        val errors = validatePassword(value)
        val mismatch = _uiState.value.confirmPassword.isNotEmpty()
                && _uiState.value.confirmPassword != value
        _uiState.update {
            it.copy(newPassword = value, newPasswordErrors = errors, confirmMismatch = mismatch, error = null)
        }
    }

    fun updateConfirmPassword(value: String) {
        val mismatch = value.isNotEmpty() && value != _uiState.value.newPassword
        _uiState.update { it.copy(confirmPassword = value, confirmMismatch = mismatch, error = null) }
    }

    fun submit() {
        val state = _uiState.value
        if (!state.isFormValid) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            try {
                val response = apiService.changePassword(
                    ChangePasswordRequest(
                        currentPassword = state.currentPassword,
                        newPassword = state.newPassword
                    )
                )
                if (response.isSuccessful) {
                    _uiState.update { it.copy(isSubmitting = false, success = true) }
                } else {
                    val msg = when (response.code()) {
                        401 -> "Current password is incorrect."
                        400 -> "Invalid request. Please check your input."
                        else -> "Failed to change password (${response.code()})."
                    }
                    _uiState.update { it.copy(isSubmitting = false, error = msg) }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSubmitting = false, error = "Network error: ${e.message}")
                }
            }
        }
    }

    fun dismissError() = _uiState.update { it.copy(error = null) }

    companion object {
        fun provideFactory(apiService: ApiService): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    ChangePasswordViewModel(apiService) as T
            }
    }
}
