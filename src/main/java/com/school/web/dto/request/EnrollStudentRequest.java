package com.school.web.dto.request;

import com.school.model.StudentClass;

public class EnrollStudentRequest {
    private long studentId;
    private int classId;
    private int sectionId;
    private int academicYearId;
    private String rollNumber;

    public StudentClass toStudentClass() {
        StudentClass sc = new StudentClass();
        sc.setStudentId(studentId);
        sc.setClassId(classId);
        sc.setSectionId(sectionId);
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

    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    public int getSectionId() {
        return sectionId;
    }

    public void setSectionId(int sectionId) {
        this.sectionId = sectionId;
    }

    public int getAcademicYearId() {
        return academicYearId;
    }

    public void setAcademicYearId(int academicYearId) {
        this.academicYearId = academicYearId;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }
}
