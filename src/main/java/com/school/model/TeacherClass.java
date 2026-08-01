package com.school.model;

public class TeacherClass {
    private long teacherClassId;
    private long teacherId;
    private int classId;
    private int sectionId;
    private int academicYearId;

    public TeacherClass() {}
    public TeacherClass(long teacherClassId, long teacherId, int classId, int sectionId, int academicYearId) {
        this.teacherClassId = teacherClassId; this.teacherId = teacherId; this.classId = classId; this.sectionId = sectionId; this.academicYearId = academicYearId;
    }

    public long getTeacherClassId() { return teacherClassId; }
    public void setTeacherClassId(long teacherClassId) { this.teacherClassId = teacherClassId; }
    public long getTeacherId() { return teacherId; }
    public void setTeacherId(long teacherId) { this.teacherId = teacherId; }
    public int getClassId() { return classId; }
    public void setClassId(int classId) { this.classId = classId; }
    public int getSectionId() { return sectionId; }
    public void setSectionId(int sectionId) { this.sectionId = sectionId; }
    public int getAcademicYearId() { return academicYearId; }
    public void setAcademicYearId(int academicYearId) { this.academicYearId = academicYearId; }
}

