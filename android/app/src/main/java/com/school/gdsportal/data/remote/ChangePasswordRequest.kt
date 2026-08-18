package com.school.gdsportal.data.remote

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)
