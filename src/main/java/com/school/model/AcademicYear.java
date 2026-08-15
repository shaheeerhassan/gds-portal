package com.school.model;

import com.fasterxml.jackson.annotation.JsonAlias;
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
    @JsonAlias("isCurrent")
    private boolean isCurrent;
}