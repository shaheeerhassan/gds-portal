package com.school.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Examination {
    public enum Status { SCHEDULED, ONGOING, COMPLETED, PUBLISHED }
    private long examinationId;
    private String examName;
    private int subjectId;
    private int sectionId;
    private int academicYearId;
    private LocalDate examDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private double maxMarks;
    private Double passingMarks;
    private Status status;
    private long createdBy;
    private LocalDateTime createdAt;

    public Examination() {}
    public Examination(long examinationId, String examName, int subjectId, int sectionId, int academicYearId, LocalDate examDate, LocalTime startTime, LocalTime endTime, double maxMarks, Double passingMarks, Status status, long createdBy, LocalDateTime createdAt) {
        this.examinationId = examinationId; this.examName = examName; this.subjectId = subjectId; this.sectionId = sectionId; this.academicYearId = academicYearId; this.examDate = examDate; this.startTime = startTime; this.endTime = endTime; this.maxMarks = maxMarks; this.passingMarks = passingMarks; this.status = status; this.createdBy = createdBy; this.createdAt = createdAt;
    }

    public long getExaminationId() { return examinationId; }
    public void setExaminationId(long examinationId) { this.examinationId = examinationId; }
    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }
    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }
    public int getSectionId() { return sectionId; }
    public void setSectionId(int sectionId) { this.sectionId = sectionId; }
    public int getAcademicYearId() { return academicYearId; }
    public void setAcademicYearId(int academicYearId) { this.academicYearId = academicYearId; }
    public LocalDate getExamDate() { return examDate; }
    public void setExamDate(LocalDate examDate) { this.examDate = examDate; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public double getMaxMarks() { return maxMarks; }
    public void setMaxMarks(double maxMarks) { this.maxMarks = maxMarks; }
    public Double getPassingMarks() { return passingMarks; }
    public void setPassingMarks(Double passingMarks) { this.passingMarks = passingMarks; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public long getCreatedBy() { return createdBy; }
    public void setCreatedBy(long createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
