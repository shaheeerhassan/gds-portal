package com.school.model;

import java.time.LocalDateTime;

public class Submission {
    public enum Status { SUBMITTED, LATE, GRADED }
    private long submissionId;
    private long assignmentId;
    private long studentId;
    private LocalDateTime submittedAt;
    private String fileUrl;
    private Status status;
    private Double marksAwarded; // wrapper class for nullable
    private String feedback;
    private Long gradedBy; // wrapper class for nullable
    private LocalDateTime gradedAt;

    public Submission() {}
    public Submission(long submissionId, long assignmentId, long studentId, LocalDateTime submittedAt, String fileUrl, Status status, Double marksAwarded, String feedback, Long gradedBy, LocalDateTime gradedAt) {
        this.submissionId = submissionId; this.assignmentId = assignmentId; this.studentId = studentId; this.submittedAt = submittedAt; this.fileUrl = fileUrl; this.status = status; this.marksAwarded = marksAwarded; this.feedback = feedback; this.gradedBy = gradedBy; this.gradedAt = gradedAt;
    }

    public long getSubmissionId() { return submissionId; }
    public void setSubmissionId(long submissionId) { this.submissionId = submissionId; }
    public long getAssignmentId() { return assignmentId; }
    public void setAssignmentId(long assignmentId) { this.assignmentId = assignmentId; }
    public long getStudentId() { return studentId; }
    public void setStudentId(long studentId) { this.studentId = studentId; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public Double getMarksAwarded() { return marksAwarded; }
    public void setMarksAwarded(Double marksAwarded) { this.marksAwarded = marksAwarded; }
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
    public Long getGradedBy() { return gradedBy; }
    public void setGradedBy(Long gradedBy) { this.gradedBy = gradedBy; }
    public LocalDateTime getGradedAt() { return gradedAt; }
    public void setGradedAt(LocalDateTime gradedAt) { this.gradedAt = gradedAt; }
}
