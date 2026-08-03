package com.school.dao.interfaces;

import com.school.model.Assignment;

import java.util.List;

public interface AssignmentDao {
    boolean insertAssignment(Assignment assignment);
    boolean updateAssignment(Assignment assignment);
    boolean deleteAssignment(long assignmentId);

    Assignment getAssignmentById(long assignmentId);

    List<Assignment> getAssignmentsBySection(int sectionId, int academicYearId);
    List<Assignment> getAssignmentsByTeacher(long teacherId, int academicYearId);
}