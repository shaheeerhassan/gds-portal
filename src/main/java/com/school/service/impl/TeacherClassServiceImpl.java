package com.school.service.impl;

import com.school.dao.impl.TeacherClassDaoImpl;
import com.school.dao.interfaces.TeacherClassDao;
import com.school.dto.TeacherClassDTO;
import com.school.exceptions.DuplicateResourceException;
import com.school.exceptions.ResourceNotFoundException;
import com.school.service.interfaces.TeacherClassService;

import java.util.List;

import static com.school.validations.ValidatorUtil.*;

public class TeacherClassServiceImpl implements TeacherClassService {

    private final TeacherClassDao teacherClassDao;

    public TeacherClassServiceImpl() {
        teacherClassDao = new TeacherClassDaoImpl();
    }

    @Override
    public void assignTeacherClass(long teacherId, int classId, int sectionId, int academicYearId) {
        validateId(teacherId);
        validateId(classId);
        validateId(sectionId);
        validateId(academicYearId);

        if (teacherClassDao.isAssigned(teacherId, classId, sectionId, academicYearId))
            throw new DuplicateResourceException("This teacher is already assigned to this class and section for the year.");

        if (!teacherClassDao.assignTeacherClass(teacherId, classId, sectionId, academicYearId))
            throw new IllegalStateException("Failed to assign teacher to class.");
    }

    @Override
    public List<TeacherClassDTO> getTeacherClasses(long teacherId, int academicYearId) {
        validateId(teacherId);
        validateId(academicYearId);
        return teacherClassDao.getTeacherClasses(teacherId, academicYearId);
    }

    @Override
    public void unassignTeacherClass(long teacherClassId) {
        validateId(teacherClassId);
        if (!teacherClassDao.unassignTeacherClass(teacherClassId))
            throw new ResourceNotFoundException("Teacher-class assignment not found.");
    }
}
