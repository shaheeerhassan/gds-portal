package com.school.gdsportal.ui.admin.parents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.remote.ParentDirectoryDTO
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ParentsDirectoryUiState(
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val error: String? = null,
    val parents: List<ParentDirectoryDTO> = emptyList(),
    val totalParents: Int = 0,
    val currentPage: Int = 0,
    val hasNextPage: Boolean = false,
    val isFetchingNextPage: Boolean = false
)

class ParentsDirectoryViewModel(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ParentsDirectoryUiState())
    val uiState: StateFlow<ParentsDirectoryUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadData(isDebounced = false)
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        loadData(isDebounced = true)
    }

    fun retry() {
        loadData(isDebounced = false, isLoadMore = false)
    }

    fun loadNextPage() {
        val state = _uiState.value
        if (!state.hasNextPage || state.isFetchingNextPage || state.isLoading) return
        loadData(isDebounced = false, isLoadMore = true)
    }

    private fun loadData(isDebounced: Boolean, isLoadMore: Boolean = false) {
        if (!isLoadMore) {
            searchJob?.cancel()
        }
        
        searchJob = viewModelScope.launch {
            if (isDebounced) {
                delay(500)
            }
            
            val state = _uiState.value
            val targetPage = if (isLoadMore) state.currentPage + 1 else 0

            if (isLoadMore) {
                _uiState.update { it.copy(isFetchingNextPage = true, error = null) }
            } else {
                _uiState.update { it.copy(isLoading = true, error = null) }
            }
            
            try {
                val currentQuery = state.searchQuery.takeIf { it.isNotBlank() }
                
                val response = apiService.getParentsDirectory(
                    query = currentQuery,
                    page = targetPage,
                    size = 20
                )
                
                if (response.isSuccessful) {
                    val paginatedResponse = response.body()?.data
                    val newItems = paginatedResponse?.content ?: emptyList()
                    _uiState.update {
                        val updatedList = if (isLoadMore) {
                            (it.parents + newItems).distinctBy { parent -> parent.parentId }
                        } else {
                            newItems
                        }
                        
                        it.copy(
                            isLoading = false,
                            isFetchingNextPage = false,
                            parents = updatedList,
                            totalParents = paginatedResponse?.totalElements ?: 0,
                            currentPage = paginatedResponse?.currentPage ?: 0,
                            hasNextPage = paginatedResponse?.hasNext ?: false
                        )
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            isFetchingNextPage = false,
                            error = "Failed to load directory. Server returned ${response.code()}"
                        ) 
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        isFetchingNextPage = false,
                        error = e.localizedMessage ?: "An unexpected error occurred"
                    ) 
                }
            }
        }
    }
}

class ParentsDirectoryViewModelFactory(private val apiService: ApiService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ParentsDirectoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ParentsDirectoryViewModel(apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
