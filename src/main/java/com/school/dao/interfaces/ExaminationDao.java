package com.school.dao.interfaces;

import com.school.model.Examination;
import java.util.List;

public interface ExaminationDao {
    boolean insertExamination(Examination examination);
    Examination getExaminationById(long examinationId);

    List<Examination> getExaminationsBySection(int sectionId, int academicYearId);
    List<Examination> getSpecificExaminationsBySection(int sectionId, String examName, int academicYearId);

    boolean updateExaminationStatus(long examinationId, Examination.Status status);
    boolean updateExamination(Examination examination);

    List<Examination> getExaminationsByTeacher(long teacherId, int academicYearId);
    List<Examination> getSpecificExaminationsByTeacher(long teacherId, String examName, int academicYearId);
}