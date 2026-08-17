package com.school.gdsportal.data.remote.dto

data class TeacherPerformanceReportResponse(
    val success: Boolean,
    val message: String,
    val data: TeacherPerformanceReportData,
    val status: Int
)

data class TeacherPerformanceReportData(
    val results: List<TeacherPerformanceResult>,
    val teacher_id: Long,
    val academic_year_id: Long,
    val total_assignments: Int,
    val total_submissions: Int,
    val pending_submissions_total: Int,
    val graded_assignments: Int,
    val overall_average_marks: Double?
)

data class TeacherPerformanceResult(
    val assignment_title: String,
    val section_id: Long,
    val subject_name: String,
    val total_submissions: Int,
    val average_marks: Double?,
    val pending_submissions: Int
)
