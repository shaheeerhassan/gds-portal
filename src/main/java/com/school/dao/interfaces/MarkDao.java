package com.school.dao.interfaces;

import com.school.model.Mark;
import java.util.List;

public interface MarkDao {
    boolean insertMarks(List<Mark> marks);
    boolean updateMark(Mark mark);
    boolean deleteMark(long markId);

    List<Mark> getMarksByExamination(long examinationId);
    List<Mark> getStudentMarksForYear(long studentId, int academicYearId);
    List<Mark> getStudentMarksByExamType(long studentId, String examName, int academicYearId);

    Mark getMarkById(long markId);
    Mark getMarkByStudentAndExamination(long studentId, long examinationId);
}