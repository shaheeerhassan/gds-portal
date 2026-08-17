package com.school.gdsportal.data.remote.dto

data class ClassAttendanceReportItem(
    val attendance_date: String,
    val student_id: Long,
    val student_name: String,
    val status: String,
    val remarks: String?
)

data class ClassAttendanceReportSummary(
    val present_records: Int,
    val absent_records: Int,
    val late_records: Int,
    val leave_records: Int,
    val total_records: Int,
    val attendance_rate: Double?
)

data class ClassAttendanceReportData(
    val section_id: Int,
    val month: Int,
    val year: Int,
    val results: List<ClassAttendanceReportItem>,
    val summary: ClassAttendanceReportSummary
)
