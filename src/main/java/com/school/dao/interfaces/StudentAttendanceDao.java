package com.school.dao.interfaces;

import com.school.model.StudentAttendance;

import java.time.LocalDate;
import java.util.List;

public interface StudentAttendanceDao {
        boolean insertStudentAttendance(List<StudentAttendance> attendanceRecords);
        boolean updateStudentAttendance(long attendanceId, StudentAttendance.Status status);
        boolean lockAttendanceForDate(LocalDate date, int sectionId);

        List<StudentAttendance> getStudentAttendanceBySectionAndDate(int sectionId, LocalDate date);
        List<StudentAttendance> getStudentAttendanceByStudentId(long studentId, LocalDate startDate, LocalDate endDate);

        StudentAttendance getStudentAttendanceById(long attendanceId);
}
