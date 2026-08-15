package com.school.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TeacherClassDTO {
    private long teacherClassId;
    private long teacherId;
    private int classId;
    private String className;
    private int sectionId;
    private String sectionName;
    private int academicYearId;
}
