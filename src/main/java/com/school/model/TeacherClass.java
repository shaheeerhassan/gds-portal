package com.school.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TeacherClass {
    private long teacherClassId;
    private long teacherId;
    private int classId;
    private int sectionId;
    private int academicYearId;
}

