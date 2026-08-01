package com.school.model;

import java.time.LocalDateTime;

public class Assignment {
    public enum Status { CREATED, PUBLISHED }
    private long assignmentId;
    private long teacherId;
    private int subjectId;
    private int sectionId;
    private String title;
    private String description;
    private double maxMarks;
    private LocalDateTime deadline;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Assignment() {}
    public Assignment(long assignmentId, long teacherId, int subjectId, int sectionId, String title, String description, double maxMarks, LocalDateTime deadline, Status status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.assignmentId = assignmentId; this.teacherId = teacherId; this.subjectId = subjectId; this.sectionId = sectionId; this.title = title; this.description = description; this.maxMarks = maxMarks; this.deadline = deadline; this.status = status; this.createdAt = createdAt; this.updatedAt = updatedAt;
    }

    public long getAssignmentId() { return assignmentId; }
    public void setAssignmentId(long assignmentId) { this.assignmentId = assignmentId; }
    public long getTeacherId() { return teacherId; }
    public void setTeacherId(long teacherId) { this.teacherId = teacherId; }
    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }
    public int getSectionId() { return sectionId; }
    public void setSectionId(int sectionId) { this.sectionId = sectionId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getMaxMarks() { return maxMarks; }
    public void setMaxMarks(double maxMarks) { this.maxMarks = maxMarks; }
    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

