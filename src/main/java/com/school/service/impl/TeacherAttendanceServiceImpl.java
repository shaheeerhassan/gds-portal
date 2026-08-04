package com.school.service.impl;

import com.school.dao.impl.TeacherAttendanceDaoImpl;
import com.school.dao.interfaces.TeacherAttendanceDao;
import com.school.exceptions.ResourceNotFoundException;
import com.school.exceptions.ValidationException;
import com.school.model.TeacherAttendance;
import com.school.service.interfaces.TeacherAttendanceService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static com.school.validations.ValidatorUtil.*;

public class TeacherAttendanceServiceImpl implements TeacherAttendanceService {

    private final TeacherAttendanceDao teacherAttendanceDao;

    public TeacherAttendanceServiceImpl() {
        teacherAttendanceDao = new TeacherAttendanceDaoImpl();
    }

    @Override
    public void markAttendance(List<TeacherAttendance> attendanceRecords) {
        if (attendanceRecords == null || attendanceRecords.isEmpty())
            throw new ValidationException("At least one attendance record is required.");

        for (TeacherAttendance record : attendanceRecords) {
            validateId(record.getTeacherId());
            if (record.getAttendanceDate() == null)
                throw new ValidationException("Attendance date is required.");
            if (record.getStatus() == null)
                throw new ValidationException("Attendance status is required.");
        }

        if (!teacherAttendanceDao.insertTeacherAttendance(attendanceRecords))
            throw new IllegalStateException("Failed to mark attendance.");
    }

    @Override
    public TeacherAttendance getTeacherAttendanceById(long attendanceId) {
        validateId(attendanceId);
        TeacherAttendance attendance = teacherAttendanceDao.getTeacherAttendanceById(attendanceId);
        if (attendance == null)
            throw new ResourceNotFoundException("Attendance record not found.");
        return attendance;
    }

    @Override
    public List<TeacherAttendance> getTeacherAttendanceByDate(LocalDate date) {
        if (date == null)
            throw new ValidationException("Date is required.");
        return teacherAttendanceDao.getTeacherAttendanceByDate(date);
    }

    @Override
    public List<TeacherAttendance> getTeacherAttendanceByTeacherId(long teacherId, LocalDate startDate, LocalDate endDate) {
        validateId(teacherId);
        validateDateRange(startDate, endDate, "Attendance period");
        return teacherAttendanceDao.getTeacherAttendanceByTeacherId(teacherId, startDate, endDate);
    }

    @Override
    public void updateTeacherAttendance(long attendanceId, TeacherAttendance.Status status) {
        validateId(attendanceId);
        if (status == null)
            throw new ValidationException("Attendance status is required.");

        if (!teacherAttendanceDao.updateTeacherAttendance(attendanceId, status))
            throw new ResourceNotFoundException("Attendance record not found.");
    }

    @Override
    public void checkIn(long attendanceId, LocalTime checkInTime) {
        validateId(attendanceId);
        if (checkInTime == null)
            throw new ValidationException("Check-in time is required.");

        if (!teacherAttendanceDao.updateTeacherCheckIn(attendanceId, checkInTime))
            throw new ResourceNotFoundException("Attendance record not found.");
    }

    @Override
    public void checkOut(long attendanceId, LocalTime checkOutTime) {
        validateId(attendanceId);
        if (checkOutTime == null)
            throw new ValidationException("Check-out time is required.");

        if (!teacherAttendanceDao.updateTeacherCheckOut(attendanceId, checkOutTime))
            throw new ResourceNotFoundException("Attendance record not found.");
    }
}
