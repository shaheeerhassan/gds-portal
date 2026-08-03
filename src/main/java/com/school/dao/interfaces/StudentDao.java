package com.school.dao.interfaces;

import com.school.model.Student;
import java.util.List;

public interface StudentDao {
    boolean insertStudent(Student student);

    List<Student> getAllStudents();
    List<Student> getAllStudentsByName(String name);
    List<Student> getAllStudentsByClass(int classId, int academicYearId);
    List<Student> getAllStudentsBySection(int sectionId);

    Student getStudentByUserId(long userId);
    Student getStudentByStudentId(long studentId);
    Student getStudentByRegistrationNumber(String registrationNumber);

    boolean updateStudentDetails(Student student);
    boolean deleteStudent(long studentId, boolean isActive);
}