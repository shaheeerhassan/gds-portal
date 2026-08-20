package com.school.gdsportal.ui.parent.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.Parent
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ParentProfileUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val parent: Parent? = null,
    val isEditing: Boolean = false,
    val editFirstName: String = "",
    val editLastName: String = "",
    val editPhone: String = "",
    val editOccupation: String = ""
)

class ParentProfileViewModel(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ParentProfileUiState())
    val uiState: StateFlow<ParentProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        _uiState.update { it.copy(isLoading = true, error = null, isEditing = false) }
        viewModelScope.launch {
            try {
                val response = apiService.getCurrentParent()
                if (response.isSuccessful) {
                    _uiState.update { it.copy(isLoading = false, parent = response.body()?.data) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load parent details.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Network error.") }
            }
        }
    }

    fun toggleEditMode() {
        val current = _uiState.value
        if (!current.isEditing && current.parent != null) {
            _uiState.update {
                it.copy(
                    isEditing = true,
                    editFirstName = current.parent.firstName,
                    editLastName = current.parent.lastName,
                    editPhone = current.parent.phone ?: "",
                    editOccupation = current.parent.occupation ?: ""
                )
            }
        } else {
            _uiState.update { it.copy(isEditing = false) }
        }
    }

    fun updateField(field: String, value: String) {
        _uiState.update {
            when (field) {
                "firstName" -> it.copy(editFirstName = value)
                "lastName" -> it.copy(editLastName = value)
                "phone" -> it.copy(editPhone = value)
                "occupation" -> it.copy(editOccupation = value)
                else -> it
            }
        }
    }

    fun saveProfile() {
        val current = _uiState.value
        val parent = current.parent ?: return

        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            try {
                val updatedParent = parent.copy(
                    firstName = current.editFirstName,
                    lastName = current.editLastName,
                    phone = current.editPhone.ifBlank { null },
                    occupation = current.editOccupation.ifBlank { null }
                )

                val response = apiService.updateParent(parent.parentId, updatedParent)

                if (response.isSuccessful) {
                    _uiState.update {
                        it.copy(isSaving = false, isEditing = false, parent = updatedParent)
                    }
                } else {
                    _uiState.update { it.copy(isSaving = false, error = "Failed to update profile.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = "Network error while saving.") }
            }
        }
    }

    companion object {
        fun provideFactory(apiService: ApiService): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ParentProfileViewModel(apiService) as T
                }
            }
    }
}