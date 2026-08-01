package com.school.model;

public class Class {
    private int classId;
    private String className;
    private int numericLevel;
    private String description;

    public Class() {}
    public Class(int classId, String className, int numericLevel, String description) {
        this.classId = classId; this.className = className; this.numericLevel = numericLevel; this.description = description;
    }

    public int getClassId() { return classId; }
    public void setClassId(int classId) { this.classId = classId; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public int getNumericLevel() { return numericLevel; }
    public void setNumericLevel(int numericLevel) { this.numericLevel = numericLevel; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

