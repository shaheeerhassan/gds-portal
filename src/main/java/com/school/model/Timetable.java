package com.school.model;

public class Timetable {
    public enum DayOfWeek { MON, TUE, WED, THU, FRI, SAT, SUN }
    private long timetableId;
    private int sectionId;
    private int subjectId;
    private long teacherId;
    private int periodId;
    private DayOfWeek dayOfWeek;
    private int academicYearId;

    public Timetable() {}
    public Timetable(long timetableId, int sectionId, int subjectId, long teacherId, int periodId, DayOfWeek dayOfWeek, int academicYearId) {
        this.timetableId = timetableId; this.sectionId = sectionId; this.subjectId = subjectId; this.teacherId = teacherId; this.periodId = periodId; this.dayOfWeek = dayOfWeek; this.academicYearId = academicYearId;
    }

    public long getTimetableId() { return timetableId; }
    public void setTimetableId(long timetableId) { this.timetableId = timetableId; }
    public int getSectionId() { return sectionId; }
    public void setSectionId(int sectionId) { this.sectionId = sectionId; }
    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }
    public long getTeacherId() { return teacherId; }
    public void setTeacherId(long teacherId) { this.teacherId = teacherId; }
    public int getPeriodId() { return periodId; }
    public void setPeriodId(int periodId) { this.periodId = periodId; }
    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public int getAcademicYearId() { return academicYearId; }
    public void setAcademicYearId(int academicYearId) { this.academicYearId = academicYearId; }
}
