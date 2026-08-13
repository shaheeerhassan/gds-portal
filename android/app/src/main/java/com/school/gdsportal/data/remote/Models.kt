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
    val academicYearId: Int,
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

data class SchoolClass(
    val classId: Int,
    val className: String,
    val numericLevel: Int,
    val description: String?
)

data class Section(
    val sectionId: Int,
    val classId: Int,
    val academicYearId: Int,
    val sectionName: String,
    val capacity: Int?,
    val roomNumber: String?
)

data class Student(
    val studentId: Long,
    val userId: Long,
    val registrationNumber: String,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: String?,
    val gender: String?,
    val admissionDate: String?,
    val isActive: Boolean
)

data class Enrollment(
    val studentClassId: Long,
    val studentId: Long,
    val classId: Int,
    val sectionId: Int,
    val academicYearId: Int,
    val rollNumber: String?,
    val enrollmentDate: String?,
    val isActive: Boolean
)
