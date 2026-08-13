package com.school.dao.interfaces;

import com.school.model.Subject;

import java.util.List;

public interface SubjectDao {
    boolean insertSubject(Subject subject);
    Subject getSubjectById(int subjectId);
    List<Subject> getAllSubjects();
    boolean updateSubject(Subject subject);
    boolean deleteSubject(int subjectId);
    int getSubjectCount();
}
