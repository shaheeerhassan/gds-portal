package com.school.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class StudentAttendance {
    public enum Status { PRESENT, ABSENT, LATE, LEAVE }
    private long attendanceId;
    private long studentClassId;
    private LocalDate attendanceDate;
    private Status status;
    private int periodId;
    private long markedBy;
    private LocalDateTime markedAt;
    private boolean isLocked;
    private String remarks;

    public StudentAttendance() {}
    public StudentAttendance(long attendanceId, long studentClassId, LocalDate attendanceDate, Status status, int periodId, long markedBy, LocalDateTime markedAt, boolean isLocked, String remarks) {
        this.attendanceId = attendanceId; this.studentClassId = studentClassId; this.attendanceDate = attendanceDate; this.status = status; this.periodId = periodId; this.markedBy = markedBy; this.markedAt = markedAt; this.isLocked = isLocked; this.remarks = remarks;
    }

    public long getAttendanceId() { return attendanceId; }
    public void setAttendanceId(long attendanceId) { this.attendanceId = attendanceId; }
    public long getStudentClassId() { return studentClassId; }
    public void setStudentClassId(long studentClassId) { this.studentClassId = studentClassId; }
    public LocalDate getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public int getPeriodId() { return periodId; }
    public void setPeriodId(int periodId) { this.periodId = periodId; }
    public long getMarkedBy() { return markedBy; }
    public void setMarkedBy(long markedBy) { this.markedBy = markedBy; }
    public LocalDateTime getMarkedAt() { return markedAt; }
    public void setMarkedAt(LocalDateTime markedAt) { this.markedAt = markedAt; }
    public boolean isLocked() { return isLocked; }
    public void setLocked(boolean locked) { isLocked = locked; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}

