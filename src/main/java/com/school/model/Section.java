package com.school.model;

public class Section {
    private int sectionId;
    private int classId;
    private int academicYearId;
    private String sectionName;
    private Integer capacity; // Using wrapper for nullable
    private String roomNumber;

    public Section() {}
    public Section(int sectionId, int classId, int academicYearId, String sectionName, Integer capacity, String roomNumber) {
        this.sectionId = sectionId; this.classId = classId; this.academicYearId = academicYearId; this.sectionName = sectionName; this.capacity = capacity; this.roomNumber = roomNumber;
    }

    public int getSectionId() { return sectionId; }
    public void setSectionId(int sectionId) { this.sectionId = sectionId; }
    public int getClassId() { return classId; }
    public void setClassId(int classId) { this.classId = classId; }
    public int getAcademicYearId() { return academicYearId; }
    public void setAcademicYearId(int academicYearId) { this.academicYearId = academicYearId; }
    public String getSectionName() { return sectionName; }
    public void setSectionName(String sectionName) { this.sectionName = sectionName; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
}
