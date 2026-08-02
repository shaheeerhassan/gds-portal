package com.school.dao.impl;

import com.school.model.Section;
import com.school.model.Subject;
import com.school.model.Teacher;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface TeacherDao {
    boolean insertTeacher(Teacher teacher);

    boolean assignTeacherSubject(long teacherId, int subjectId, int sectionId, int academicYearId);
    boolean assignTeacherClass(long teacherId, int classId, int sectionId, int academicYearId);

    List<Subject> getTeacherSubjects(long teacherId, int academicYearId);
    List<Section> getTeacherClasses(long teacherId, int academicYearId);
    boolean unassignTeacherSubject(long teacherId, int subjectId, int sectionId, int academicYearId);
    boolean unassignTeacherClass(long teacherId, int classId, int sectionId, int academicYearId);

    List<Teacher> getAllTeachers();
    List<Teacher> getAllTeachersByName(String name);
    List<Teacher> getAllTeachersBySubject(int subjectId);
    Map<Teacher, Section> getAllClassTeachers();
    Teacher getTeacherByUserId(long userId);
    List<Teacher> getTeachersBySection(int sectionId, int academicYearId);

    Teacher getTeacherById(long teacherId);

    boolean updateTeacherDetails(Teacher teacher);
    boolean updateTeacherStatus(long teacherId, boolean isActive);

    boolean assignClassTeacher(long teacherId, int sectionId, int academicYearId, LocalDate assignedDate);
    boolean removeClassTeacher(long assignmentId, LocalDate removedDate);
}