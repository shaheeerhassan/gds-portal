package com.school.service.impl;

import com.school.dao.impl.StudentAttendanceDaoImpl;
import com.school.dao.interfaces.StudentAttendanceDao;
import com.school.exceptions.ResourceNotFoundException;
import com.school.exceptions.ValidationException;
import com.school.model.StudentAttendance;
import com.school.service.interfaces.StudentAttendanceService;

import java.time.LocalDate;
import java.util.List;

import static com.school.validations.ValidatorUtil.*;

public class StudentAttendanceServiceImpl implements StudentAttendanceService {

    private final StudentAttendanceDao studentAttendanceDao;

    public StudentAttendanceServiceImpl() {
        studentAttendanceDao = new StudentAttendanceDaoImpl();
    }

    @Override
    public void markAttendance(List<StudentAttendance> attendanceRecords) {
        if (attendanceRecords == null || attendanceRecords.isEmpty())
            throw new ValidationException("At least one attendance record is required.");

        for (StudentAttendance record : attendanceRecords) {
            validateId(record.getStudentClassId());
            if (record.getAttendanceDate() == null)
                throw new ValidationException("Attendance date is required.");
            if (record.getStatus() == null)
                throw new ValidationException("Attendance status is required.");
        }

        if (!studentAttendanceDao.insertStudentAttendance(attendanceRecords))
            throw new IllegalStateException("Failed to mark attendance.");
    }

    @Override
    public StudentAttendance getStudentAttendanceById(long attendanceId) {
        validateId(attendanceId);
        StudentAttendance attendance = studentAttendanceDao.getStudentAttendanceById(attendanceId);
        if (attendance == null)
            throw new ResourceNotFoundException("Attendance record not found.");
        return attendance;
    }

    @Override
    public List<StudentAttendance> getStudentAttendanceBySectionAndDate(int sectionId, LocalDate date) {
        validateId(sectionId);
        if (date == null)
            throw new ValidationException("Date is required.");
        return studentAttendanceDao.getStudentAttendanceBySectionAndDate(sectionId, date);
    }

    @Override
    public List<StudentAttendance> getStudentAttendanceByStudentId(long studentId, LocalDate startDate, LocalDate endDate) {
        validateId(studentId);
        validateDateRange(startDate, endDate, "Attendance period");
        return studentAttendanceDao.getStudentAttendanceByStudentId(studentId, startDate, endDate);
    }

    @Override
    public void updateStudentAttendance(long attendanceId, StudentAttendance.Status status) {
        validateId(attendanceId);
        if (status == null)
            throw new ValidationException("Attendance status is required.");

        if (!studentAttendanceDao.updateStudentAttendance(attendanceId, status))
            throw new ResourceNotFoundException("Attendance record not found.");
    }

    @Override
    public void lockAttendanceForDate(LocalDate date, int sectionId) {
        validateId(sectionId);
        if (date == null)
            throw new ValidationException("Date is required.");
        if (!studentAttendanceDao.lockAttendanceForDate(date, sectionId))
            throw new IllegalStateException("Failed to lock attendance.");
    }
}
