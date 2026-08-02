package com.school.dao.impl;

import com.school.model.AcademicYear;
import java.util.List;

public interface AcademicYearDao {
    boolean insertAcademicYear(AcademicYear academicYear);
    AcademicYear getCurrentAcademicYear();
    AcademicYear getAcademicYearById(int academicYearId);
    List<AcademicYear> getAllAcademicYears();
    boolean updateAcademicYear(AcademicYear academicYear);
    boolean setCurrentAcademicYear(int academicYearId);
}