package com.school.dao.interfaces;

import com.school.model.TeacherAttendance;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface TeacherAttendanceDao {
    boolean insertTeacherAttendance(List<TeacherAttendance> attendanceRecords);
    boolean updateTeacherAttendance(long attendanceId, TeacherAttendance.Status status);
    boolean updateTeacherCheckIn(long attendanceId, LocalTime checkInTime);
    boolean updateTeacherCheckOut(long attendanceId, LocalTime checkOutTime);
    boolean existsAttendance(long teacherId, LocalDate date);

    List<TeacherAttendance> getTeacherAttendanceByDate(LocalDate date);
    List<TeacherAttendance> getTeacherAttendanceByTeacherId(long teacherId, LocalDate startDate, LocalDate endDate);

    TeacherAttendance getTeacherAttendanceById(long attendanceId);
}
