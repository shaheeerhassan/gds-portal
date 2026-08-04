package com.school.service.interfaces;

import com.school.model.Subject;

import java.util.List;

public interface TeacherSubjectService {
    void assignTeacherSubject(long teacherId, int subjectId, int sectionId, int academicYearId);
    List<Subject> getTeacherSubjects(long teacherId, int academicYearId);
    void unassignTeacherSubject(long teacherSubjectId);
}
