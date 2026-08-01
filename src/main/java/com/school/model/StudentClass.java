package com.school.model;

import java.time.LocalDate;

public class StudentClass {
    private long studentClassId;
    private long studentId;
    private int classId;
    private int sectionId;
    private int academicYearId;
    private String rollNumber;
    private LocalDate enrollmentDate;
    private boolean isActive;

    public StudentClass() {}
    public StudentClass(long studentClassId, long studentId, int classId, int sectionId, int academicYearId, String rollNumber, LocalDate enrollmentDate, boolean isActive) {
        this.studentClassId = studentClassId; this.studentId = studentId; this.classId = classId; this.sectionId = sectionId; this.academicYearId = academicYearId; this.rollNumber = rollNumber; this.enrollmentDate = enrollmentDate; this.isActive = isActive;
    }

    public long getStudentClassId() { return studentClassId; }
    public void setStudentClassId(long studentClassId) { this.studentClassId = studentClassId; }
    public long getStudentId() { return studentId; }
    public void setStudentId(long studentId) { this.studentId = studentId; }
    public int getClassId() { return classId; }
    public void setClassId(int classId) { this.classId = classId; }
    public int getSectionId() { return sectionId; }
    public void setSectionId(int sectionId) { this.sectionId = sectionId; }
    public int getAcademicYearId() { return academicYearId; }
    public void setAcademicYearId(int academicYearId) { this.academicYearId = academicYearId; }
    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }
    public LocalDate getEnrollmentDate() { return enrollmentDate; }
    public void setEnrollmentDate(LocalDate enrollmentDate) { this.enrollmentDate = enrollmentDate; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
