package com.school.model;

import lombok.*;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Submission {
    public enum Status { SUBMITTED, LATE, GRADED }
    private long submissionId;
    private long assignmentId;
    private long studentId;
    private LocalDateTime submittedAt;
    private String fileUrl;
    private Status status;
    private Double marksAwarded; // wrapper class for nullable
    private String feedback;
    private Long gradedBy; // wrapper class for nullable
    private LocalDateTime gradedAt;
}
