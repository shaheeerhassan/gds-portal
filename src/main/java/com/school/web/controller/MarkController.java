package com.school.web.controller;

import com.school.exceptions.ValidationException;
import com.school.model.Mark;
import com.school.service.impl.MarkServiceImpl;
import com.school.service.impl.ParentServiceImpl;
import com.school.service.impl.StudentServiceImpl;
import com.school.service.interfaces.MarkService;
import com.school.service.interfaces.ParentService;
import com.school.service.interfaces.StudentService;
import com.school.web.auth.AuthContext;
import com.school.web.auth.OwnershipGuard;
import com.school.web.auth.RoleGuard;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = "/api/marks/*")
public class MarkController extends BaseServlet {

    private static final String ROLE_ADMIN = "ADMINISTRATOR";
    private static final String ROLE_PRINCIPAL = "PRINCIPAL";
    private static final String ROLE_TEACHER = "TEACHER";
    private static final String ROLE_STUDENT = "STUDENT";
    private static final String ROLE_PARENT = "PARENT";

    private final MarkService markService;
    private final StudentService studentService;
    private final ParentService parentService;

    public MarkController() {
        this.markService = new MarkServiceImpl();
        this.studentService = new StudentServiceImpl();
        this.parentService = new ParentServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = pathInfo(req);

        if (path.startsWith("/id/")) {
            RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
            writeJson(resp, markService.getMarkById(parseLong(path.substring("/id/".length()))));
            return;
        }
        if (path.startsWith("/student/")) {
            String[] parts = path.substring("/student/".length()).split("/");
            if (parts.length == 3 && "examination".equals(parts[1])) {
                long studentId = parseLong(parts[0]);
                requireMarkRead(req, studentId);
                writeJson(resp, markService.getMarkByStudentAndExamination(
                        studentId, parseLong(parts[2])));
                return;
            }
            if (parts.length == 3 && "year".equals(parts[1])) {
                long studentId = parseLong(parts[0]);
                requireMarkRead(req, studentId);
                writeJson(resp, markService.getStudentMarksForYear(studentId, parseInt(parts[2])));
                return;
            }
            if (parts.length == 5 && "type".equals(parts[1]) && "year".equals(parts[3])) {
                long studentId = parseLong(parts[0]);
                requireMarkRead(req, studentId);
                writeJson(resp, markService.getStudentMarksByExamType(
                        studentId, parts[2], parseInt(parts[4])));
                return;
            }
            throw new ValidationException("Expected /student/{studentId}/examination/{examinationId}, "
                    + "/student/{studentId}/year/{academicYearId} or /student/{studentId}/type/{examName}/year/{academicYearId}.");
        }
        if (path.startsWith("/examination/")) {
            RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
            writeJson(resp, markService.getMarksByExamination(parseLong(path.substring("/examination/".length()))));
            return;
        }
        throw new ValidationException("Unsupported path: " + path);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
        String path = pathInfo(req);

        // OPTIONAL: Support for Batch Insert at /api/marks/batch
        if (path != null && path.equals("/batch")) {
            EnterMarksRequest request = readBody(req, EnterMarksRequest.class);
            if (request == null || request.getMarks() == null || request.getMarks().isEmpty())
                throw new ValidationException("marks array is required for batch insertion.");

            List<Mark> marks = request.getMarks();
            for (Mark mark : marks) {
                if (mark.getEnteredBy() == 0L) mark.setEnteredBy(AuthContext.getUserId(req));
                if (mark.getEnteredAt() == null) mark.setEnteredAt(java.time.LocalDateTime.now());
            }
            markService.enterMarks(marks);
            writeStatusMessage(resp, "Batch marks entered successfully.");
            return;
        }

        // DEFAULT: Single Mark Insertion at /api/marks/
        Mark mark = readBody(req, Mark.class);
        if (mark == null)
            throw new ValidationException("Request body is required.");

        if (mark.getEnteredBy() == 0L) {
            mark.setEnteredBy(AuthContext.getUserId(req));
        }
        if (mark.getEnteredAt() == null) {
            mark.setEnteredAt(java.time.LocalDateTime.now());
        }

        // Reuse your batch service by wrapping the single mark in a list
        markService.enterMarks(java.util.Collections.singletonList(mark));
        writeStatusMessage(resp, "Mark entered successfully.");
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
        long markId = parseLong(pathInfo(req).substring(1));
        Mark mark = readBody(req, Mark.class);
        if (mark == null)
            throw new ValidationException("Request body is required.");

        Mark original = markService.getMarkById(markId);
        mark.setMarkId(markId);

        if (mark.getEnteredBy() == 0L) {
            mark.setEnteredBy(AuthContext.getUserId(req));
        }
        if (mark.getEnteredAt() == null) {
            mark.setEnteredAt(original.getEnteredAt());
        }

        markService.updateMark(mark);
        writeStatusMessage(resp, "Mark updated successfully.");
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL);
        long markId = parseLong(pathInfo(req).substring(1));
        markService.deleteMark(markId);
        writeStatusMessage(resp, "Mark deleted.");
    }

    private void requireMarkRead(HttpServletRequest req, long studentId) {
        if (AuthContext.hasRole(req, ROLE_STUDENT)) {
            OwnershipGuard.requireOwnStudent(req, studentService, studentId);
            return;
        }
        if (AuthContext.hasRole(req, ROLE_PARENT)) {
            OwnershipGuard.requireLinkedParent(req, parentService, studentId);
            return;
        }
        RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
    }

    @Getter
    @Setter
    public static class EnterMarksRequest {
        private List<Mark> marks;
    }
}