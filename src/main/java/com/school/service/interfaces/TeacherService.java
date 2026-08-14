package com.school.service.interfaces;

import com.school.model.Section;
import com.school.model.Teacher;
import com.school.model.User;

import java.util.List;
import java.util.Map;

public interface TeacherService {
    Teacher createTeacher(User user, String password, Teacher teacher);
    List<Teacher> getAllTeachers();
    List<Teacher> searchTeachersByNameEmpId(String name);
    List<Teacher> getTeachersBySubject(int subjectId);
    List<Teacher> getTeachersBySection(int sectionId, int academicYearId);
    Map<Long, List<Section>> getAllClassTeachers();
    Teacher getTeacherByUserId(long userId);
    Teacher getTeacherById(long teacherId);
    Teacher getTeacherByEmployeeId(String employeeId);
    Teacher getTeacherByEmail(String email);
    int getTeacherCount();
    void updateTeacher(Teacher teacher);
    void deactivateTeacher(long teacherId);
}
