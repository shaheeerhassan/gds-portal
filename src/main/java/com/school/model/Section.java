package com.school.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Section {
    private int sectionId;
    private int classId;
    private int academicYearId;
    private String sectionName;
    private Integer capacity; // Using wrapper for nullable
    private String roomNumber;
}
