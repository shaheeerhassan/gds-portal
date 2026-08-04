package com.school.service.impl;

import com.school.dao.impl.PeriodDaoImpl;
import com.school.dao.interfaces.PeriodDao;
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
}
