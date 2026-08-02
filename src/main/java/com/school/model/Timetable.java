package com.school.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Timetable {
    public enum DayOfWeek { MON, TUE, WED, THU, FRI, SAT, SUN }
    private long timetableId;
    private int sectionId;
    private int subjectId;
    private long teacherId;
    private int periodId;
    private DayOfWeek dayOfWeek;
    private int academicYearId;
}
