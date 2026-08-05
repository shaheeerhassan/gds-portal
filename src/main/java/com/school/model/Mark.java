package com.school.model;

import lombok.*;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Mark {
    private long markId;
    private long examinationId;
    private long studentId;
    private Double marksObtained;
    private String grade;
    private String remarks;
    private long enteredBy;
    private LocalDateTime enteredAt;
}
