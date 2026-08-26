package com.school.gdsportal.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.school.gdsportal.data.local.TokenManager
import com.school.gdsportal.data.remote.LoginRequest
import com.school.gdsportal.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val isSuccess: Boolean = false
)

class LoginViewModel(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, pass: String) {
        _uiState.update { it.copy(emailError = null, passwordError = null, error = null) }

        if (email.isBlank()) {
            _uiState.update { it.copy(emailError = "Email is required") }
        }
        if (pass.isBlank()) {
            _uiState.update { it.copy(passwordError = "Password is required") }
        }
        if (email.isBlank() || pass.isBlank()) return

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val response = apiService.login(LoginRequest(email, pass))
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null && responseBody.success) {
                    val token = responseBody.data?.token
                    val refreshToken = responseBody.data?.refreshToken
                    val role = responseBody.data?.profile?.role?.roleName
                    val profileWrapper = responseBody.data?.profile

                    if (token != null) {
                        tokenManager.saveToken(token)
                        if (refreshToken != null) tokenManager.saveRefreshToken(refreshToken)
                        if (role != null) tokenManager.saveRole(role)

                        // Extract the nested profile data using Gson
                        if (profileWrapper != null) {
                            try {
                                val gson = com.google.gson.Gson()

                                // 1. Convert the outer ProfileWrapper to a generic JsonObject
                                val wrapperJson = gson.toJson(profileWrapper)
                                val jsonObject = gson.fromJson(wrapperJson, com.google.gson.JsonObject::class.java)

                                // 2. Extract the nested "profile" object containing firstName/lastName
                                val innerProfile = jsonObject.getAsJsonObject("profile")

                                if (innerProfile != null) {
                                    // 3. Map the inner object to your User data class
                                    val user = gson.fromJson(innerProfile, com.school.gdsportal.data.remote.User::class.java)
                                    tokenManager.saveUserProfile(user)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace() // Fallback if parsing fails, but login will still succeed
                            }
                        }

                        _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = "Invalid response from server.") }
                    }
                } else {
                    val errorMsg = responseBody?.message ?: "Invalid email or password."
                    _uiState.update { it.copy(isLoading = false, error = errorMsg) }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Unable to connect to the server. Please try again later."
                    )
                }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
    
    // Factory for manual DI
    companion object {
        fun provideFactory(
            apiService: ApiService,
            tokenManager: TokenManager
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LoginViewModel(apiService, tokenManager) as T
            }
        }
    }
}
