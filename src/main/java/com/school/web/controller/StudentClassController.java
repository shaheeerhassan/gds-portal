package com.school.web.controller;

import com.school.exceptions.ValidationException;
import com.school.model.StudentClass;
import com.school.service.impl.StudentClassServiceImpl;
import com.school.service.interfaces.StudentClassService;
import com.school.web.auth.RoleGuard;
import com.school.web.dto.request.EnrollStudentRequest;
import com.school.web.dto.request.TransferStudentRequest;
import com.school.web.dto.request.UpdateRollNumberRequest;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = "/api/enrollments/*")
public class StudentClassController extends BaseServlet {

    private static final String ROLE_ADMIN = "ADMINISTRATOR";
    private static final String ROLE_PRINCIPAL = "PRINCIPAL";
    private static final String ROLE_TEACHER = "TEACHER";

    private final StudentClassService studentClassService;

    public StudentClassController() {
        this.studentClassService = new StudentClassServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = pathInfo(req);

        if (path.startsWith("/student/")) {
            RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
            long studentId = parseLong(path.substring("/student/".length()));
            writeJson(resp, studentClassService.getEnrollmentHistory(studentId));
            return;
        }
        if (path.startsWith("/current/")) {
            RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
            long studentId = parseLong(path.substring("/current/".length()));
            writeJson(resp, studentClassService.getCurrentEnrollment(studentId));
            return;
        }
        if (path.startsWith("/section/")) {
            RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
            String[] parts = path.substring("/section/".length()).split("/");
            if (parts.length != 2)
                throw new ValidationException("Expected /section/{sectionId}/{academicYearId}.");
            writeJson(resp, studentClassService.getStudentsBySection(parseInt(parts[0]), parseInt(parts[1])));
            return;
        }
        throw new ValidationException("Unsupported path: " + path);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN);
        String path = pathInfo(req);

        switch (path) {
            case "/enroll": {
                EnrollStudentRequest request = readBody(req, EnrollStudentRequest.class);
                if (request == null)
                    throw new ValidationException("Request body is required.");
                writeJson(resp, studentClassService.enrollStudent(request.toStudentClass()), "Student enrolled.");
                return;
            }
            case "/transfer": {
                TransferStudentRequest request = readBody(req, TransferStudentRequest.class);
                if (request == null || request.getNewSectionId() == 0)
                    throw new ValidationException("newSectionId is required.");
                studentClassService.transferStudent(request.toStudentClass(), request.getNewSectionId());
                writeStatusMessage(resp, "Student transferred.");
                return;
            }
            case "/promote": {
                RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL);
                PromoteRequest request = readBody(req, PromoteRequest.class);
                if (request == null)
                    throw new ValidationException("Request body is required.");
                int count = studentClassService.promoteSection(
                        request.getSourceSectionId(), request.getSourceAcademicYearId(),
                        request.getTargetSectionId(), request.getTargetAcademicYearId());
                writeJson(resp, count, "Promotion completed.");
                return;
            }
            default:
                throw new ValidationException("Unsupported path: " + path);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN);
        String path = pathInfo(req);

        if (path.startsWith("/end/")) {
            String[] parts = path.substring("/end/".length()).split("/");
            if (parts.length != 2)
                throw new ValidationException("Expected /end/{studentId}/{academicYearId}.");
            studentClassService.endEnrollment(parseLong(parts[0]), parseInt(parts[1]));
            writeStatusMessage(resp, "Enrollment ended.");
            return;
        }
        if (path.startsWith("/roll-number/")) {
            String[] parts = path.substring("/roll-number/".length()).split("/");
            if (parts.length != 2)
                throw new ValidationException("Expected /roll-number/{studentId}/{academicYearId}.");
            UpdateRollNumberRequest request = readBody(req, UpdateRollNumberRequest.class);
            if (request == null || request.getNewRollNumber() == null)
                throw new ValidationException("newRollNumber is required.");
            studentClassService.updateRollNumber(parseLong(parts[0]), parseInt(parts[1]), request.getNewRollNumber());
            writeStatusMessage(resp, "Roll number updated.");
            return;
        }
        throw new ValidationException("Unsupported path: " + path);
    }

    public static class PromoteRequest {
        private int sourceSectionId;
        private int sourceAcademicYearId;
        private int targetSectionId;
        private int targetAcademicYearId;

        public int getSourceSectionId() {
            return sourceSectionId;
        }

        public void setSourceSectionId(int sourceSectionId) {
            this.sourceSectionId = sourceSectionId;
        }

        public int getSourceAcademicYearId() {
            return sourceAcademicYearId;
        }

        public void setSourceAcademicYearId(int sourceAcademicYearId) {
            this.sourceAcademicYearId = sourceAcademicYearId;
        }

        public int getTargetSectionId() {
            return targetSectionId;
        }

        public void setTargetSectionId(int targetSectionId) {
            this.targetSectionId = targetSectionId;
        }

        public int getTargetAcademicYearId() {
            return targetAcademicYearId;
        }

        public void setTargetAcademicYearId(int targetAcademicYearId) {
            this.targetAcademicYearId = targetAcademicYearId;
        }
    }
}
