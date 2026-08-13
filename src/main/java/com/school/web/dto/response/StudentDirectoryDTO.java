package com.school.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentDirectoryDTO {
    private long studentId;
    private String registrationNumber;
    private String firstName;
    private String lastName;
    private boolean isActive;

    // Enrollment Data (can be null if not enrolled in the selected academic year)
    private Integer academicYearId;
    private String academicYearName;
    private Integer classId;
    private String className;
    private Integer sectionId;
    private String sectionName;
    private String rollNumber;
}
