package com.school.model;

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
    private boolean isActive;
}
