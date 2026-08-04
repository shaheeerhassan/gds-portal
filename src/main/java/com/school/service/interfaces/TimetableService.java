package com.school.service.interfaces;

import com.school.model.Timetable;

import java.util.List;

public interface TimetableService {
    Timetable createTimetableEntry(Timetable timetable);
    void updateTimetableEntry(Timetable timetable);
    void deleteTimetableEntry(long timetableId);
    List<Timetable> getTimetableBySectionAndYear(int sectionId, int academicYearId);
    List<Timetable> getTimetableByTeacher(long teacherId, int academicYearId);
    List<Timetable> getTimetableByDay(int sectionId, Timetable.DayOfWeek day);
}
