package com.school.dao.impl;

import com.school.model.StudentAttendance;
import com.school.model.TeacherAttendance;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AttendanceDao {
    boolean insertStudentAttendance(List<StudentAttendance> attendanceRecords);
    boolean insertTeacherAttendance(List<TeacherAttendance> attendanceRecords);

    List<StudentAttendance> getStudentAttendanceByDate(int sectionId, LocalDate date);
    List<StudentAttendance> getStudentAttendanceByDate(int sectionId, LocalDate date, int periodId);
    List<TeacherAttendance> getTeacherAttendanceByDate(LocalDate date);
    boolean updateTeacherAttendance(long attendanceId, TeacherAttendance.Status status);
    boolean updateTeacherCheckIn(long attendanceId, LocalTime checkInTime);
    boolean updateTeacherCheckOut(long attendanceId, LocalTime checkOutTime);
    List<StudentAttendance> getStudentAttendanceByStudent(long studentId, LocalDate startDate, LocalDate endDate);
    List<TeacherAttendance> getTeacherAttendanceByTeacher(long teacherId, LocalDate startDate, LocalDate endDate);
    boolean lockAttendanceForDate(LocalDate date, int sectionId);

    boolean updateStudentAttendance(long attendanceId, StudentAttendance.Status status);
}