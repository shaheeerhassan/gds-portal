package com.school.model;

import java.time.LocalDateTime;

public class Mark {
    private long markId;
    private long examinationId;
    private long studentId;
    private double marksObtained;
    private String grade;
    private String remarks;
    private long enteredBy;
    private LocalDateTime enteredAt;

    public Mark() {}
    public Mark(long markId, long examinationId, long studentId, double marksObtained, String grade, String remarks, long enteredBy, LocalDateTime enteredAt) {
        this.markId = markId; this.examinationId = examinationId; this.studentId = studentId; this.marksObtained = marksObtained; this.grade = grade; this.remarks = remarks; this.enteredBy = enteredBy; this.enteredAt = enteredAt;
    }

    public long getMarkId() { return markId; }
    public void setMarkId(long markId) { this.markId = markId; }
    public long getExaminationId() { return examinationId; }
    public void setExaminationId(long examinationId) { this.examinationId = examinationId; }
    public long getStudentId() { return studentId; }
    public void setStudentId(long studentId) { this.studentId = studentId; }
    public double getMarksObtained() { return marksObtained; }
    public void setMarksObtained(double marksObtained) { this.marksObtained = marksObtained; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public long getEnteredBy() { return enteredBy; }
    public void setEnteredBy(long enteredBy) { this.enteredBy = enteredBy; }
    public LocalDateTime getEnteredAt() { return enteredAt; }
    public void setEnteredAt(LocalDateTime enteredAt) { this.enteredAt = enteredAt; }
}
