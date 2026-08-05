package com.school.service.impl;

import com.school.dao.impl.PeriodDaoImpl;
import com.school.dao.interfaces.PeriodDao;
import com.school.exceptions.DuplicateResourceException;
import com.school.exceptions.ResourceNotFoundException;
import com.school.exceptions.ValidationException;
import com.school.model.Period;
import com.school.service.interfaces.PeriodService;

import java.util.List;

import static com.school.validations.ValidatorUtil.*;

public class PeriodServiceImpl implements PeriodService {

    private final PeriodDao periodDao;

    public PeriodServiceImpl() {
        periodDao = new PeriodDaoImpl();
    }

    @Override
    public Period createPeriod(Period period) {
        validatePeriod(period);
        checkDuplicateNumber(period, 0);
        checkOverlap(period, 0);

        int periodId = periodDao.insertPeriod(period);
        if (periodId <= 0)
            throw new IllegalStateException("Failed to create period.");

        period.setPeriodId(periodId);
        return period;
    }

    @Override
    public Period getPeriodById(int periodId) {
        validateId(periodId);
        Period period = periodDao.getPeriodById(periodId);
        if (period == null)
            throw new ResourceNotFoundException("Period not found.");
        return period;
    }

    @Override
    public List<Period> getAllPeriods() {
        return periodDao.getAllPeriods();
    }

    @Override
    public void updatePeriod(Period period) {
        validateId(period.getPeriodId());
        validatePeriod(period);
        checkDuplicateNumber(period, period.getPeriodId());
        checkOverlap(period, period.getPeriodId());

        if (!periodDao.updatePeriod(period))
            throw new ResourceNotFoundException("Period not found.");
    }

    @Override
    public void deletePeriod(int periodId) {
        validateId(periodId);
        if (!periodDao.deletePeriod(periodId))
            throw new ResourceNotFoundException("Period not found.");
    }

    private void validatePeriod(Period period) {
        validateId(period.getPeriodNumber());
        if (period.getStartTime() == null)
            throw new ValidationException("Start time is required.");
        if (period.getEndTime() == null)
            throw new ValidationException("End time is required.");
        if (!period.getEndTime().isAfter(period.getStartTime()))
            throw new ValidationException("End time must be after start time.");
    }

    private void checkDuplicateNumber(Period period, int excludePeriodId) {
        if (periodDao.existsByPeriodNumber(period.getPeriodNumber(), excludePeriodId))
            throw new DuplicateResourceException("A period with this number already exists.");
    }

    private void checkOverlap(Period period, int excludePeriodId) {
        for (Period existing : periodDao.getAllPeriods()) {
            if (existing.getPeriodId() == excludePeriodId)
                continue;
            boolean overlaps = existing.getStartTime().isBefore(period.getEndTime())
                    && existing.getEndTime().isAfter(period.getStartTime());
            if (overlaps)
                throw new ValidationException("Period time range overlaps with period number " + existing.getPeriodNumber() + ".");
        }
    }
}
