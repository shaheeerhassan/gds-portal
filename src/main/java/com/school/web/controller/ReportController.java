package com.school.web.controller;

import com.school.exceptions.ValidationException;
import com.school.service.impl.ReportServiceImpl;
import com.school.service.interfaces.ReportService;
import com.school.web.auth.RoleGuard;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = "/api/reports/*")
public class ReportController extends BaseServlet {

    private static final String ROLE_ADMIN = "ADMINISTRATOR";
    private static final String ROLE_PRINCIPAL = "PRINCIPAL";

    private final ReportService reportService;

    public ReportController() {
        this.reportService = new ReportServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL);
        String path = pathInfo(req);

        if (path.startsWith("/student-performance/")) {
            String[] parts = path.substring("/student-performance/".length()).split("/");
            if (parts.length != 2)
                throw new ValidationException("Expected /student-performance/{studentId}/{academicYearId}.");
            writeJson(resp, reportService.getStudentPerformanceReport(parseLong(parts[0]), parseInt(parts[1])));
            return;
        }
        if (path.startsWith("/teacher-attendance/")) {
            String[] parts = path.substring("/teacher-attendance/".length()).split("/");
            if (parts.length != 3)
                throw new ValidationException("Expected /teacher-attendance/{teacherId}/{month}/{year}.");
            writeJson(resp, reportService.getTeacherAttendanceReport(
                    parseLong(parts[0]), parseInt(parts[1]), parseInt(parts[2])));
            return;
        }
        if (path.startsWith("/class-attendance/")) {
            String[] parts = path.substring("/class-attendance/".length()).split("/");
            if (parts.length != 3)
                throw new ValidationException("Expected /class-attendance/{sectionId}/{month}/{year}.");
            writeJson(resp, reportService.getClassAttendanceReport(
                    parseInt(parts[0]), parseInt(parts[1]), parseInt(parts[2])));
            return;
        }
        if (path.startsWith("/teacher-performance/")) {
            String[] parts = path.substring("/teacher-performance/".length()).split("/");
            if (parts.length != 2)
                throw new ValidationException("Expected /teacher-performance/{teacherId}/{academicYearId}.");
            writeJson(resp, reportService.getTeacherPerformanceReport(parseLong(parts[0]), parseInt(parts[1])));
            return;
        }
        if (path.startsWith("/examination/")) {
            writeJson(resp, reportService.getExaminationReport(parseLong(path.substring("/examination/".length()))));
            return;
        }
        if (path.startsWith("/student-attendance-summary/")) {
            String[] parts = path.substring("/student-attendance-summary/".length()).split("/");
            if (parts.length != 2)
                throw new ValidationException("Expected /student-attendance-summary/{studentId}/{academicYearId}.");
            writeJson(resp, reportService.getStudentAttendanceSummary(parseLong(parts[0]), parseInt(parts[1])));
            return;
        }
        throw new ValidationException("Unsupported path: " + path);
    }
}
