package com.school.service.interfaces;

import com.school.model.Assignment;

import java.util.List;

public interface AssignmentService {
    Assignment createAssignment(Assignment assignment);
    Assignment getAssignmentById(long assignmentId);
    List<Assignment> getAssignmentsBySection(int sectionId, int academicYearId);
    List<Assignment> getAssignmentsByTeacher(long teacherId, int academicYearId);
    void updateAssignment(Assignment assignment);
    void publishAssignment(long assignmentId);
    void deleteAssignment(long assignmentId);
}
