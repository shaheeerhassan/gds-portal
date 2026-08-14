package com.school.dao.interfaces;

import com.school.dto.TeacherSubjectDTO;
import java.util.List;

public interface TeacherSubjectDao {
    boolean assignTeacherSubject(long teacherId, int subjectId, int sectionId, int academicYearId);
    boolean isAssigned(long teacherId, int subjectId, int sectionId, int academicYearId);
    List<TeacherSubjectDTO> getTeacherSubjects(long teacherId, int academicYearId);
    boolean unassignTeacherSubject(long teacherSubjectId);
}
