package com.school.service.impl;

import com.school.dao.impl.AcademicYearDaoImpl;
import com.school.dao.interfaces.AcademicYearDao;
import com.school.exceptions.ResourceNotFoundException;
import com.school.model.AcademicYear;
import com.school.service.interfaces.AcademicYearService;

import java.util.List;

import static com.school.validations.ValidatorUtil.*;

public class AcademicYearServiceImpl implements AcademicYearService {

    private final AcademicYearDao academicYearDao;

    public AcademicYearServiceImpl() {
        academicYearDao = new AcademicYearDaoImpl();
    }

    @Override
    public AcademicYear createAcademicYear(AcademicYear academicYear) {
        academicYear.setYearName(validateRequired(academicYear.getYearName(), "Year name"));
        validateDateRange(academicYear.getStartDate(), academicYear.getEndDate(), "Academic year");

        if (!academicYearDao.insertAcademicYear(academicYear))
            throw new IllegalStateException("Failed to create academic year.");

        if (academicYear.isCurrent()) {
            academicYearDao.setCurrentAcademicYear(academicYear.getAcademicYearId());
        }
        return academicYear;
    }

    @Override
    public AcademicYear getCurrentAcademicYear() {
        AcademicYear academicYear = academicYearDao.getCurrentAcademicYear();
        if (academicYear == null)
            throw new ResourceNotFoundException("No current academic year is set.");
        return academicYear;
    }

    @Override
    public AcademicYear getAcademicYearById(int academicYearId) {
        validateId(academicYearId);
        AcademicYear academicYear = academicYearDao.getAcademicYearById(academicYearId);
        if (academicYear == null)
            throw new ResourceNotFoundException("Academic year not found.");
        return academicYear;
    }

    @Override
    public List<AcademicYear> getAllAcademicYears() {
        return academicYearDao.getAllAcademicYears();
    }

    @Override
    public void updateAcademicYear(AcademicYear academicYear) {
        validateId(academicYear.getAcademicYearId());
        academicYear.setYearName(validateRequired(academicYear.getYearName(), "Year name"));
        validateDateRange(academicYear.getStartDate(), academicYear.getEndDate(), "Academic year");

        if (!academicYearDao.updateAcademicYear(academicYear))
            throw new ResourceNotFoundException("Academic year not found.");
    }

    @Override
    public void setCurrentAcademicYear(int academicYearId) {
        validateId(academicYearId);
        if (academicYearDao.getAcademicYearById(academicYearId) == null)
            throw new ResourceNotFoundException("Academic year not found.");
        if (!academicYearDao.setCurrentAcademicYear(academicYearId))
            throw new IllegalStateException("Failed to set current academic year.");
    }
}
