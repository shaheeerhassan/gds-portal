package com.school.dao.impl;

import com.school.model.StudentClass;
import java.util.List;

public interface StudentClassDao {
    boolean enrollStudent(StudentClass studentClass);
    StudentClass getCurrentEnrollment(long studentId);
    List<StudentClass> getEnrollmentHistory(long studentId);
    boolean endEnrollment(long studentId, int academicYearId);
    boolean transferStudent(long studentId, int newSectionId, int academicYearId);
    boolean updateRollNumber(long studentId, int academicYearId, String newRollNumber);
}