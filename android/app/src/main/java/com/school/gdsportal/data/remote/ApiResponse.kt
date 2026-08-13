package com.school.gdsportal.data.remote

data class ApiResponse<T>(
    val success: Boolean,
    val message: String?,
    val data: T?,
    val errors: List<String>?,
    val status: Int
)
