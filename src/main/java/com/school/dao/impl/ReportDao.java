package com.school.dao.impl;

import java.util.List;
import java.util.Map;

public interface ReportDao {
    // These methods return data sets (like Maps or custom DTOs) by running complex JOINs
    Map<String, Object> getStudentPerformanceReport(long studentId, int academicYearId);
    Map<String, Object> getTeacherAttendanceReport(long teacherId, int month, int year);
    List<Map<String, Object>> getClassAttendanceReport(int sectionId, int month, int year);
    Map<String, Object> getTeacherPerformanceReport(long teacherId, int academicYearId);
    List<Map<String, Object>> getExaminationReport(long examinationId);
    Map<String, Object> getStudentAttendanceSummary(long studentId, int academicYearId);
}