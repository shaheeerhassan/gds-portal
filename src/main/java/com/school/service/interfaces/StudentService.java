package com.school.service.interfaces;

import com.school.model.Student;
import com.school.model.User;

import java.util.List;

public interface StudentService {
    Student createStudent(User user, String password, Student student);
    Student getStudentByStudentId(long studentId);
    Student getStudentByUserId(long userId);
    Student getStudentByRegistrationNumber(String registrationNumber);
    List<Student> getAllStudents();
    List<Student> searchStudentsByName(String name);
    List<Student> getStudentsByClass(int classId, int academicYearId);
    List<Student> getStudentsBySection(int sectionId);
    List<Student> getStudentsByParentId(long parentId);
    void updateStudent(Student student);
    void deactivateStudent(long studentId);
    int getActiveStudentCount();
}
