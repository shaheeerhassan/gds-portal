package com.school.dao.interfaces;

import com.school.model.Submission;

import java.util.List;

public interface SubmissionDao {
    boolean insertSubmission(Submission submission);
    boolean updateSubmission(Submission submission);
    boolean markSubmissionLate(long submissionId);
    boolean gradeSubmission(long submissionId, double marksObtained, String feedback);
    boolean deleteSubmission(long submissionId);
    boolean submissionExists(long assignmentId, long studentId);

    int getSubmissionCount(long assignmentId);

    List<Submission> getSubmissionsByAssignment(long assignmentId);
    List<Submission> getSubmissionsByStudent(long studentId);
    List<Submission> getSubmissionsByStatus(Submission.Status status);

    Submission getSubmission(long assignmentId, long studentId);
    Submission getSubmissionById(long submissionId);
}
