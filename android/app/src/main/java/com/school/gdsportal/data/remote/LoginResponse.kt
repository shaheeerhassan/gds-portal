package com.school.gdsportal.data.remote

data class LoginData(
    val token: String,
    val expiresIn: Long,
    val refreshToken: String,
    val user: UserSummary,
    val profile: ProfileWrapper
)

data class UserSummary(
    val userId: Long,
    val roleId: Int,
    val username: String,
    val email: String,
    val profilePictureUrl: String?,
    val lastLoginAt: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val active: Boolean
)

data class ProfileWrapper(
    val user: UserSummary,
    val role: RoleData,
    val profile: Any? // Can remain flexible or typed depending on requirements
)

data class RoleData(
    val roleId: Int,
    val roleName: String,
    val description: String?
)