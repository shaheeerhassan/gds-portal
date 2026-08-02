package com.school.dao.impl;

import com.school.model.Assignment;
import com.school.model.Submission;
import java.util.List;

public interface AssignmentDao {
    boolean insertAssignment(Assignment assignment);
    boolean updateAssignment(Assignment assignment);
    boolean disableAssignment(long assignmentId); // Soft delete

    Assignment getAssignmentById(long assignmentId);

    List<Assignment> getAssignmentsBySection(int sectionId, int academicYearId);
    List<Assignment> getAssignmentsByTeacher(long teacherId, int academicYearId);
}