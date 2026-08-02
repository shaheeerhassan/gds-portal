package com.school.dao.impl;

import com.school.model.ClassTeacherAssignment;
import java.util.List;

public interface ClassTeacherAssignmentDao {
    boolean assignClassTeacher(ClassTeacherAssignment assignment);
    boolean deleteClassTeacher(int sectionId, int academicYearId);

    ClassTeacherAssignment getCurrentAssignmentBySection(int sectionId);
    ClassTeacherAssignment getCurrentAssignmentForTeacher(long teacherId, int academicYearId);

    boolean isTeacherAssignedAsClassTeacher(long teacherId, int academicYearId);

    List<ClassTeacherAssignment> getAssignmentHistoryForTeacher(long teacherId);
    List<ClassTeacherAssignment> getAssignmentHistoryForSection(int sectionId);
}