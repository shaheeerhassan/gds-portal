package com.school.service.interfaces;

import com.school.model.Period;

import java.util.List;

public interface PeriodService {
    Period createPeriod(Period period);
    Period getPeriodById(int periodId);
    List<Period> getAllPeriods();
    void updatePeriod(Period period);
    void deletePeriod(int periodId);
}
