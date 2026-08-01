package com.school.model;

import java.time.LocalDate;

public class ClassTeacherAssignment {
    private long assignmentId;
    private long teacherId;
    private int sectionId;
    private int academicYearId;
    private LocalDate assignedDate;
    private LocalDate removedDate;
    private boolean isActive;

    public ClassTeacherAssignment() {}
    public ClassTeacherAssignment(long assignmentId, long teacherId, int sectionId, int academicYearId, LocalDate assignedDate, LocalDate removedDate, boolean isActive) {
        this.assignmentId = assignmentId; this.teacherId = teacherId; this.sectionId = sectionId; this.academicYearId = academicYearId; this.assignedDate = assignedDate; this.removedDate = removedDate; this.isActive = isActive;
    }

    public long getAssignmentId() { return assignmentId; }
    public void setAssignmentId(long assignmentId) { this.assignmentId = assignmentId; }
    public long getTeacherId() { return teacherId; }
    public void setTeacherId(long teacherId) { this.teacherId = teacherId; }
    public int getSectionId() { return sectionId; }
    public void setSectionId(int sectionId) { this.sectionId = sectionId; }
    public int getAcademicYearId() { return academicYearId; }
    public void setAcademicYearId(int academicYearId) { this.academicYearId = academicYearId; }
    public LocalDate getAssignedDate() { return assignedDate; }
    public void setAssignedDate(LocalDate assignedDate) { this.assignedDate = assignedDate; }
    public LocalDate getRemovedDate() { return removedDate; }
    public void setRemovedDate(LocalDate removedDate) { this.removedDate = removedDate; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
