package com.school.dao.impl;

import com.school.model.Student;
import java.time.LocalDate;
import java.util.List;

public interface StudentDao {
    boolean insertStudent(Student student);

    boolean assignStudentClass(long studentId, int classId, int sectionId, int academicYearId, String rollNo, LocalDate enrollmentDate);

    List<Student> getAllStudents();
    List<Student> getAllStudentsByName(String name);
    List<Student> getAllStudentsByClass(int classId);
    List<Student> getAllStudentsBySection(int sectionId);
    Student getStudentByUserId(long userId);
    List<Student> getAllStudentsByClassAndSection(int classId, int sectionId, int academicYearId);

    Student getStudentById(long studentId);
    Student getStudentByRegistrationNumber(String registrationNumber);

    boolean updateStudentDetails(Student student);
    boolean updateStudentStatus(long studentId, boolean isActive);
}