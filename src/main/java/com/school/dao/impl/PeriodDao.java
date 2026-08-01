package com.school.dao.impl;

import com.school.model.Period;
import java.util.List;

public interface PeriodDao {
    int insertPeriod(Period period);
    Period getPeriodById(int periodId);
    List<Period> getAllPeriods();
    boolean updatePeriod(Period period);
    boolean deletePeriod(int periodId);
}