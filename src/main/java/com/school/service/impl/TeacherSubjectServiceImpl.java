package com.school.service.impl;

import com.school.dao.impl.TeacherSubjectDaoImpl;
import com.school.dao.interfaces.TeacherSubjectDao;
import com.school.dto.TeacherSubjectDTO;
import com.school.exceptions.DuplicateResourceException;
import com.school.exceptions.ResourceNotFoundException;
import com.school.service.interfaces.TeacherSubjectService;

import java.util.List;

import static com.school.validations.ValidatorUtil.*;

public class TeacherSubjectServiceImpl implements TeacherSubjectService {

    private final TeacherSubjectDao teacherSubjectDao;

    public TeacherSubjectServiceImpl() {
        teacherSubjectDao = new TeacherSubjectDaoImpl();
    }

    @Override
    public void assignTeacherSubject(long teacherId, int subjectId, int sectionId, int academicYearId) {
        validateId(teacherId);
        validateId(subjectId);
        validateId(sectionId);
        validateId(academicYearId);

        if (teacherSubjectDao.isAssigned(teacherId, subjectId, sectionId, academicYearId))
            throw new DuplicateResourceException("This subject is already assigned to the teacher for this section and year.");

        if (!teacherSubjectDao.assignTeacherSubject(teacherId, subjectId, sectionId, academicYearId))
            throw new IllegalStateException("Failed to assign subject to teacher.");
    }

    @Override
    public List<TeacherSubjectDTO> getTeacherSubjects(long teacherId, int academicYearId) {
        validateId(teacherId);
        validateId(academicYearId);
        return teacherSubjectDao.getTeacherSubjects(teacherId, academicYearId);
    }

    @Override
    public void unassignTeacherSubject(long teacherSubjectId) {
        validateId(teacherSubjectId);
        if (!teacherSubjectDao.unassignTeacherSubject(teacherSubjectId))
            throw new ResourceNotFoundException("Teacher-subject assignment not found.");
    }
}
