package com.school.dao.interfaces;

import com.school.model.Timetable;

import java.util.List;

public interface TimetableDao {
    boolean insertTimeTable(Timetable timetable);
    boolean updateTimeTable(Timetable timetable);
    boolean deleteTimetable(long timetableId);

    List<Timetable> getTimetableBySectionAndYear(int sectionId, int academicYearId);
    List<Timetable> getTimetableByTeacher(long teacherId, int academicYearId);
    List<Timetable> getTimetableByDay(int sectionId, Timetable.DayOfWeek day, int academicYearId);

    boolean isSectionSlotOccupied(int sectionId, int academicYearId, Timetable.DayOfWeek day, int periodId, long excludeTimetableId);
    boolean isTeacherSlotOccupied(long teacherId, int academicYearId, Timetable.DayOfWeek day, int periodId, long excludeTimetableId);
}