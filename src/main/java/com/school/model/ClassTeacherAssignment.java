package com.school.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ClassTeacherAssignment {
    private long assignmentId;
    private long teacherId;
    private int sectionId;
    private int academicYearId;
    private LocalDate assignedDate;
    private LocalDate removedDate;
    @JsonProperty("isActive")
    private boolean isActive;
}
