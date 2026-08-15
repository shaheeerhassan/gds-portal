package com.school.web.controller;

import com.school.exceptions.UnauthorizedException;
import com.school.exceptions.ValidationException;
import com.school.model.Teacher;
import com.school.model.TeacherAttendance;
import com.school.service.impl.TeacherAttendanceServiceImpl;
import com.school.service.impl.TeacherServiceImpl;
import com.school.service.interfaces.TeacherAttendanceService;
import com.school.service.interfaces.TeacherService;
import com.school.web.auth.AuthContext;
import com.school.web.auth.RoleGuard;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@WebServlet(urlPatterns = "/api/attendance/teachers/*")
public class TeacherAttendanceController extends BaseServlet {

    private static final String ROLE_ADMIN = "ADMINISTRATOR";
    private static final String ROLE_PRINCIPAL = "PRINCIPAL";
    private static final String ROLE_TEACHER = "TEACHER";

    private final TeacherAttendanceService teacherAttendanceService;
    private final TeacherService teacherService;

    public TeacherAttendanceController() {
        this.teacherAttendanceService = new TeacherAttendanceServiceImpl();
        this.teacherService = new TeacherServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = pathInfo(req);

        if (path.startsWith("/id/")) {
            RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
            writeJson(resp, teacherAttendanceService.getTeacherAttendanceById(parseLong(path.substring("/id/".length()))));
            return;
        }
        if (path.startsWith("/date/")) {
            RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL);
            writeJson(resp, teacherAttendanceService.getTeacherAttendanceByDate(parseDate(path.substring("/date/".length()))));
            return;
        }
        if (path.startsWith("/teacher/")) {
            RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
            long teacherId = parseLong(path.substring("/teacher/".length()));
            LocalDate startDate = parseDate(req.getParameter("startDate"));
            LocalDate endDate = parseDate(req.getParameter("endDate"));
            if (startDate == null || endDate == null)
                throw new ValidationException("startDate and endDate query parameters are required.");
            requireTeacherSelfAccess(req, teacherId);
            writeJson(resp, teacherAttendanceService.getTeacherAttendanceByTeacherId(teacherId, startDate, endDate));
            return;
        }
        throw new ValidationException("Unsupported path: " + path);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL);
        MarkAttendanceRequest request = readBody(req, MarkAttendanceRequest.class);
        if (request == null || request.getRecords() == null || request.getRecords().isEmpty())
            throw new ValidationException("records is required.");
        List<TeacherAttendance> records = request.getRecords();
        for (TeacherAttendance record : records) {
            if (record.getMarkedAt() == null)
                record.setMarkedAt(java.time.LocalDateTime.now());
        }
        teacherAttendanceService.markAttendance(records);
        writeStatusMessage(resp, "Attendance marked.");
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
        String path = pathInfo(req);

        if (path.startsWith("/status/")) {
            long attendanceId = parseLong(path.substring("/status/".length()));
            StatusRequest request = readBody(req, StatusRequest.class);
            if (request == null || request.getStatus() == null)
                throw new ValidationException("status is required.");
            teacherAttendanceService.updateTeacherAttendance(attendanceId, parseStatus(request.getStatus()));
            writeStatusMessage(resp, "Attendance status updated.");
            return;
        }
        if (path.startsWith("/check-in/")) {
            long attendanceId = parseLong(path.substring("/check-in/".length()));
            TimeRequest request = readBody(req, TimeRequest.class);
            if (request == null || request.getTime() == null)
                throw new ValidationException("time is required.");
            teacherAttendanceService.checkIn(attendanceId, parseTime(request.getTime()));
            writeStatusMessage(resp, "Checked in.");
            return;
        }
        if (path.startsWith("/check-out/")) {
            long attendanceId = parseLong(path.substring("/check-out/".length()));
            TimeRequest request = readBody(req, TimeRequest.class);
            if (request == null || request.getTime() == null)
                throw new ValidationException("time is required.");
            teacherAttendanceService.checkOut(attendanceId, parseTime(request.getTime()));
            writeStatusMessage(resp, "Checked out.");
            return;
        }
        throw new ValidationException("Unsupported path: " + path);
    }

    private void requireTeacherSelfAccess(HttpServletRequest req, long teacherId) {
        if (AuthContext.hasRole(req, ROLE_TEACHER)) {
            Teacher target = teacherService.getTeacherById(teacherId);
            if (target.getUserId() != AuthContext.getUserId(req))
                throw new UnauthorizedException("You can only access your own data.");
        }
    }

    private TeacherAttendance.Status parseStatus(String value) {
        try {
            return TeacherAttendance.Status.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid status: " + value);
        }
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank())
            return null;
        try {
            return LocalDate.parse(value);
        } catch (java.time.format.DateTimeParseException e) {
            throw new ValidationException("Invalid date: " + value);
        }
    }

    private LocalTime parseTime(String value) {
        try {
            return LocalTime.parse(value);
        } catch (java.time.format.DateTimeParseException e) {
            throw new ValidationException("Invalid time: " + value);
        }
    }

    public static class MarkAttendanceRequest {
        private List<TeacherAttendance> records;

        public List<TeacherAttendance> getRecords() {
            return records;
        }

        public void setRecords(List<TeacherAttendance> records) {
            this.records = records;
        }
    }

    public static class StatusRequest {
        private String status;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    public static class TimeRequest {
        private String time;

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }
    }
}
