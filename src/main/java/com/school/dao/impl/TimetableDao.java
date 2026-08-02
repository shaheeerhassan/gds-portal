package com.school.dao.impl;

import com.school.model.Timetable;

import java.util.List;

public interface TimetableDao {
    boolean insertTimeTable(Timetable timetable);
    List<Timetable> getTimetableBySectionAndYear(int sectionId, int academicYearId);
    boolean updateTimeTable(Timetable timetable);
    boolean deleteTimetable(long timetableId);
    List<Timetable> getTimetableByTeacher(long teacherId, int academicYearId);
    List<Timetable> getTimetableByDay(int sectionId, Timetable.DayOfWeek day);
}