package com.school.web.controller;

import com.school.exceptions.ValidationException;
import com.school.model.StudentAttendance;
import com.school.service.impl.ParentServiceImpl;
import com.school.service.impl.StudentAttendanceServiceImpl;
import com.school.service.impl.StudentServiceImpl;
import com.school.service.interfaces.ParentService;
import com.school.service.interfaces.StudentAttendanceService;
import com.school.service.interfaces.StudentService;
import com.school.web.auth.AuthContext;
import com.school.web.auth.OwnershipGuard;
import com.school.web.auth.RoleGuard;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@WebServlet(urlPatterns = "/api/attendance/students/*")
public class StudentAttendanceController extends BaseServlet {

    private static final String ROLE_ADMIN = "ADMINISTRATOR";
    private static final String ROLE_PRINCIPAL = "PRINCIPAL";
    private static final String ROLE_TEACHER = "TEACHER";
    private static final String ROLE_STUDENT = "STUDENT";
    private static final String ROLE_PARENT = "PARENT";

    private final StudentAttendanceService studentAttendanceService;
    private final StudentService studentService;
    private final ParentService parentService;

    public StudentAttendanceController() {
        this.studentAttendanceService = new StudentAttendanceServiceImpl();
        this.studentService = new StudentServiceImpl();
        this.parentService = new ParentServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = pathInfo(req);

        if (path.startsWith("/id/")) {
            RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
            writeJson(resp, studentAttendanceService.getStudentAttendanceById(parseLong(path.substring("/id/".length()))));
            return;
        }
        if (path.equals("/present/count")) {
            RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL);
            writeJson(resp, studentAttendanceService.getPresentStudentCountToday());
            return;
        }
        if (path.startsWith("/section/")) {
            RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
            String[] parts = path.substring("/section/".length()).split("/");
            if (parts.length != 3 || !"date".equals(parts[1]))
                throw new ValidationException("Expected /section/{sectionId}/date/{date}.");
            writeJson(resp, studentAttendanceService.getStudentAttendanceBySectionAndDate(
                    parseInt(parts[0]), parseDate(parts[2])));
            return;
        }
        if (path.startsWith("/student/")) {
            String[] parts = path.substring("/student/".length()).split("/");
            if (parts.length != 1)
                throw new ValidationException("Expected /student/{studentId}?startDate=&endDate=.");
            long studentId = parseLong(parts[0]);
            if (AuthContext.hasRole(req, ROLE_STUDENT)) {
                OwnershipGuard.requireOwnStudent(req, studentService, studentId);
            } else if (AuthContext.hasRole(req, ROLE_PARENT)) {
                OwnershipGuard.requireLinkedParent(req, parentService, studentId);
            } else {
                RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
            }
            LocalDate startDate = parseDate(req.getParameter("startDate"));
            LocalDate endDate = parseDate(req.getParameter("endDate"));
            if (startDate == null || endDate == null)
                throw new ValidationException("startDate and endDate query parameters are required.");
            writeJson(resp, studentAttendanceService.getStudentAttendanceByStudentId(studentId, startDate, endDate));
            return;
        }
        throw new ValidationException("Unsupported path: " + path);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
        String path = pathInfo(req);

        if (path.startsWith("/lock")) {
            LockRequest request = readBody(req, LockRequest.class);
            if (request == null)
                throw new ValidationException("Request body is required.");
            studentAttendanceService.lockAttendanceForDate(
                    parseDate(request.getDate()), parseInt(String.valueOf(request.getSectionId())));
            writeStatusMessage(resp, "Attendance locked.");
            return;
        }
        if ("/".equals(path) || "".equals(path)) {
            MarkAttendanceRequest request = readBody(req, MarkAttendanceRequest.class);
            if (request == null || request.getRecords() == null || request.getRecords().isEmpty())
                throw new ValidationException("records is required.");
            List<StudentAttendance> records = request.getRecords();
            for (StudentAttendance record : records) {
                if (record.getMarkedBy() == 0)
                    record.setMarkedBy(AuthContext.getUserId(req));
            }
            studentAttendanceService.markAttendance(records);
            writeStatusMessage(resp, "Attendance marked.");
            return;
        }
        throw new ValidationException("Unsupported path: " + path);
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
            studentAttendanceService.updateStudentAttendance(attendanceId, parseStatus(request.getStatus()));
            writeStatusMessage(resp, "Attendance status updated.");
            return;
        }
        throw new ValidationException("Unsupported path: " + path);
    }

    private StudentAttendance.Status parseStatus(String value) {
        try {
            return StudentAttendance.Status.valueOf(value.toUpperCase());
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

    public static class MarkAttendanceRequest {
        private List<StudentAttendance> records;

        public List<StudentAttendance> getRecords() {
            return records;
        }

        public void setRecords(List<StudentAttendance> records) {
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

    public static class LockRequest {
        private String date;
        private int sectionId;

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public int getSectionId() {
            return sectionId;
        }

        public void setSectionId(int sectionId) {
            this.sectionId = sectionId;
        }
    }
}
