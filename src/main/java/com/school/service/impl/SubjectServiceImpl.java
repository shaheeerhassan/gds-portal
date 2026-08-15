package com.school.service.impl;

import com.school.dao.impl.SubjectDaoImpl;
import com.school.dao.interfaces.SubjectDao;
import com.school.exceptions.ResourceNotFoundException;
import com.school.exceptions.ValidationException;
import com.school.model.Subject;
import com.school.service.interfaces.SubjectService;

import java.util.List;

import static com.school.validations.ValidatorUtil.*;

public class SubjectServiceImpl implements SubjectService {

    private final SubjectDao subjectDao;

    public SubjectServiceImpl() {
        subjectDao = new SubjectDaoImpl();
    }

    @Override
    public Subject createSubject(Subject subject) {
        subject.setSubjectName(validateRequired(subject.getSubjectName(), "Subject name"));
        subject.setSubjectCode(validateRequired(subject.getSubjectCode(), "Subject code"));

        if (!subjectDao.insertSubject(subject))
            throw new IllegalStateException("Failed to create subject.");

        return subject;
    }

    @Override
    public Subject getSubjectById(int subjectId) {
        validateId(subjectId);
        Subject subject = subjectDao.getSubjectById(subjectId);
        if (subject == null)
            throw new ResourceNotFoundException("Subject not found.");
        return subject;
    }

    @Override
    public List<Subject> getAllSubjects() {
        return subjectDao.getAllSubjects();
    }

    @Override
    public List<Subject> getSubjectsBySection(int sectionId, int academicYearId) {
        return subjectDao.getSubjectsBySection(sectionId, academicYearId);
    }

    @Override
    public void updateSubject(Subject subject) {
        validateId(subject.getSubjectId());
        subject.setSubjectName(validateRequired(subject.getSubjectName(), "Subject name"));
        subject.setSubjectCode(validateRequired(subject.getSubjectCode(), "Subject code"));

        if (!subjectDao.updateSubject(subject))
            throw new ResourceNotFoundException("Subject not found.");
    }

    @Override
    public void deleteSubject(int subjectId) {
        validateId(subjectId);
        if (!subjectDao.deleteSubject(subjectId)) {
            throw new ValidationException("Failed to delete subject. It may not exist.");
        }
    }

    @Override
    public int getSubjectCount() {
        return subjectDao.getSubjectCount();
    }
}
