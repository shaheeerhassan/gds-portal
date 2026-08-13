package com.school.gdsportal.data.remote

data class User(
    val id: Int,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: Role? = null
)

data class Role(
    val id: Int,
    val name: String
)

data class AcademicYear(
    val id: Int,
    val yearName: String,
    val startDate: String,
    val endDate: String,
    val isCurrent: Boolean
)

data class Announcement(
    val id: Int,
    val title: String,
    val content: String,
    val createdAt: String,
    val targetRole: String? = null,
    val active: Boolean = true
)

data class TeacherAttendance(
    val id: Int,
    val teacherId: Int,
    val date: String,
    val status: String,
    val checkInTime: String? = null,
    val checkOutTime: String? = null
)

data class NotificationCountResponse(
    val count: Int
)
