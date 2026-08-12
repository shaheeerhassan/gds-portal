package com.school.service.interfaces;

import com.school.model.StudentAttendance;

import java.time.LocalDate;
import java.util.List;

public interface StudentAttendanceService {
    void markAttendance(List<StudentAttendance> attendanceRecords);
    StudentAttendance getStudentAttendanceById(long attendanceId);
    List<StudentAttendance> getStudentAttendanceBySectionAndDate(int sectionId, LocalDate date);
    List<StudentAttendance> getStudentAttendanceByStudentId(long studentId, LocalDate startDate, LocalDate endDate);
    void updateStudentAttendance(long attendanceId, StudentAttendance.Status status);
    void lockAttendanceForDate(LocalDate date, int sectionId);
    int getPresentStudentCountToday();
}
