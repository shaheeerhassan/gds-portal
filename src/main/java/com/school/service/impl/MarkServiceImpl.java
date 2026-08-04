package com.school.service.impl;

import com.school.dao.impl.MarkDaoImpl;
import com.school.dao.interfaces.MarkDao;
import com.school.exceptions.ResourceNotFoundException;
import com.school.exceptions.ValidationException;
import com.school.model.Mark;
import com.school.service.interfaces.MarkService;

import java.util.List;

import static com.school.validations.ValidatorUtil.*;

public class MarkServiceImpl implements MarkService {

    private final MarkDao markDao;

    public MarkServiceImpl() {
        markDao = new MarkDaoImpl();
    }

    @Override
    public void enterMarks(List<Mark> marks) {
        if (marks == null || marks.isEmpty())
            throw new ValidationException("At least one mark record is required.");

        for (Mark mark : marks)
            validateMark(mark);

        if (!markDao.insertMarks(marks))
            throw new IllegalStateException("Failed to enter marks.");
    }

    @Override
    public Mark getMarkById(long markId) {
        validateId(markId);
        Mark mark = markDao.getMarkById(markId);
        if (mark == null)
            throw new ResourceNotFoundException("Mark record not found.");
        return mark;
    }

    @Override
    public Mark getMarkByStudentAndExamination(long studentId, long examinationId) {
        validateId(studentId);
        validateId(examinationId);
        Mark mark = markDao.getMarkByStudentAndExamination(studentId, examinationId);
        if (mark == null)
            throw new ResourceNotFoundException("Mark record not found.");
        return mark;
    }

    @Override
    public List<Mark> getMarksByExamination(long examinationId) {
        validateId(examinationId);
        return markDao.getMarksByExamination(examinationId);
    }

    @Override
    public List<Mark> getStudentMarksForYear(long studentId, int academicYearId) {
        validateId(studentId);
        validateId(academicYearId);
        return markDao.getStudentMarksForYear(studentId, academicYearId);
    }

    @Override
    public List<Mark> getStudentMarksByExamType(long studentId, String examName, int academicYearId) {
        validateId(studentId);
        validateId(academicYearId);
        examName = validateRequired(examName, "Exam name");
        return markDao.getStudentMarksByExamType(studentId, examName, academicYearId);
    }

    @Override
    public void updateMark(Mark mark) {
        validateId(mark.getMarkId());
        validateMark(mark);

        if (!markDao.updateMark(mark))
            throw new ResourceNotFoundException("Mark record not found.");
    }

    @Override
    public void deleteMark(long markId) {
        validateId(markId);
        if (!markDao.deleteMark(markId))
            throw new ResourceNotFoundException("Mark record not found.");
    }

    private void validateMark(Mark mark) {
        validateId(mark.getExaminationId());
        validateId(mark.getStudentId());
        if (mark.getMarksObtained() < 0)
            throw new ValidationException("Marks obtained cannot be negative.");
    }
}
