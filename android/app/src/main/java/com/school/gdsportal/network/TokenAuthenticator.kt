package com.school.gdsportal.network

import com.school.gdsportal.data.local.TokenManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import org.json.JSONObject

class TokenAuthenticator(
    private val tokenManager: TokenManager
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Prevent infinite loops if the refresh token itself is rejected (returns 401)
        if (response.request.url.encodedPath.contains("api/auth/refresh") ||
            response.request.url.encodedPath.contains("api/auth/login")
        ) {
            return null
        }

        return synchronized(this) {
            // 1. Get the current token from storage
            val currentToken = runBlocking { tokenManager.tokenFlow.firstOrNull() }
            val requestToken = response.request.header("Authorization")?.removePrefix("Bearer ")

            // 2. If the current token is different from the one that failed,
            // another thread already refreshed it. Retry with the new token.
            if (currentToken != null && currentToken != requestToken) {
                return@synchronized response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            }

            // 3. Otherwise, we need to refresh the token
            val refreshToken = runBlocking { tokenManager.refreshTokenFlow.firstOrNull() }
            if (refreshToken.isNullOrEmpty()) {
                // No refresh token available, session is definitely expired
                runBlocking { tokenManager.triggerSessionExpired() }
                return@synchronized null
            }

            // 4. Perform the refresh call synchronously
            val refreshRequestJson = JSONObject().apply {
                put("refreshToken", refreshToken)
            }.toString()

            val requestBody = refreshRequestJson.toRequestBody("application/json".toMediaType())

            // Construct the URL dynamically using the same host as the failed request
            val url = response.request.url.newBuilder()
                .encodedPath("/gds-portal/api/auth/refresh")
                .build()

            val refreshRequest = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            // Use a separate lightweight client to avoid interceptor recursion
            val refreshClient = OkHttpClient.Builder().build()

            try {
                val refreshResponse = refreshClient.newCall(refreshRequest).execute()
                if (refreshResponse.isSuccessful) {
                    val responseBody = refreshResponse.body?.string()
                    responseBody?.let {
                        val jsonObject = JSONObject(it)
                        val success = jsonObject.optBoolean("success", false)
                        if (success) {
                            val data = jsonObject.optJSONObject("data")
                            val newToken = data?.optString("token")
                            val newRefreshToken = data?.optString("refreshToken")

                            if (!newToken.isNullOrEmpty() && !newRefreshToken.isNullOrEmpty()) {
                                // Save new tokens
                                runBlocking {
                                    tokenManager.saveToken(newToken)
                                    tokenManager.saveRefreshToken(newRefreshToken)
                                }
                                // Retry the original request with the new token
                                return@synchronized response.request.newBuilder()
                                    .header("Authorization", "Bearer $newToken")
                                    .build()
                            }
                        }
                    }
                }

                // If we reach here, refresh failed (e.g. 401 or invalid response)
                runBlocking { tokenManager.triggerSessionExpired() }
                return@synchronized null

            } catch (e: Exception) {
                // Network error during refresh (e.g. timeout, no internet)
                // We don't trigger session expired here to allow retrying later when network recovers
                return@synchronized null
            }
        }
    }
}
