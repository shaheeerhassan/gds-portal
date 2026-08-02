package com.school.model;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TeacherAttendance {
    public enum Status { PRESENT, ABSENT, LATE, LEAVE }
    private long attendanceId;
    private long teacherId;
    private LocalDate attendanceDate;
    private Status status;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private LocalDateTime markedAt;
}
