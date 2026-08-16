package com.school.gdsportal.data.remote.dto

data class TeacherAttendanceReportData(
    val teacher: TeacherAttendanceReportTeacher,
    val results: List<TeacherAttendanceReportItem>,
    val present_days: Int,
    val absent_days: Int,
    val late_days: Int,
    val leave_days: Int,
    val total_days: Int,
    val attendance_rate: Double,
    val absence_rate: Double
)

data class TeacherAttendanceReportTeacher(
    val teacher_id: Long,
    val teacher_name: String
)

data class TeacherAttendanceReportItem(
    val attendance_date: String,
    val status: String
)
