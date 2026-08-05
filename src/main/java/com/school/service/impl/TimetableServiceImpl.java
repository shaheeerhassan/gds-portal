package com.school.service.impl;

import com.school.dao.impl.AcademicYearDaoImpl;
import com.school.dao.impl.TimetableDaoImpl;
import com.school.dao.interfaces.AcademicYearDao;
import com.school.dao.interfaces.TimetableDao;
import com.school.exceptions.BusinessRuleException;
import com.school.exceptions.ResourceNotFoundException;
import com.school.exceptions.ValidationException;
import com.school.model.AcademicYear;
import com.school.model.Timetable;
import com.school.service.interfaces.TimetableService;

import java.util.List;

import static com.school.validations.ValidatorUtil.*;

public class TimetableServiceImpl implements TimetableService {

    private final TimetableDao timetableDao;
    private final AcademicYearDao academicYearDao;

    public TimetableServiceImpl() {
        timetableDao = new TimetableDaoImpl();
        academicYearDao = new AcademicYearDaoImpl();
    }

    @Override
    public Timetable createTimetableEntry(Timetable timetable) {
        validateTimetable(timetable);
        checkSlotConflicts(timetable, 0L);

        if (!timetableDao.insertTimeTable(timetable))
            throw new IllegalStateException("Failed to create timetable entry.");

        return timetable;
    }

    @Override
    public void updateTimetableEntry(Timetable timetable) {
        validateId(timetable.getTimetableId());
        validateTimetable(timetable);
        checkSlotConflicts(timetable, timetable.getTimetableId());

        if (!timetableDao.updateTimeTable(timetable))
            throw new ResourceNotFoundException("Timetable entry not found.");
    }

    @Override
    public void deleteTimetableEntry(long timetableId) {
        validateId(timetableId);
        if (!timetableDao.deleteTimetable(timetableId))
            throw new ResourceNotFoundException("Timetable entry not found.");
    }

    @Override
    public List<Timetable> getTimetableBySectionAndYear(int sectionId, int academicYearId) {
        validateId(sectionId);
        validateId(academicYearId);
        return timetableDao.getTimetableBySectionAndYear(sectionId, academicYearId);
    }

    @Override
    public List<Timetable> getTimetableByTeacher(long teacherId, int academicYearId) {
        validateId(teacherId);
        validateId(academicYearId);
        return timetableDao.getTimetableByTeacher(teacherId, academicYearId);
    }

    @Override
    public List<Timetable> getTimetableByDay(int sectionId, Timetable.DayOfWeek day) {
        AcademicYear currentAcademicYear = academicYearDao.getCurrentAcademicYear();

        int currentAcademicYearId;
        if (currentAcademicYear != null)
            currentAcademicYearId = currentAcademicYear.getAcademicYearId();
        else
            throw new ResourceNotFoundException("Current academic year not found.");

        validateId(sectionId);
        if (day == null)
            throw new ValidationException("Day of week is required.");
        return timetableDao.getTimetableByDay(sectionId, day, currentAcademicYearId);
    }

    private void validateTimetable(Timetable timetable) {
        validateId(timetable.getSectionId());
        validateId(timetable.getSubjectId());
        validateId(timetable.getTeacherId());
        validateId(timetable.getPeriodId());
        validateId(timetable.getAcademicYearId());
        if (timetable.getDayOfWeek() == null)
            throw new ValidationException("Day of week is required.");
    }

    private void checkSlotConflicts(Timetable timetable, long excludeTimetableId) {
        if (timetableDao.isSectionSlotOccupied(timetable.getSectionId(), timetable.getAcademicYearId(),
                timetable.getDayOfWeek(), timetable.getPeriodId(), excludeTimetableId))
            throw new BusinessRuleException("This section already has a lesson for this day and period.");

        if (timetableDao.isTeacherSlotOccupied(timetable.getTeacherId(), timetable.getAcademicYearId(),
                timetable.getDayOfWeek(), timetable.getPeriodId(), excludeTimetableId))
            throw new BusinessRuleException("This teacher is already assigned to another section at this day and period.");
    }
}
