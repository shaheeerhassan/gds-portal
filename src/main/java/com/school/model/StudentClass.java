package com.school.model;

import lombok.*;
import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class StudentClass {
    private long studentClassId;
    private long studentId;
    private int classId;
    private int sectionId;
    private int academicYearId;
    private String rollNumber;
    private LocalDate enrollmentDate;
    private boolean isActive;
}
