package com.school.model;

import java.time.LocalDate;

public class AcademicYear {
    private int academicYearId;
    private String yearName;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isCurrent;

    public AcademicYear() {}
    public AcademicYear(int academicYearId, String yearName, LocalDate startDate, LocalDate endDate, boolean isCurrent) {
        this.academicYearId = academicYearId; this.yearName = yearName; this.startDate = startDate; this.endDate = endDate; this.isCurrent = isCurrent;
    }

    public int getAcademicYearId() { return academicYearId; }
    public void setAcademicYearId(int academicYearId) { this.academicYearId = academicYearId; }
    public String getYearName() { return yearName; }
    public void setYearName(String yearName) { this.yearName = yearName; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public boolean isCurrent() { return isCurrent; }
    public void setCurrent(boolean current) { isCurrent = current; }
}