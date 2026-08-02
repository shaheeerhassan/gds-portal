package com.school.model;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
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
}

