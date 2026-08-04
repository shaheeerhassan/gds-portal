package com.school.service.interfaces;

import com.school.model.Examination;

import java.util.List;

public interface ExaminationService {
    Examination createExamination(Examination examination);
    Examination getExaminationById(long examinationId);
    List<Examination> getExaminationsBySection(int sectionId, int academicYearId);
    List<Examination> getSpecificExaminationsBySection(int sectionId, String examName, int academicYearId);
    List<Examination> getExaminationsByTeacher(long teacherId, int academicYearId);
    List<Examination> getSpecificExaminationsByTeacher(long teacherId, String examName, int academicYearId);
    List<Examination> getExaminationsByTeacherAndSection(long teacherId, int sectionId, int academicYearId);
    List<Examination> getSpecificExaminationsByTeacherAndSection(long teacherId, int sectionId, String examName, int academicYearId);
    void updateExamination(Examination examination);
    void updateExaminationStatus(long examinationId, Examination.Status status);
}
