package com.school.dao.impl;

import com.school.model.ClassTeacherAssignment;
import java.util.List;

public interface ClassTeacherAssignmentDao {
    boolean assignClassTeacher(ClassTeacherAssignment assignment);
    boolean removeClassTeacher(int sectionId, int academicYearId);
    List<ClassTeacherAssignment> getAssignmentHistoryByTeacher(long teacherId);
    ClassTeacherAssignment getCurrentAssignmentBySection(int sectionId, int academicYearId);
    ClassTeacherAssignment getCurrentAssignmentByTeacher(long teacherId, int academicYearId);
}