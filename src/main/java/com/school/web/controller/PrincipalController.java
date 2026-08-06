package com.school.web.controller;

import com.school.exceptions.ValidationException;
import com.school.model.Principal;
import com.school.service.impl.PrincipalServiceImpl;
import com.school.service.interfaces.PrincipalService;
import com.school.web.auth.RoleGuard;
import com.school.web.dto.request.CreatePrincipalRequest;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = "/api/principals/*")
public class PrincipalController extends BaseServlet {

    private static final String ROLE_ADMIN = "ADMINISTRATOR";
    private static final String ROLE_PRINCIPAL = "PRINCIPAL";

    private final PrincipalService principalService;

    public PrincipalController() {
        this.principalService = new PrincipalServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL);
        String path = pathInfo(req);

        switch (path) {
            case "/":
            case "":
                writeJson(resp, principalService.getAllPrincipals());
                break;
            default:
                if (path.startsWith("/user/")) {
                    writeJson(resp, principalService.getPrincipalByUserId(parseLong(path.substring("/user/".length()))));
                } else {
                    writeJson(resp, principalService.getPrincipalById(parseLong(path.substring(1))));
                }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN);
        CreatePrincipalRequest request = readBody(req, CreatePrincipalRequest.class);
        if (request == null || request.getPrincipal() == null)
            throw new ValidationException("Request body with principal details is required.");

        Principal created = principalService.createPrincipal(request.toUser(), request.getPassword(), request.getPrincipal());
        writeJson(resp, created, "Principal created.");
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN);
        long principalId = parseLong(pathInfo(req).substring(1));
        Principal principal = readBody(req, Principal.class);
        if (principal == null)
            throw new ValidationException("Request body is required.");
        principal.setPrincipalId(principalId);
        principalService.updatePrincipal(principal);
        writeStatusMessage(resp, "Principal updated.");
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN);
        long principalId = parseLong(pathInfo(req).substring(1));
        principalService.deactivatePrincipal(principalId);
        writeStatusMessage(resp, "Principal deactivated.");
    }
}
