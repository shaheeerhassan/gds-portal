package com.school.web.controller;

import com.school.exceptions.ValidationException;
import com.school.model.ClassTeacherAssignment;
import com.school.service.impl.ClassTeacherAssignmentServiceImpl;
import com.school.service.interfaces.ClassTeacherAssignmentService;
import com.school.web.auth.RoleGuard;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = "/api/class-teachers/*")
public class ClassTeacherAssignmentController extends BaseServlet {

    private static final String ROLE_ADMIN = "ADMINISTRATOR";
    private static final String ROLE_PRINCIPAL = "PRINCIPAL";
    private static final String ROLE_TEACHER = "TEACHER";

    private final ClassTeacherAssignmentService classTeacherAssignmentService;

    public ClassTeacherAssignmentController() {
        this.classTeacherAssignmentService = new ClassTeacherAssignmentServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = pathInfo(req);

        if (path.startsWith("/section/")) {
            String[] parts = path.substring("/section/".length()).split("/");
            if (parts.length == 1) {
                RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
                writeJson(resp, classTeacherAssignmentService.getCurrentAssignmentBySection(parseInt(parts[0])));
                return;
            }
            if (parts.length == 2 && "history".equals(parts[1])) {
                RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL);
                writeJson(resp, classTeacherAssignmentService.getAssignmentHistoryForSection(parseInt(parts[0])));
                return;
            }
            throw new ValidationException("Expected /section/{sectionId} or /section/{sectionId}/history.");
        }
        if (path.startsWith("/teacher/")) {
            String[] parts = path.substring("/teacher/".length()).split("/");
            if (parts.length == 3 && "year".equals(parts[1])) {
                RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
                writeJson(resp, classTeacherAssignmentService.getCurrentAssignmentForTeacher(
                        parseLong(parts[0]), parseInt(parts[2])));
                return;
            }
            if (parts.length == 2 && "assigned".equals(parts[1])) {
                RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
                String academicYearParam = req.getParameter("academicYearId");
                if (academicYearParam == null)
                    throw new ValidationException("academicYearId query parameter is required.");
                writeJson(resp, classTeacherAssignmentService.isTeacherAssignedAsClassTeacher(
                        parseLong(parts[0]), parseInt(academicYearParam)));
                return;
            }
            if (parts.length == 2 && "history".equals(parts[1])) {
                RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
                writeJson(resp, classTeacherAssignmentService.getAssignmentHistoryForTeacher(parseLong(parts[0])));
                return;
            }
            throw new ValidationException("Expected /teacher/{teacherId}/year/{academicYearId}, "
                    + "/teacher/{teacherId}/assigned?academicYearId= or /teacher/{teacherId}/history.");
        }
        throw new ValidationException("Unsupported path: " + path);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL);
        ClassTeacherAssignment assignment = readBody(req, ClassTeacherAssignment.class);
        if (assignment == null)
            throw new ValidationException("Request body is required.");
        if (assignment.getAssignedDate() == null)
            assignment.setAssignedDate(java.time.LocalDate.now());
        classTeacherAssignmentService.assignClassTeacher(assignment);
        writeStatusMessage(resp, "Class teacher assigned.");
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL);
        String[] parts = pathInfo(req).substring(1).split("/");
        if (parts.length != 4 || !"section".equals(parts[0]) || !"year".equals(parts[2]))
            throw new ValidationException("Expected /section/{sectionId}/year/{academicYearId}.");
        classTeacherAssignmentService.deleteClassTeacher(parseInt(parts[1]), parseInt(parts[3]));
        writeStatusMessage(resp, "Class teacher assignment removed.");
    }
}
