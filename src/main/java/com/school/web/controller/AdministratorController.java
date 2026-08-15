package com.school.web.controller;

import com.school.exceptions.ValidationException;
import com.school.model.Administrator;
import com.school.service.impl.AdministratorServiceImpl;
import com.school.service.interfaces.AdministratorService;
import com.school.web.auth.RoleGuard;
import com.school.web.dto.request.CreateAdministratorRequest;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = "/api/administrators/*")
public class AdministratorController extends BaseServlet {

    private static final String ROLE_ADMIN = "ADMINISTRATOR";
    private static final String ROLE_PRINCIPAL = "PRINCIPAL";

    private final AdministratorService administratorService;

    public AdministratorController() {
        this.administratorService = new AdministratorServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL);
        String path = pathInfo(req);

        switch (path) {
            case "/":
            case "":
                writeJson(resp, administratorService.getAllAdmins());
                break;
            default:
                if (path.startsWith("/user/")) {
                    writeJson(resp, administratorService.getAdminByUserId(parseLong(path.substring("/user/".length()))));
                } else {
                    writeJson(resp, administratorService.getAdministratorById(parseLong(path.substring(1))));
                }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN);
        CreateAdministratorRequest request = readBody(req, CreateAdministratorRequest.class);
        if (request == null || request.getAdministrator() == null)
            throw new ValidationException("Request body with administrator details is required.");

        Administrator created = administratorService.createAdministrator(request.toUser(), request.getPassword(), request.getAdministrator());
        writeJson(resp, created, "Administrator created.");
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN);
        long adminId = parseLong(pathInfo(req).substring(1));
        Administrator administrator = readBody(req, Administrator.class);
        if (administrator == null)
            throw new ValidationException("Request body is required.");
        administrator.setAdminId(adminId);
        administratorService.updateAdministrator(administrator);
        writeStatusMessage(resp, "Administrator updated.");
    }
}
