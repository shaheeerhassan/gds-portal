package com.school.model;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Assignment {
    public enum Status { CREATED, PUBLISHED }
    private long assignmentId;
    private long teacherId;
    private int subjectId;
    private int sectionId;
    private String title;
    private String description;
    private double maxMarks;
    private LocalDateTime deadline;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

