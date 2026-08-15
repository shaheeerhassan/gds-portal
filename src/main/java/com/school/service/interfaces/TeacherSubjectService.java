package com.school.service.interfaces;

import com.school.dto.TeacherSubjectDTO;

import java.util.List;

public interface TeacherSubjectService {
    void assignTeacherSubject(long teacherId, int subjectId, int sectionId, int academicYearId);
    List<TeacherSubjectDTO> getTeacherSubjects(long teacherId, int academicYearId);
    void unassignTeacherSubject(long teacherSubjectId);
}
