package com.school.service.impl;

import com.school.dao.impl.SubmissionDaoImpl;
import com.school.dao.interfaces.SubmissionDao;
import com.school.exceptions.BusinessRuleException;
import com.school.exceptions.ResourceNotFoundException;
import com.school.exceptions.ValidationException;
import com.school.model.Submission;
import com.school.service.interfaces.SubmissionService;

import java.util.List;

import static com.school.validations.ValidatorUtil.*;

public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionDao submissionDao;

    public SubmissionServiceImpl() {
        submissionDao = new SubmissionDaoImpl();
    }

    @Override
    public Submission submitAssignment(Submission submission) {
        validateId(submission.getAssignmentId());
        validateId(submission.getStudentId());
        if (submission.getFileUrl() == null || submission.getFileUrl().isBlank())
            throw new ValidationException("A file is required to submit an assignment.");

        if (submissionDao.submissionExists(submission.getAssignmentId(), submission.getStudentId()))
            throw new BusinessRuleException("This assignment has already been submitted.");

        if (!submissionDao.insertSubmission(submission))
            throw new IllegalStateException("Failed to submit assignment.");

        return submission;
    }

    @Override
    public Submission getSubmission(long assignmentId, long studentId) {
        validateId(assignmentId);
        validateId(studentId);
        Submission submission = submissionDao.getSubmission(assignmentId, studentId);
        if (submission == null)
            throw new ResourceNotFoundException("Submission not found.");
        return submission;
    }

    @Override
    public Submission getSubmissionById(long submissionId) {
        validateId(submissionId);
        Submission submission = submissionDao.getSubmissionById(submissionId);
        if (submission == null)
            throw new ResourceNotFoundException("Submission not found.");
        return submission;
    }

    @Override
    public List<Submission> getSubmissionsByAssignment(long assignmentId) {
        validateId(assignmentId);
        return submissionDao.getSubmissionsByAssignment(assignmentId);
    }

    @Override
    public List<Submission> getSubmissionsByStudent(long studentId) {
        validateId(studentId);
        return submissionDao.getSubmissionsByStudent(studentId);
    }

    @Override
    public List<Submission> getSubmissionsByStatus(Submission.Status status) {
        if (status == null)
            throw new ValidationException("Submission status is required.");
        return submissionDao.getSubmissionsByStatus(status);
    }

    @Override
    public int getSubmissionCount(long assignmentId) {
        validateId(assignmentId);
        return submissionDao.getSubmissionCount(assignmentId);
    }

    @Override
    public void updateSubmission(Submission submission) {
        validateId(submission.getSubmissionId());

        Submission existing = submissionDao.getSubmissionById(submission.getSubmissionId());

        if (existing == null)
            throw new ResourceNotFoundException("Submission not found.");

        if (submission.getFileUrl() == null)
            submission.setFileUrl(existing.getFileUrl());
        if (submission.getStatus() == null)
            submission.setStatus(existing.getStatus());

        if (!submissionDao.updateSubmission(submission))
            throw new ResourceNotFoundException("Submission not found.");
    }

    @Override
    public void markLate(long submissionId) {
        validateId(submissionId);
        if (!submissionDao.markSubmissionLate(submissionId))
            throw new ResourceNotFoundException("Submission not found.");
    }

    @Override
    public void gradeSubmission(long submissionId, double marksAwarded, String feedback) {
        validateId(submissionId);
        if (marksAwarded < 0)
            throw new ValidationException("Marks awarded cannot be negative.");
        if (!submissionDao.gradeSubmission(submissionId, marksAwarded, feedback))
            throw new ResourceNotFoundException("Submission not found.");
    }

    @Override
    public void deleteSubmission(long submissionId) {
        validateId(submissionId);
        if (!submissionDao.deleteSubmission(submissionId))
            throw new ResourceNotFoundException("Submission not found.");
    }
}
