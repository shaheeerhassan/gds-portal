package com.school.gdsportal.data.remote.dto

data class StudentDirectoryDTO(
    val studentId: Long,
    val registrationNumber: String,
    val firstName: String,
    val lastName: String,
    val isActive: Boolean,
    
    // Enrollment Data (nullable if not enrolled)
    val academicYearId: Int?,
    val academicYearName: String?,
    val classId: Int?,
    val className: String?,
    val sectionId: Int?,
    val sectionName: String?,
    val rollNumber: String?
)
