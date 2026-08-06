package com.school.web.controller;

import com.school.exceptions.ValidationException;
import com.school.model.Parent;
import com.school.model.StudentParentLink;
import com.school.service.impl.ParentServiceImpl;
import com.school.service.interfaces.ParentService;
import com.school.web.auth.AuthContext;
import com.school.web.auth.OwnershipGuard;
import com.school.web.auth.RoleGuard;
import com.school.web.dto.request.CreateParentRequest;
import com.school.web.dto.request.LinkParentRequest;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = "/api/parents/*")
public class ParentController extends BaseServlet {

    private static final String ROLE_ADMIN = "ADMINISTRATOR";
    private static final String ROLE_PRINCIPAL = "PRINCIPAL";
    private static final String ROLE_PARENT = "PARENT";

    private final ParentService parentService;

    public ParentController() {
        this.parentService = new ParentServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = pathInfo(req);

        switch (path) {
            case "/me": {
                long userId = AuthContext.getUserId(req);
                Parent parent = parentService.getParentByUserId(userId);
                RoleGuard.requireUserOrRole(req, userId, ROLE_ADMIN, ROLE_PRINCIPAL);
                writeJson(resp, parent);
                return;
            }
            case "/":
            case "": {
                RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL);
                throw new ValidationException("List all parents is not supported. Use /parents/student/{studentId}.");
            }
            default:
                if (path.startsWith("/student/")) {
                    long studentId = parseLong(path.substring("/student/".length()));
                    if (AuthContext.hasRole(req, ROLE_PARENT)) {
                        OwnershipGuard.requireLinkedParent(req, parentService, studentId);
                    } else {
                        RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL);
                    }
                    writeJson(resp, parentService.getParentsByStudentId(studentId));
                    return;
                }
                if (path.startsWith("/user/")) {
                    long userId = parseLong(path.substring("/user/".length()));
                    Parent parent = parentService.getParentByUserId(userId);
                    RoleGuard.requireUserOrRole(req, userId, ROLE_ADMIN, ROLE_PRINCIPAL);
                    writeJson(resp, parent);
                    return;
                }
                if (path.startsWith("/")) {
                    long parentId = parseLong(path.substring(1));
                    Parent parent = parentService.getParentById(parentId);
                    RoleGuard.requireUserOrRole(req, parent.getUserId(), ROLE_ADMIN, ROLE_PRINCIPAL);
                    writeJson(resp, parent);
                    return;
                }
                throw new ValidationException("Unsupported path: " + path);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = pathInfo(req);
        if ("/link".equals(path)) {
            RoleGuard.requireRole(req, ROLE_ADMIN);
            LinkParentRequest request = readBody(req, LinkParentRequest.class);
            if (request == null || request.getStudentId() == 0 || request.getParentId() == 0)
                throw new ValidationException("studentId and parentId are required.");
            parentService.linkParentToStudent(request.getStudentId(), request.getParentId(),
                    request.getRelationshipType(), request.isPrimaryContact());
            writeStatusMessage(resp, "Parent linked to student.");
            return;
        }

        RoleGuard.requireRole(req, ROLE_ADMIN);
        CreateParentRequest request = readBody(req, CreateParentRequest.class);
        if (request == null || request.getParent() == null)
            throw new ValidationException("Request body with parent details is required.");

        Parent created = parentService.createParent(request.toUser(), request.getPassword(), request.getParent());
        writeJson(resp, created, "Parent created.");
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN);
        long parentId = parseLong(pathInfo(req).substring(1));
        Parent parent = readBody(req, Parent.class);
        if (parent == null)
            throw new ValidationException("Request body is required.");
        parent.setParentId(parentId);
        parentService.updateParent(parent);
        writeStatusMessage(resp, "Parent updated.");
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = pathInfo(req);
        if (path.startsWith("/link/")) {
            RoleGuard.requireRole(req, ROLE_ADMIN);
            String[] parts = path.substring("/link/".length()).split("/");
            if (parts.length != 2)
                throw new ValidationException("Expected /link/{parentId}/{studentId}.");
            parentService.unlinkParentFromStudent(parseLong(parts[0]), parseLong(parts[1]));
            writeStatusMessage(resp, "Parent unlinked from student.");
            return;
        }

        RoleGuard.requireRole(req, ROLE_ADMIN);
        long parentId = parseLong(path.substring(1));
        parentService.deactivateParent(parentId);
        writeStatusMessage(resp, "Parent deactivated.");
    }
}
