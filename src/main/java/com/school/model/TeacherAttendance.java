package com.school.model;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
    private LocalDateTime markedAt;
}
