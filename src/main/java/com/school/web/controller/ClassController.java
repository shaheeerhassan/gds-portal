package com.school.web.controller;

import com.school.exceptions.ValidationException;
import com.school.model.Class;
import com.school.service.impl.ClassServiceImpl;
import com.school.service.interfaces.ClassService;
import com.school.web.auth.RoleGuard;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = "/api/classes/*")
public class ClassController extends BaseServlet {

    private static final String ROLE_ADMIN = "ADMINISTRATOR";
    private static final String ROLE_PRINCIPAL = "PRINCIPAL";
    private static final String ROLE_TEACHER = "TEACHER";

    private final ClassService classService;

    public ClassController() {
        this.classService = new ClassServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = pathInfo(req);

        switch (path) {
            case "/":
            case "":
                RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
                writeJson(resp, classService.getAllClasses());
                return;
            default:
                if (path.startsWith("/level/")) {
                    RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
                    writeJson(resp, classService.getClassByNumericLevel(parseInt(path.substring("/level/".length()))));
                    return;
                }
                if (path.startsWith("/")) {
                    RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
                    writeJson(resp, classService.getClassById(parseInt(path.substring(1))));
                    return;
                }
                throw new ValidationException("Unsupported path: " + path);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN);
        Class c = readBody(req, Class.class);
        if (c == null)
            throw new ValidationException("Request body is required.");
        writeJson(resp, classService.createClass(c), "Class created.");
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN);
        int classId = parseInt(pathInfo(req).substring(1));
        Class c = readBody(req, Class.class);
        if (c == null)
            throw new ValidationException("Request body is required.");
        c.setClassId(classId);
        classService.updateClass(c);
        writeStatusMessage(resp, "Class updated.");
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN);
        int classId = parseInt(pathInfo(req).substring(1));
        classService.deleteClass(classId);
        writeStatusMessage(resp, "Class deleted.");
    }
}
