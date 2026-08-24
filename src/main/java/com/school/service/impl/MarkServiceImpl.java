package com.school.service.impl;

import com.school.dao.impl.ExaminationDaoImpl;
import com.school.dao.impl.MarkDaoImpl;
import com.school.dao.interfaces.ExaminationDao;
import com.school.dao.interfaces.MarkDao;
import com.school.exceptions.ResourceNotFoundException;
import com.school.exceptions.ValidationException;
import com.school.model.Examination;
import com.school.model.Mark;
import com.school.service.interfaces.MarkService;

import java.util.List;

import static com.school.validations.ValidatorUtil.*;

public class MarkServiceImpl implements MarkService {

    private final MarkDao markDao;
    private final ExaminationDao examinationDao;

    public MarkServiceImpl() {
        markDao = new MarkDaoImpl();
        examinationDao = new ExaminationDaoImpl();
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

        Mark existing = markDao.getMarkById(mark.getMarkId());

        if (existing == null)
            throw new ResourceNotFoundException("Mark not found.");

        if (mark.getExaminationId() == 0)
            mark.setExaminationId(existing.getExaminationId());
        if (mark.getStudentId() == 0)
            mark.setStudentId(existing.getStudentId());
        if (mark.getMarksObtained() == null)
            mark.setMarksObtained(existing.getMarksObtained());
        if (mark.getGrade() == null)
            mark.setGrade(existing.getGrade());
        if (mark.getRemarks() == null)
            mark.setRemarks(existing.getRemarks());
        if (mark.getEnteredBy() == 0)
            mark.setEnteredBy(existing.getEnteredBy());

        System.out.println("before validation: markId: "+mark.getMarkId() + "enteredBy: "+ mark.getEnteredBy() + "obtained: "+mark.getMarksObtained()+"examinationId "+mark.getExaminationId());
        validateMark(mark);
        System.out.println("after validation: markId: "+mark.getMarkId() + "enteredBy: "+ mark.getEnteredBy() + "obtained: "+mark.getMarksObtained()+"examinationId "+mark.getExaminationId());

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
        if (mark.getMarksObtained() == null)
            throw new ValidationException("Marks obtained is required.");
        if (mark.getMarksObtained() < 0)
            throw new ValidationException("Marks obtained cannot be negative.");

        Examination examination = examinationDao.getExaminationById(mark.getExaminationId());
        if (examination == null)
            throw new ResourceNotFoundException("Examination not found.");
        if (mark.getMarksObtained() > examination.getMaxMarks())
            throw new ValidationException("Marks obtained cannot exceed the examination max marks.");
    }
}