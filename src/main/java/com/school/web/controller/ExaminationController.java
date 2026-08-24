package com.school.web.controller;

import com.school.exceptions.ValidationException;
import com.school.model.Examination;
import com.school.model.Parent;
import com.school.service.impl.ExaminationServiceImpl;
import com.school.service.interfaces.ExaminationService;
import com.school.web.auth.AuthContext;
import com.school.web.auth.RoleGuard;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = "/api/examinations/*")
public class ExaminationController extends BaseServlet {

    private static final String ROLE_ADMIN = "ADMINISTRATOR";
    private static final String ROLE_PRINCIPAL = "PRINCIPAL";
    private static final String ROLE_TEACHER = "TEACHER";
    private static final String ROLE_PARENT = "PARENT";

    private final ExaminationService examinationService;

    public ExaminationController() {
        this.examinationService = new ExaminationServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = pathInfo(req);

        if (path.startsWith("/id/")) {
            RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER, ROLE_PARENT);
            writeJson(resp, examinationService.getExaminationById(parseLong(path.substring("/id/".length()))));
            return;
        }
        if (path.startsWith("/section/")) {
            RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER, ROLE_PARENT);
            String[] parts = path.substring("/section/".length()).split("/");
            if (parts.length == 3 && "year".equals(parts[1])) {
                writeJson(resp, examinationService.getExaminationsBySection(parseInt(parts[0]), parseInt(parts[2])));
                return;
            }
            if (parts.length == 5 && "type".equals(parts[1]) && "year".equals(parts[3])) {
                writeJson(resp, examinationService.getSpecificExaminationsBySection(
                        parseInt(parts[0]), parts[2], parseInt(parts[4])));
                return;
            }
            throw new ValidationException("Expected /section/{sectionId}/year/{academicYearId} or "
                    + "/section/{sectionId}/type/{examName}/year/{academicYearId}.");
        }
        if (path.startsWith("/teacher/")) {
            RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
            String[] parts = path.substring("/teacher/".length()).split("/");
            if (parts.length == 3 && "year".equals(parts[1])) {
                writeJson(resp, examinationService.getExaminationsByTeacher(parseLong(parts[0]), parseInt(parts[2])));
                return;
            }
            if (parts.length == 5 && "type".equals(parts[1]) && "year".equals(parts[3])) {
                writeJson(resp, examinationService.getSpecificExaminationsByTeacher(
                        parseLong(parts[0]), parts[2], parseInt(parts[4])));
                return;
            }
            if (parts.length == 5 && "section".equals(parts[1]) && "year".equals(parts[3])) {
                writeJson(resp, examinationService.getExaminationsByTeacherAndSection(
                        parseLong(parts[0]), parseInt(parts[2]), parseInt(parts[4])));
                return;
            }
            if (parts.length == 7 && "section".equals(parts[1]) && "type".equals(parts[3]) && "year".equals(parts[5])) {
                writeJson(resp, examinationService.getSpecificExaminationsByTeacherAndSection(
                        parseLong(parts[0]), parseInt(parts[2]), parts[4], parseInt(parts[6])));
                return;
            }
            throw new ValidationException("Unsupported teacher examination path.");
        }
        throw new ValidationException("Unsupported path: " + path);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
        Examination examination = readBody(req, Examination.class);
        if (examination == null)
            throw new ValidationException("Request body is required.");
        if (examination.getCreatedBy() == 0)
            examination.setCreatedBy(AuthContext.getUserId(req));
        if (examination.getCreatedAt() == null)
            examination.setCreatedAt(java.time.LocalDateTime.now());
        writeJson(resp, examinationService.createExamination(examination), "Examination created.");
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
        String path = pathInfo(req);

        if (path.startsWith("/status/")) {
            long examinationId = parseLong(path.substring("/status/".length()));
            StatusRequest request = readBody(req, StatusRequest.class);
            if (request == null || request.getStatus() == null)
                throw new ValidationException("status is required.");
            examinationService.updateExaminationStatus(examinationId, parseStatus(request.getStatus()));
            writeStatusMessage(resp, "Examination status updated.");
            return;
        }
        long examinationId = parseLong(path.substring(1));
        Examination examination = readBody(req, Examination.class);
        if (examination == null)
            throw new ValidationException("Request body is required.");
        examination.setExaminationId(examinationId);
        examinationService.updateExamination(examination);
        writeStatusMessage(resp, "Examination updated.");
    }

    private Examination.Status parseStatus(String value) {
        try {
            return Examination.Status.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid status: " + value);
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
}
