package com.school.gdsportal.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.school.gdsportal.data.remote.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "gds_settings")

class TokenManager(context: Context) {
    private val dataStore = context.dataStore
    private val gson = Gson()

    private val _sessionExpiredEvent = MutableSharedFlow<Unit>()
    val sessionExpiredEvent = _sessionExpiredEvent.asSharedFlow()

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("jwt_refresh_token")
        private val ROLE_KEY = stringPreferencesKey("user_role")
        private val USER_PROFILE_KEY = stringPreferencesKey("user_profile")
    }

    val tokenFlow: Flow<String?> = dataStore.data.map { preferences ->
        preferences[TOKEN_KEY]
    }

    val refreshTokenFlow: Flow<String?> = dataStore.data.map { preferences ->
        preferences[REFRESH_TOKEN_KEY]
    }

    val roleFlow: Flow<String?> = dataStore.data.map { preferences ->
        preferences[ROLE_KEY]
    }

    suspend fun saveToken(token: String) {
        dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    suspend fun saveRefreshToken(refreshToken: String) {
        dataStore.edit { preferences ->
            preferences[REFRESH_TOKEN_KEY] = refreshToken
        }
    }

    suspend fun saveRole(role: String) {
        dataStore.edit { preferences ->
            preferences[ROLE_KEY] = role
        }
    }

    // Function to Save Complete Profile
    suspend fun saveUserProfile(user: User) {
        val userJson = gson.toJson(user)
        dataStore.edit { preferences ->
            preferences[USER_PROFILE_KEY] = userJson
        }
    }

    // Function to Retrieve Complete Profile Instantly
    suspend fun getUserProfile(): User? {
        val userJson = dataStore.data.map { it[USER_PROFILE_KEY] }.first()
        return if (userJson != null) {
            gson.fromJson(userJson, User::class.java)
        } else {
            null
        }
    }

    suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(REFRESH_TOKEN_KEY)
            preferences.remove(ROLE_KEY)
            preferences.remove(USER_PROFILE_KEY) // Clear this on logout
        }
    }

    suspend fun triggerSessionExpired() {
        clearSession()
        _sessionExpiredEvent.emit(Unit)
    }
}