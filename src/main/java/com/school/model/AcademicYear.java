package com.school.model;

import lombok.*;
import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AcademicYear {
    private int academicYearId;
    private String yearName;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isCurrent;
}