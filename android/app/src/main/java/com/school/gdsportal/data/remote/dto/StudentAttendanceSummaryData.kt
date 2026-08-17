package com.school.gdsportal.data.remote.dto

data class StudentAttendanceSummaryResponse(
    val success: Boolean,
    val message: String,
    val data: StudentAttendanceSummaryData,
    val status: Int
)

data class StudentAttendanceSummaryData(
    val student_id: Long,
    val academic_year_id: Int,
    val present_days: Int,
    val absent_days: Int,
    val late_days: Int,
    val leave_days: Int,
    val total_days: Int
)
