package com.school.dao.impl;

import com.school.model.Assignment;
import com.school.model.Submission;
import java.util.List;

public interface AssignmentDao {
    long insertAssignment(Assignment assignment);
    Assignment getAssignmentById(long assignmentId);

    List<Assignment> getAssignmentsBySection(int sectionId, int academicYearId);
    boolean updateAssignment(Assignment assignment);
    boolean disableAssignment(long assignmentId); // Soft delete

    List<Assignment> getAssignmentsByTeacher(long teacherId, int academicYearId);
    List<Submission> getSubmissionsByAssignment(long assignmentId);
    List<Submission> getSubmissionsByStudent(long studentId);
    boolean updateSubmission(Submission submission);
    boolean markSubmissionLate(long submissionId);

    boolean insertSubmission(Submission submission);
    Submission getSubmission(long assignmentId, long studentId);
    boolean gradeSubmission(long submissionId, double marksObtained, String feedback);
}