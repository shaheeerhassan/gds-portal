package com.school.dao.interfaces;

import com.school.model.Section;
import com.school.model.Teacher;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

public interface TeacherDao {
    boolean insertTeacher(Teacher teacher);
    boolean insertTeacher(Teacher teacher, Connection connection);

    List<Teacher> getAllTeachers();
    List<Teacher> searchTeachersByName(String name);
    List<Teacher> getAllTeachersBySubject(int subjectId);
    List<Teacher> getTeachersBySection(int sectionId, int academicYearId);

    Map<Long, List<Section>> getAllClassTeachers();

    Teacher getTeacherByUserId(long userId);
    Teacher getTeacherById(long teacherId);
    Teacher getTeacherByEmployeeId(String employeeId);
    Teacher getTeacherByEmail(String email);

    int getTeacherCount();

    boolean updateTeacherDetails(Teacher teacher);
    boolean deleteTeacher(long teacherId);
}