package com.school.service.interfaces;

import com.school.model.Subject;

import java.util.List;

public interface SubjectService {
    Subject createSubject(Subject subject);
    Subject getSubjectById(int subjectId);
    List<Subject> getAllSubjects();
    int getSubjectCount();
    void updateSubject(Subject subject);
    void deleteSubject(int subjectId);
}
