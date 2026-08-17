package com.school.gdsportal.data.remote.dto

data class ExaminationReportResponse(
    val success: Boolean,
    val message: String,
    val data: ExaminationReportData,
    val status: Int
)

data class ExaminationReportData(
    val examination_id: Long,
    val results: List<ExaminationReportResult>,
    val total_students: Int,
    val average_marks: Double?,
    val highest_marks: Double?,
    val lowest_marks: Double?,
    val passing_marks: Double,
    val passed_students: Int,
    val pass_percentage: Double?
)

data class ExaminationReportResult(
    val subject_name: String,
    val exam_name: String,
    val max_marks: Double,
    val passing_marks: Double,
    val student_id: Long,
    val student_name: String,
    val marks_obtained: Double?,
    val grade: String?,
    val percentage: Double?
)
