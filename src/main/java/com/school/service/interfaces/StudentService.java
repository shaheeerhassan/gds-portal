package com.school.service.interfaces;

import com.school.model.Student;
import com.school.model.User;
import com.school.web.dto.response.PaginatedResponse;
import com.school.web.dto.response.StudentDirectoryDTO;

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

    PaginatedResponse<StudentDirectoryDTO> getStudentDirectory(String query, Integer academicYearId, Integer classId, Integer sectionId, Boolean isEnrolled, int page, int size);
}
