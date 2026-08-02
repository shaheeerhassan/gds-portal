package com.school.dao.impl;

import com.school.model.TeacherAttendance;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface TeacherAttendanceDao {
    boolean insertTeacherAttendance(List<TeacherAttendance> attendanceRecords);
    boolean updateTeacherAttendance(long attendanceId, TeacherAttendance.Status status);
    boolean updateTeacherCheckIn(long attendanceId, LocalTime checkInTime);
    boolean updateTeacherCheckOut(long attendanceId, LocalTime checkOutTime);

    List<TeacherAttendance> getTeacherAttendanceByDate(LocalDate date);
    List<TeacherAttendance> getTeacherAttendanceByTeacherId(long teacherId, LocalDate startDate, LocalDate endDate);

    TeacherAttendance getTeacherAttendanceById(long attendanceId);
}
