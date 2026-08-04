package com.school.service.interfaces;

import com.school.model.TeacherAttendance;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface TeacherAttendanceService {
    void markAttendance(List<TeacherAttendance> attendanceRecords);
    TeacherAttendance getTeacherAttendanceById(long attendanceId);
    List<TeacherAttendance> getTeacherAttendanceByDate(LocalDate date);
    List<TeacherAttendance> getTeacherAttendanceByTeacherId(long teacherId, LocalDate startDate, LocalDate endDate);
    void updateTeacherAttendance(long attendanceId, TeacherAttendance.Status status);
    void checkIn(long attendanceId, LocalTime checkInTime);
    void checkOut(long attendanceId, LocalTime checkOutTime);
}
