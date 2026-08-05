package com.school.service.interfaces;

import com.school.model.Submission;

import java.util.List;

public interface SubmissionService {
    Submission submitAssignment(Submission submission);
    Submission getSubmission(long assignmentId, long studentId);
    Submission getSubmissionById(long submissionId);
    List<Submission> getSubmissionsByAssignment(long assignmentId);
    List<Submission> getSubmissionsByStudent(long studentId);
    List<Submission> getSubmissionsByStatus(Submission.Status status);
    int getSubmissionCount(long assignmentId);
    void updateSubmission(Submission submission);
    void markLate(long submissionId);
    void gradeSubmission(long submissionId, double marksAwarded, String feedback, Long gradedBy);
    void deleteSubmission(long submissionId);
}
