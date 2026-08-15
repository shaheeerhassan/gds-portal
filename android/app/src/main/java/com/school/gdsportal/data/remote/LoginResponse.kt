package com.school.gdsportal.data.remote

data class LoginResponse(
    val token: String,
    val role: String,
    val userId: Int
)
