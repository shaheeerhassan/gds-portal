package com.school.model;

import lombok.*;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TeacherSubject {
    private long teacherSubjectId;
    private long teacherId;
    private int subjectId;
    private int sectionId;
    private int academicYearId;
    private LocalDateTime assignedAt;
}
