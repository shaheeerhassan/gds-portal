package com.school.model;

import java.time.LocalTime;

public class Period {
    private int periodId;
    private int periodNumber;
    private LocalTime startTime;
    private LocalTime endTime;

    public Period() {}
    public Period(int periodId, int periodNumber, LocalTime startTime, LocalTime endTime) {
        this.periodId = periodId; this.periodNumber = periodNumber; this.startTime = startTime; this.endTime = endTime;
    }

    public int getPeriodId() { return periodId; }
    public void setPeriodId(int periodId) { this.periodId = periodId; }
    public int getPeriodNumber() { return periodNumber; }
    public void setPeriodNumber(int periodNumber) { this.periodNumber = periodNumber; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
}