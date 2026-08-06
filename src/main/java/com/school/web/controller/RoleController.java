package com.school.web.controller;

import com.school.exceptions.ValidationException;
import com.school.service.impl.RoleServiceImpl;
import com.school.service.interfaces.RoleService;
import com.school.web.auth.RoleGuard;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = "/api/roles/*")
public class RoleController extends BaseServlet {

    private static final String ROLE_ADMIN = "ADMINISTRATOR";
    private static final String ROLE_PRINCIPAL = "PRINCIPAL";
    private static final String ROLE_TEACHER = "TEACHER";
    private static final String ROLE_STUDENT = "STUDENT";
    private static final String ROLE_PARENT = "PARENT";

    private final RoleService roleService;

    public RoleController() {
        this.roleService = new RoleServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = pathInfo(req);

        switch (path) {
            case "/":
            case "":
                RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER, ROLE_STUDENT, ROLE_PARENT);
                writeJson(resp, roleService.getAllRoles());
                return;
            default:
                if (path.startsWith("/name/")) {
                    writeJson(resp, roleService.getRoleByName(path.substring("/name/".length())));
                    return;
                }
                if (path.startsWith("/")) {
                    writeJson(resp, roleService.getRoleById(parseInt(path.substring(1))));
                    return;
                }
                throw new ValidationException("Unsupported path: " + path);
        }
    }
}
