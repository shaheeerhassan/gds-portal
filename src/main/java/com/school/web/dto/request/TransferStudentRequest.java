package com.school.web.dto.request;

import com.school.model.StudentClass;

public class TransferStudentRequest {
    private long studentId;
    private int academicYearId;
    private int newSectionId;
    private String rollNumber;

    public StudentClass toStudentClass() {
        StudentClass sc = new StudentClass();
        sc.setStudentId(studentId);
        sc.setAcademicYearId(academicYearId);
        sc.setRollNumber(rollNumber);
        return sc;
    }

    public long getStudentId() {
        return studentId;
    }

    public void setStudentId(long studentId) {
        this.studentId = studentId;
    }

    public int getAcademicYearId() {
        return academicYearId;
    }

    public void setAcademicYearId(int academicYearId) {
        this.academicYearId = academicYearId;
    }

    public int getNewSectionId() {
        return newSectionId;
    }

    public void setNewSectionId(int newSectionId) {
        this.newSectionId = newSectionId;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }
}
