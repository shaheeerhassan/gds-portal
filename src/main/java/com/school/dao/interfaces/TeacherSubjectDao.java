package com.school.dao.interfaces;

import com.school.model.Subject;

import java.util.List;

public interface TeacherSubjectDao {
    boolean assignTeacherSubject(long teacherId, int subjectId, int sectionId, int academicYearId);
    boolean isAssigned(long teacherId, int subjectId, int sectionId, int academicYearId);
    List<Subject> getTeacherSubjects(long teacherId, int academicYearId);
    boolean unassignTeacherSubject(long teacherSubjectId);
}
