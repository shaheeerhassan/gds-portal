package com.school.service.interfaces;

import com.school.model.StudentClass;

import java.util.List;

public interface StudentClassService {
    StudentClass enrollStudent(StudentClass studentClass);
    StudentClass getCurrentEnrollment(long studentId);
    List<StudentClass> getEnrollmentHistory(long studentId);
    List<StudentClass> getStudentsBySection(int sectionId, int academicYearId);
    void endEnrollment(long studentId, int academicYearId);
    void transferStudent(StudentClass studentClass ,int newSectionId);
    void updateRollNumber(long studentId, int academicYearId, String newRollNumber);
    int promoteSection(int sourceSectionId, int sourceAcademicYearId, int targetSectionId, int targetAcademicYearId);
}
