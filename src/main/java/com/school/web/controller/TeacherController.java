package com.school.web.controller;

import com.school.exceptions.ValidationException;
import com.school.model.Teacher;
import com.school.service.impl.TeacherServiceImpl;
import com.school.service.interfaces.TeacherService;
import com.school.web.auth.AuthContext;
import com.school.web.auth.RoleGuard;
import com.school.web.dto.request.CreateTeacherRequest;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = "/api/teachers/*")
public class TeacherController extends BaseServlet {

    private static final String ROLE_ADMIN = "ADMINISTRATOR";
    private static final String ROLE_PRINCIPAL = "PRINCIPAL";
    private static final String ROLE_TEACHER = "TEACHER";
    private static final String ROLE_STUDENT = "STUDENT";
    private static final String ROLE_PARENT = "PARENT";

    private final TeacherService teacherService;

    public TeacherController() {
        this.teacherService = new TeacherServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = pathInfo(req);

        switch (path) {
            case "/me": {
                long userId = AuthContext.getUserId(req);
                Teacher teacher = teacherService.getTeacherByUserId(userId);
                RoleGuard.requireUserOrRole(req, userId, ROLE_ADMIN, ROLE_PRINCIPAL);
                writeJson(resp, teacher);
                return;
            }
            case "/":
            case "": {
                RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL);
                writeJson(resp, teacherService.getAllTeachers());
                return;
            }
            default:
                if (path.startsWith("/class-teachers")) {
                    RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL);
                    writeJson(resp, teacherService.getAllClassTeachers());
                    return;
                }
                if (path.startsWith("/search/")) {
                    RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL);
                    writeJson(resp, teacherService.searchTeachersByNameEmpId(path.substring("/search/".length())));
                    return;
                }
                if (path.startsWith("/subject/")) {
                    RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL);
                    writeJson(resp, teacherService.getTeachersBySubject(parseInt(path.substring("/subject/".length()))));
                    return;
                }
                if (path.startsWith("/section/")) {
                    RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER, ROLE_STUDENT, ROLE_PARENT);
                    String[] parts = path.substring("/section/".length()).split("/");
                    if (parts.length != 2)
                        throw new ValidationException("Expected /section/{sectionId}/{academicYearId}.");
                    writeJson(resp, teacherService.getTeachersBySection(parseInt(parts[0]), parseInt(parts[1])));
                    return;
                }
                if (path.startsWith("/user/")) {
                    RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL);
                    long userId = parseLong(path.substring("/user/".length()));
                    writeJson(resp, teacherService.getTeacherByUserId(userId));
                    return;
                }
                if (path.startsWith("/employee/")) {
                    RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL);
                    writeJson(resp, teacherService.getTeacherByEmployeeId(path.substring("/employee/".length())));
                    return;
                }
                if (path.startsWith("/email/")) {
                    RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL);
                    writeJson(resp, teacherService.getTeacherByEmail(path.substring("/email/".length())));
                    return;
                }
                if (path.startsWith("/count")) {
                    RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL);
                    writeJson(resp, teacherService.getTeacherCount());
                    return;
                }
                if (path.startsWith("/")) {
                    long teacherId = parseLong(path.substring(1));
                    Teacher teacher = teacherService.getTeacherById(teacherId);
                    RoleGuard.requireUserOrRole(req, teacher.getUserId(), ROLE_ADMIN, ROLE_PRINCIPAL);
                    writeJson(resp, teacher);
                    return;
                }
                throw new ValidationException("Unsupported path: " + path);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN);
        CreateTeacherRequest request = readBody(req, CreateTeacherRequest.class);
        if (request == null || request.getTeacher() == null)
            throw new ValidationException("Request body with teacher details is required.");

        Teacher created = teacherService.createTeacher(request.toUser(), request.getPassword(), request.getTeacher());
        writeJson(resp, created, "Teacher created.");
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long teacherId = parseLong(pathInfo(req).substring(1));
        Teacher existing = teacherService.getTeacherById(teacherId);
        RoleGuard.requireUserOrRole(req, existing.getUserId(), ROLE_ADMIN, ROLE_PRINCIPAL);
        Teacher teacher = readBody(req, Teacher.class);
        if (teacher == null)
            throw new ValidationException("Request body is required.");
        teacher.setTeacherId(teacherId);
        teacherService.updateTeacher(teacher);
        writeStatusMessage(resp, "Teacher updated.");
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long teacherId = parseLong(pathInfo(req).substring(1));
        Teacher existing = teacherService.getTeacherById(teacherId);
        RoleGuard.requireUserOrRole(req, existing.getUserId(), ROLE_ADMIN, ROLE_PRINCIPAL);
        teacherService.deactivateTeacher(teacherId);
        writeStatusMessage(resp, "Teacher deactivated.");
    }
}