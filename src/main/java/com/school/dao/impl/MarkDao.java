package com.school.dao.impl;

import com.school.model.Mark;
import java.util.List;

public interface MarkDao {
    boolean insertMarks(List<Mark> marks);
    List<Mark> getMarksByExamination(long examinationId);
    List<Mark> getStudentMarksForYear(long studentId, int academicYearId);
    boolean updateMark(long markId, double newScore);
    Mark getMark(long studentId, long examinationId);
    boolean deleteMark(long markId);
    boolean updateMarkDetails(long markId, double newScore, String remarks);

}