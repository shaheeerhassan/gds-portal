package com.school.service.interfaces;

import com.school.model.AcademicYear;

import java.util.List;

public interface AcademicYearService {
    AcademicYear createAcademicYear(AcademicYear academicYear);
    AcademicYear getCurrentAcademicYear();
    AcademicYear getAcademicYearById(int academicYearId);
    List<AcademicYear> getAllAcademicYears();
    void updateAcademicYear(AcademicYear academicYear);
    void setCurrentAcademicYear(int academicYearId);
}
