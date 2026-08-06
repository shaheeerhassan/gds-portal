package com.school.service.interfaces;

import java.util.Map;

public interface ReportService {
    Map<String, Object> getStudentPerformanceReport(long studentId, int academicYearId);
    Map<String, Object> getTeacherAttendanceReport(long teacherId, int month, int year);
    Map<String, Object> getClassAttendanceReport(int sectionId, int month, int year);
    Map<String, Object> getTeacherPerformanceReport(long teacherId, int academicYearId);
    Map<String, Object> getExaminationReport(long examinationId);
    Map<String, Object> getStudentAttendanceSummary(long studentId, int academicYearId);
}
