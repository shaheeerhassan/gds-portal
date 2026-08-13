package com.school.gdsportal.di

import android.content.Context
import com.school.gdsportal.data.local.TokenManager
import com.school.gdsportal.network.ApiService
import com.school.gdsportal.network.AuthInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Manual Dependency Injection Container.
 * Replaces Hilt to avoid complex Kotlin/AGP 9 annotation processing errors.
 */
class AppContainer(private val context: Context) {
    
    // BASE_URL is for the Android Emulator to reach localhost. 
    private val BASE_URL = "http://10.0.2.2:8080/gds-portal/"

    val tokenManager: TokenManager by lazy {
        TokenManager(context)
    }

    private val authInterceptor: AuthInterceptor by lazy {
        AuthInterceptor(tokenManager)
    }

    private val loggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
