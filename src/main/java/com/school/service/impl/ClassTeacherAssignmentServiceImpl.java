package com.school.service.impl;

import com.school.dao.impl.ClassTeacherAssignmentDaoImpl;
import com.school.dao.impl.SectionDaoImpl;
import com.school.dao.interfaces.ClassTeacherAssignmentDao;
import com.school.dao.interfaces.SectionDao;
import com.school.exceptions.ResourceNotFoundException;
import com.school.model.ClassTeacherAssignment;
import com.school.service.interfaces.ClassTeacherAssignmentService;

import java.util.List;

import static com.school.validations.ValidatorUtil.*;

public class ClassTeacherAssignmentServiceImpl implements ClassTeacherAssignmentService {

    private final ClassTeacherAssignmentDao classTeacherAssignmentDao;
    private final SectionDao sectionDao;

    public ClassTeacherAssignmentServiceImpl() {
        classTeacherAssignmentDao = new ClassTeacherAssignmentDaoImpl();
        sectionDao = new SectionDaoImpl();
    }

    @Override
    public void assignClassTeacher(ClassTeacherAssignment assignment) {
        validateId(assignment.getTeacherId(), assignment.getSectionId(), assignment.getAcademicYearId());

        deleteClassTeacher(assignment.getSectionId(), assignment.getAcademicYearId());

        if (!classTeacherAssignmentDao.assignClassTeacher(assignment))
            throw new IllegalStateException("Failed to assign class teacher.");
    }

    @Override
    public void deleteClassTeacher(int sectionId, int academicYearId) {
        validateId(sectionId);
        validateId(academicYearId);
        if (!classTeacherAssignmentDao.deleteClassTeacher(sectionId, academicYearId))
            throw new ResourceNotFoundException("Class teacher assignment not found.");
    }

    @Override
    public ClassTeacherAssignment getCurrentAssignmentBySection(int sectionId) {
        int academicYearId = sectionDao.getSectionById(sectionId).getSectionId();
        validateId(sectionId, academicYearId);
        ClassTeacherAssignment assignment = classTeacherAssignmentDao.getCurrentAssignmentBySection(sectionId, academicYearId);
        if (assignment == null)
            throw new ResourceNotFoundException("No class teacher assigned to this section.");
        return assignment;
    }

    @Override
    public ClassTeacherAssignment getCurrentAssignmentForTeacher(long teacherId, int academicYearId) {
        validateId(teacherId);
        validateId(academicYearId);
        ClassTeacherAssignment assignment = classTeacherAssignmentDao.getCurrentAssignmentForTeacher(teacherId, academicYearId);
        if (assignment == null)
            throw new ResourceNotFoundException("No current class teacher assignment for teacher.");
        return assignment;
    }

    @Override
    public boolean isTeacherAssignedAsClassTeacher(long teacherId, int academicYearId) {
        validateId(teacherId);
        validateId(academicYearId);
        return classTeacherAssignmentDao.isTeacherAssignedAsClassTeacher(teacherId, academicYearId);
    }

    @Override
    public List<ClassTeacherAssignment> getAssignmentHistoryForTeacher(long teacherId) {
        validateId(teacherId);
        return classTeacherAssignmentDao.getAssignmentHistoryForTeacher(teacherId);
    }

    @Override
    public List<ClassTeacherAssignment> getAssignmentHistoryForSection(int sectionId) {
        validateId(sectionId);
        return classTeacherAssignmentDao.getAssignmentHistoryForSection(sectionId);
    }
}
