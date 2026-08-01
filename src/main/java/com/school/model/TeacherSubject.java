package com.school.model;

import java.time.LocalDateTime;

public class TeacherSubject {
    private long teacherSubjectId;
    private long teacherId;
    private int subjectId;
    private int sectionId;
    private int academicYearId;
    private LocalDateTime assignedAt;

    public TeacherSubject() {}
    public TeacherSubject(long teacherSubjectId, long teacherId, int subjectId, int sectionId, int academicYearId, LocalDateTime assignedAt) {
        this.teacherSubjectId = teacherSubjectId; this.teacherId = teacherId; this.subjectId = subjectId; this.sectionId = sectionId; this.academicYearId = academicYearId; this.assignedAt = assignedAt;
    }

    public long getTeacherSubjectId() { return teacherSubjectId; }
    public void setTeacherSubjectId(long teacherSubjectId) { this.teacherSubjectId = teacherSubjectId; }
    public long getTeacherId() { return teacherId; }
    public void setTeacherId(long teacherId) { this.teacherId = teacherId; }
    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }
    public int getSectionId() { return sectionId; }
    public void setSectionId(int sectionId) { this.sectionId = sectionId; }
    public int getAcademicYearId() { return academicYearId; }
    public void setAcademicYearId(int academicYearId) { this.academicYearId = academicYearId; }
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
}
