package com.school.model;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Examination {
    public enum Status { SCHEDULED, ONGOING, COMPLETED, PUBLISHED }
    private long examinationId;
    private String examName;
    private int subjectId;
    private int sectionId;
    private int academicYearId;
    private LocalDate examDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private double maxMarks;
    private Double passingMarks;
    private Status status;
    private long createdBy;
    private LocalDateTime createdAt;
}
