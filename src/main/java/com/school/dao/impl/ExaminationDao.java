package com.school.dao.impl;

import com.school.model.Examination;
import java.util.List;

public interface ExaminationDao {
    boolean insertExamination(Examination examination);
    Examination getExaminationById(long examinationId);
    List<Examination> getExaminationsBySection(int sectionId, int academicYearId);
    boolean updateExaminationStatus(long examinationId, Examination.Status status);

    boolean updateExamination(Examination examination);
    List<Examination> getExaminationsByTeacher(long teacherId, int academicYearId);
}