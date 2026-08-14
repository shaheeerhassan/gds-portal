package com.school.gdsportal.ui.admin.students.parents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.*
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddParentUiState(
    val currentTab: Int = 0, // 0 = Search, 1 = Create
    
    // Search State
    val searchQuery: String = "",
    val searchResults: List<ParentDirectoryDTO> = emptyList(),
    val isSearching: Boolean = false,
    val searchError: String? = null,
    
    // Create Form State
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "",
    val occupation: String = "",
    val email: String = "",
    val username: String = "",
    val password: String = "",
    
    // Shared Relationship State
    val selectedParentId: Long? = null,
    val relationship: String = "FATHER",
    val isPrimaryContact: Boolean = false,
    
    // Common
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
    val success: Boolean = false
) {
    val isCreateFormValid: Boolean
        get() = firstName.isNotBlank() && lastName.isNotBlank() && email.isNotBlank() && username.isNotBlank() && password.isNotBlank()
}

class AddParentViewModel(
    private val studentId: Long,
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddParentUiState())
    val uiState: StateFlow<AddParentUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        // Load initial directory
        viewModelScope.launch {
            performSearch("")
        }
    }

    fun setTab(index: Int) {
        _uiState.update { it.copy(currentTab = index, submitError = null) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500) // Debounce
            performSearch(query)
        }
    }

    private suspend fun performSearch(query: String) {
        _uiState.update { it.copy(isSearching = true, searchError = null) }
        try {
            val response = apiService.getParentsDirectory(
                query = query.takeIf { it.isNotBlank() },
                page = 0,
                size = 50 // Fetch a good chunk for search
            )
            if (response.isSuccessful && response.body()?.data != null) {
                _uiState.update { 
                    it.copy(
                        isSearching = false,
                        searchResults = response.body()!!.data!!.content
                    )
                }
            } else {
                _uiState.update { it.copy(isSearching = false, searchError = "Failed to load parents") }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(isSearching = false, searchError = "Network error") }
        }
    }

    fun selectParentToLink(parentId: Long) {
        _uiState.update { it.copy(selectedParentId = parentId, submitError = null) }
    }

    fun clearSelectedParent() {
        _uiState.update { it.copy(selectedParentId = null) }
    }

    fun updateRelationship(rel: String) {
        _uiState.update { it.copy(relationship = rel) }
    }

    fun updatePrimaryContact(isPrimary: Boolean) {
        _uiState.update { it.copy(isPrimaryContact = isPrimary) }
    }

    fun updateCreateField(field: String, value: String) {
        _uiState.update { state ->
            when (field) {
                "firstName" -> state.copy(firstName = value)
                "lastName" -> state.copy(lastName = value)
                "phone" -> state.copy(phone = value)
                "occupation" -> state.copy(occupation = value)
                "email" -> state.copy(email = value)
                "username" -> state.copy(username = value)
                "password" -> state.copy(password = value)
                else -> state
            }
        }
    }

    fun submitLinkExisting() {
        val state = _uiState.value
        val parentId = state.selectedParentId ?: return
        linkParent(parentId, state.relationship, state.isPrimaryContact)
    }

    fun submitCreateNew() {
        val state = _uiState.value
        if (!state.isCreateFormValid) {
            _uiState.update { it.copy(submitError = "Please fill all required fields.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, submitError = null) }
            try {
                val request = CreateParentRequest(
                    email = state.email.trim(),
                    username = state.username.trim(),
                    password = state.password,
                    parent = ParentDetails(
                        firstName = state.firstName.trim(),
                        lastName = state.lastName.trim(),
                        phone = state.phone.trim().takeIf { it.isNotEmpty() },
                        occupation = state.occupation.trim().takeIf { it.isNotEmpty() }
                    )
                )
                
                val response = apiService.createParent(request)
                if (response.isSuccessful && response.body()?.data != null) {
                    val createdParent = response.body()!!.data!!
                    linkParent(createdParent.parentId, state.relationship, state.isPrimaryContact)
                } else {
                    _uiState.update { it.copy(isSubmitting = false, submitError = "Failed to create parent: ${response.message()}") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSubmitting = false, submitError = e.localizedMessage ?: "Network error creating parent") }
            }
        }
    }

    private fun linkParent(parentId: Long, relType: String, isPrimary: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, submitError = null) }
            try {
                val request = LinkParentRequest(
                    studentId = studentId,
                    parentId = parentId,
                    relationshipType = relType,
                    primaryContact = isPrimary
                )
                val response = apiService.linkParent(request)
                if (response.isSuccessful) {
                    _uiState.update { it.copy(isSubmitting = false, success = true) }
                } else {
                    _uiState.update { it.copy(isSubmitting = false, submitError = "Failed to link parent") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSubmitting = false, submitError = "Network error linking parent") }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(submitError = null) }
    }

    companion object {
        fun provideFactory(studentId: Long, apiService: ApiService): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AddParentViewModel(studentId, apiService) as T
            }
        }
    }
}
