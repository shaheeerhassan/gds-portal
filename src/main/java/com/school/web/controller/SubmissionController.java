package com.school.web.controller;

import com.school.exceptions.ValidationException;
import com.school.model.Submission;
import com.school.service.impl.StudentServiceImpl;
import com.school.service.impl.SubmissionServiceImpl;
import com.school.service.interfaces.StudentService;
import com.school.service.interfaces.SubmissionService;
import com.school.web.auth.AuthContext;
import com.school.web.auth.OwnershipGuard;
import com.school.web.auth.RoleGuard;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = "/api/submissions/*")
public class SubmissionController extends BaseServlet {

    private static final String ROLE_ADMIN = "ADMINISTRATOR";
    private static final String ROLE_PRINCIPAL = "PRINCIPAL";
    private static final String ROLE_TEACHER = "TEACHER";
    private static final String ROLE_STUDENT = "STUDENT";

    private final SubmissionService submissionService;
    private final StudentService studentService;

    public SubmissionController() {
        this.submissionService = new SubmissionServiceImpl();
        this.studentService = new StudentServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = pathInfo(req);

        if (path.startsWith("/count/")) {
            RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
            writeJson(resp, submissionService.getSubmissionCount(parseLong(path.substring("/count/".length()))));
            return;
        }
        if (path.startsWith("/id/")) {
            long submissionId = parseLong(path.substring("/id/".length()));
            Submission submission = submissionService.getSubmissionById(submissionId);
            requireSubmissionRead(req, submission.getStudentId());
            writeJson(resp, submission);
            return;
        }
        if (path.startsWith("/assignment/")) {
            String[] parts = path.substring("/assignment/".length()).split("/");
            if (parts.length == 3 && "student".equals(parts[1])) {
                long assignmentId = parseLong(parts[0]);
                long studentId = parseLong(parts[2]);
                requireSubmissionRead(req, studentId);
                writeJson(resp, submissionService.getSubmission(assignmentId, studentId));
                return;
            }
            if (parts.length == 1) {
                RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
                writeJson(resp, submissionService.getSubmissionsByAssignment(parseLong(parts[0])));
                return;
            }
            throw new ValidationException("Expected /assignment/{assignmentId} or /assignment/{assignmentId}/student/{studentId}.");
        }
        if (path.startsWith("/student/")) {
            long studentId = parseLong(path.substring("/student/".length()));
            requireSubmissionRead(req, studentId);
            writeJson(resp, submissionService.getSubmissionsByStudent(studentId));
            return;
        }
        if (path.startsWith("/status/")) {
            RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
            writeJson(resp, submissionService.getSubmissionsByStatus(parseStatus(path.substring("/status/".length()))));
            return;
        }
        throw new ValidationException("Unsupported path: " + path);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_STUDENT);
        Submission submission = readBody(req, Submission.class);
        if (submission == null)
            throw new ValidationException("Request body is required.");
        if (AuthContext.hasRole(req, ROLE_STUDENT)) {
            submission.setStudentId(OwnershipGuard.ownStudentId(req, studentService));
        }

        if (submission.getSubmittedAt() == null)
            submission.setSubmittedAt(java.time.LocalDateTime.now());
        writeJson(resp, submissionService.submitAssignment(submission), "Assignment submitted.");
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = pathInfo(req);

        if (path.startsWith("/grade/")) {
            RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
            GradeRequest request = readBody(req, GradeRequest.class);
            if (request == null)
                throw new ValidationException("Request body is required.");
            long submissionId = parseLong(path.substring("/grade/".length()));
            submissionService.gradeSubmission(submissionId, request.getMarksAwarded(), request.getFeedback(), request.getGradedBy());
            writeStatusMessage(resp, "Submission graded.");
            return;
        }
        if (path.startsWith("/late/")) {
            RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
            submissionService.markLate(parseLong(path.substring("/late/".length())));
            writeStatusMessage(resp, "Submission marked as late.");
            return;
        }
        long submissionId = parseLong(path.substring(1));
        Submission existing = submissionService.getSubmissionById(submissionId);
        if (AuthContext.hasRole(req, ROLE_STUDENT)) {
            OwnershipGuard.requireOwnStudent(req, studentService, existing.getStudentId());
            Submission submission = readBody(req, Submission.class);
            if (submission == null)
                throw new ValidationException("Request body is required.");
            submission.setSubmissionId(submissionId);
            submission.setStatus(null);
            submission.setMarksAwarded(null);
            submission.setFeedback(null);
            submission.setGradedBy(null);
            submissionService.updateSubmission(submission);
            writeStatusMessage(resp, "Submission updated.");
            return;
        }
        RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL);
        Submission submission = readBody(req, Submission.class);
        if (submission == null)
            throw new ValidationException("Request body is required.");
        submission.setSubmissionId(submissionId);
        submissionService.updateSubmission(submission);
        writeStatusMessage(resp, "Submission updated.");
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = pathInfo(req);
        long submissionId = parseLong(path.substring(1));
        Submission existing = submissionService.getSubmissionById(submissionId);
        if (AuthContext.hasRole(req, ROLE_STUDENT)) {
            OwnershipGuard.requireOwnStudent(req, studentService, existing.getStudentId());
        } else {
            RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL);
        }
        submissionService.deleteSubmission(submissionId);
        writeStatusMessage(resp, "Submission deleted.");
    }

    private void requireSubmissionRead(HttpServletRequest req, long studentId) {
        if (AuthContext.hasRole(req, ROLE_STUDENT)) {
            OwnershipGuard.requireOwnStudent(req, studentService, studentId);
            return;
        }
        RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
    }

    private Submission.Status parseStatus(String value) {
        try {
            return Submission.Status.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid status: " + value);
        }
    }

    public static class GradeRequest {
        private double marksAwarded;
        private String feedback;
        private Long gradedBy;

        public double getMarksAwarded() {
            return marksAwarded;
        }

        public void setMarksAwarded(double marksAwarded) {
            this.marksAwarded = marksAwarded;
        }

        public String getFeedback() {
            return feedback;
        }

        public void setFeedback(String feedback) {
            this.feedback = feedback;
        }

        public Long getGradedBy() {
            return gradedBy;
        }

        public void setGradedBy(Long gradedBy) {
            this.gradedBy = gradedBy;
        }
    }
}
