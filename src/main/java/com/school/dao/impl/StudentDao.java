package com.school.dao.impl;

import com.school.model.Student;
import java.util.List;

public interface StudentDao {
    boolean insertStudent(Student student);

    List<Student> getAllStudents();
    List<Student> getAllStudentsByName(String name);
    List<Student> getAllStudentsByClass(int classId, int academicYearId);
    List<Student> getAllStudentsBySection(int sectionId);
    Student getStudentByUserId(long userId);
    List<Student> getAllStudentsByClassAndSection(int classId, int sectionId, int academicYearId);

    Student getStudentById(long studentId);
    Student getStudentByRegistrationNumber(String registrationNumber);

    boolean updateStudentDetails(Student student);
    boolean updateStudentStatus(long studentId, boolean isActive);
}