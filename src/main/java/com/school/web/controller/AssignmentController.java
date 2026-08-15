package com.school.web.controller;

import com.school.exceptions.UnauthorizedException;
import com.school.exceptions.ValidationException;
import com.school.model.Assignment;
import com.school.model.Teacher;
import com.school.service.impl.AssignmentServiceImpl;
import com.school.service.impl.TeacherServiceImpl;
import com.school.service.interfaces.AssignmentService;
import com.school.service.interfaces.TeacherService;
import com.school.web.auth.AuthContext;
import com.school.web.auth.RoleGuard;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = "/api/assignments/*")
public class AssignmentController extends BaseServlet {

    private static final String ROLE_ADMIN = "ADMINISTRATOR";
    private static final String ROLE_PRINCIPAL = "PRINCIPAL";
    private static final String ROLE_TEACHER = "TEACHER";

    private final AssignmentService assignmentService;
    private final TeacherService teacherService;

    public AssignmentController() {
        this.assignmentService = new AssignmentServiceImpl();
        this.teacherService = new TeacherServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = pathInfo(req);

        if (path.startsWith("/id/")) {
            RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
            writeJson(resp, assignmentService.getAssignmentById(parseLong(path.substring("/id/".length()))));
            return;
        }
        if (path.startsWith("/section/")) {
            RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
            String[] parts = path.substring("/section/".length()).split("/");
            if (parts.length != 2)
                throw new ValidationException("Expected /section/{sectionId}/{academicYearId}.");
            writeJson(resp, assignmentService.getAssignmentsBySection(parseInt(parts[0]), parseInt(parts[1])));
            return;
        }
        if (path.startsWith("/teacher/")) {
            RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
            String[] parts = path.substring("/teacher/".length()).split("/");
            if (parts.length != 2)
                throw new ValidationException("Expected /teacher/{teacherId}/{academicYearId}.");
            long teacherId = parseLong(parts[0]);
            requireTeacherSelfAccess(req, teacherId);
            writeJson(resp, assignmentService.getAssignmentsByTeacher(teacherId, parseInt(parts[1])));
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

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
        Assignment assignment = readBody(req, Assignment.class);
        if (assignment == null)
            throw new ValidationException("Request body is required.");
        if (assignment.getTeacherId() == 0)
            assignment.setTeacherId(AuthContext.getUserId(req));
        if (assignment.getCreatedAt() == null)
            assignment.setCreatedAt(java.time.LocalDateTime.now());
        writeJson(resp, assignmentService.createAssignment(assignment), "Assignment created.");
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
        String path = pathInfo(req);

        if (path.startsWith("/publish/")) {
            assignmentService.publishAssignment(parseLong(path.substring("/publish/".length())));
            writeStatusMessage(resp, "Assignment published.");
            return;
        }
        long assignmentId = parseLong(path.substring(1));
        Assignment assignment = readBody(req, Assignment.class);
        if (assignment == null)
            throw new ValidationException("Request body is required.");
        assignment.setAssignmentId(assignmentId);
        assignmentService.updateAssignment(assignment);
        writeStatusMessage(resp, "Assignment updated.");
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
        long assignmentId = parseLong(pathInfo(req).substring(1));
        assignmentService.deleteAssignment(assignmentId);
        writeStatusMessage(resp, "Assignment deleted.");
    }
}
