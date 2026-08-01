package com.school.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class TeacherAttendance {
    public enum Status { PRESENT, ABSENT, LATE, LEAVE }
    private long attendanceId;
    private long teacherId;
    private LocalDate attendanceDate;
    private Status status;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private LocalDateTime markedAt;

    public TeacherAttendance() {}
    public TeacherAttendance(long attendanceId, long teacherId, LocalDate attendanceDate, Status status, LocalTime checkInTime, LocalTime checkOutTime, LocalDateTime markedAt) {
        this.attendanceId = attendanceId; this.teacherId = teacherId; this.attendanceDate = attendanceDate; this.status = status; this.checkInTime = checkInTime; this.checkOutTime = checkOutTime; this.markedAt = markedAt;
    }

    public long getAttendanceId() { return attendanceId; }
    public void setAttendanceId(long attendanceId) { this.attendanceId = attendanceId; }
    public long getTeacherId() { return teacherId; }
    public void setTeacherId(long teacherId) { this.teacherId = teacherId; }
    public LocalDate getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public LocalTime getCheckInTime() { return checkInTime; }
    public void setCheckInTime(LocalTime checkInTime) { this.checkInTime = checkInTime; }
    public LocalTime getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(LocalTime checkOutTime) { this.checkOutTime = checkOutTime; }
    public LocalDateTime getMarkedAt() { return markedAt; }
    public void setMarkedAt(LocalDateTime markedAt) { this.markedAt = markedAt; }
}
