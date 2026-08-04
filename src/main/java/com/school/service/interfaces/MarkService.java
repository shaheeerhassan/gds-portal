package com.school.service.interfaces;

import com.school.model.Mark;

import java.util.List;

public interface MarkService {
    void enterMarks(List<Mark> marks);
    Mark getMarkById(long markId);
    Mark getMarkByStudentAndExamination(long studentId, long examinationId);
    List<Mark> getMarksByExamination(long examinationId);
    List<Mark> getStudentMarksForYear(long studentId, int academicYearId);
    List<Mark> getStudentMarksByExamType(long studentId, String examName, int academicYearId);
    void updateMark(Mark mark);
    void deleteMark(long markId);
}
