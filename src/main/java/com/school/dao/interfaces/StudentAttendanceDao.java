package com.school.dao.interfaces;

import com.school.model.StudentAttendance;

import java.time.LocalDate;
import java.util.List;

public interface StudentAttendanceDao {
        boolean insertStudentAttendance(List<StudentAttendance> attendanceRecords);
        boolean updateStudentAttendance(long attendanceId, StudentAttendance.Status status);
        boolean lockAttendanceForDate(LocalDate date, int sectionId);
        boolean existsAttendance(long studentClassId, LocalDate date, Integer periodId);
        boolean isAttendanceLocked(long studentClassId, LocalDate date);
        int countPresentStudents(LocalDate date);

        List<StudentAttendance> getStudentAttendanceBySectionAndDate(int sectionId, LocalDate date);
        List<StudentAttendance> getStudentAttendanceByStudentId(long studentId, LocalDate startDate, LocalDate endDate);

        StudentAttendance getStudentAttendanceById(long attendanceId);
}
