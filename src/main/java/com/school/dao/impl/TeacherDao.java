package com.school.dao.impl;

import com.school.model.Section;
import com.school.model.Subject;
import com.school.model.Teacher;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface TeacherDao {
    boolean insertTeacher(Teacher teacher);

    List<Teacher> getAllTeachers();
    List<Teacher> searchTeachersByName(String name);
    List<Teacher> getAllTeachersBySubject(int subjectId);
    List<Teacher> getTeachersBySection(int sectionId, int academicYearId);

    Map<Teacher, Section> getAllClassTeachers();

    Teacher getTeacherByUserId(long userId);
    Teacher getTeacherById(long teacherId);
    Teacher getTeacherByEmployeeId(String employeeId);
    Teacher getTeacherByEmail(String email);

    int getTeacherCount();

    boolean updateTeacherDetails(Teacher teacher);
    boolean deleteTeacher(long teacherId);
}