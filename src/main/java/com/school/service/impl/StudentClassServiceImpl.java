package com.school.service.impl;

import com.school.dao.impl.StudentClassDaoImpl;
import com.school.dao.interfaces.StudentClassDao;
import com.school.exceptions.ResourceNotFoundException;
import com.school.model.StudentClass;
import com.school.service.interfaces.StudentClassService;

import java.util.List;

import static com.school.validations.ValidatorUtil.*;

public class StudentClassServiceImpl implements StudentClassService {

    private final StudentClassDao studentClassDao;

    public StudentClassServiceImpl() {
        studentClassDao = new StudentClassDaoImpl();
    }

    @Override
    public StudentClass enrollStudent(StudentClass studentClass) {
        validateId(studentClass.getStudentId());
        validateId(studentClass.getClassId());
        validateId(studentClass.getSectionId());
        validateId(studentClass.getAcademicYearId());

        if (!studentClassDao.enrollStudent(studentClass))
            throw new IllegalStateException("Failed to enroll student.");

        return studentClass;
    }

    @Override
    public StudentClass getCurrentEnrollment(long studentId) {
        validateId(studentId);
        StudentClass enrollment = studentClassDao.getCurrentEnrollment(studentId);
        if (enrollment == null)
            throw new ResourceNotFoundException("No current enrollment found for student.");
        return enrollment;
    }

    @Override
    public List<StudentClass> getEnrollmentHistory(long studentId) {
        validateId(studentId);
        return studentClassDao.getEnrollmentHistory(studentId);
    }

    @Override
    public List<StudentClass> getStudentsBySection(int sectionId, int academicYearId) {
        validateId(sectionId);
        validateId(academicYearId);
        return studentClassDao.getStudentsBySection(sectionId, academicYearId);
    }

    @Override
    public void endEnrollment(long studentId, int academicYearId) {
        validateId(studentId);
        validateId(academicYearId);
        if (!studentClassDao.endEnrollment(studentId, academicYearId))
            throw new ResourceNotFoundException("Active enrollment not found for student.");
    }

    @Override
    public void transferStudent(long studentId, int newSectionId, int academicYearId) {
        validateId(studentId);
        validateId(newSectionId);
        validateId(academicYearId);
        if (!studentClassDao.transferStudent(studentId, newSectionId, academicYearId))
            throw new ResourceNotFoundException("Active enrollment not found for student.");
    }

    @Override
    public void updateRollNumber(long studentId, int academicYearId, String newRollNumber) {
        validateId(studentId);
        validateId(academicYearId);
        newRollNumber = validateRequired(newRollNumber, "Roll number");
        if (!studentClassDao.updateRollNumber(studentId, academicYearId, newRollNumber))
            throw new ResourceNotFoundException("Active enrollment not found for student.");
    }

    @Override
    public int promoteSection(int sourceSectionId, int sourceAcademicYearId, int targetSectionId, int targetAcademicYearId) {
        validateId(sourceSectionId);
        validateId(sourceAcademicYearId);
        validateId(targetSectionId);
        validateId(targetAcademicYearId);
        return studentClassDao.promoteSection(sourceSectionId, sourceAcademicYearId, targetSectionId, targetAcademicYearId);
    }
}
