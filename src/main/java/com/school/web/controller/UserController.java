package com.school.web.controller;

import com.school.exceptions.ValidationException;
import com.school.model.User;
import com.school.service.impl.UserServiceImpl;
import com.school.service.interfaces.UserService;
import com.school.web.auth.AuthContext;
import com.school.web.auth.RoleGuard;
import com.school.web.dto.request.CreateUserRequest;
import com.school.web.dto.request.UpdateProfilePictureRequest;
import com.school.web.dto.request.UpdateUserRequest;
import com.school.web.dto.request.UpdateUserStatusRequest;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = "/api/users/*")
public class UserController extends BaseServlet {

    private static final String ROLE_ADMIN = "ADMINISTRATOR";
    private static final String ROLE_PRINCIPAL = "PRINCIPAL";

    private final UserService userService;

    public UserController() {
        this.userService = new UserServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = pathInfo(req);

        if ("/me".equals(path)) {
            long userId = AuthContext.getUserId(req);
            if (userId == 0)
                throw new ValidationException("Authentication required.");
            writeJson(resp, userService.getUserById(userId));
            return;
        }

        RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL);

        switch (path) {
            case "/":
            case "":
                writeJson(resp, userService.getAllUsers());
                break;
            default:
                if (path.startsWith("/role/")) {
                    int roleId = parsePathId(path, "/role/");
                    writeJson(resp, userService.getUsersByRole(roleId));
                } else if (path.startsWith("/email/")) {
                    String email = path.substring("/email/".length());
                    writeJson(resp, userService.getUserByEmail(email));
                } else if (path.startsWith("/username/")) {
                    String username = path.substring("/username/".length());
                    writeJson(resp, userService.getUserByUsername(username));
                } else {
                    long userId = parseId(path);
                    writeJson(resp, userService.getUserById(userId));
                }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN);
        CreateUserRequest request = readBody(req, CreateUserRequest.class);
        if (request == null || request.getPassword() == null || request.getPassword().isBlank())
            throw new ValidationException("password is required.");
        if (request.getRoleId() == 0)
            throw new ValidationException("roleId is required.");
        writeJson(resp, userService.createUser(request.toUser(), request.getPassword()), "User created.");
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = pathInfo(req);

        if (path.startsWith("/profile-picture/")) {
            long userId = parseLong(path.substring("/profile-picture/".length()));
            RoleGuard.requireUserOrRole(req, userId, ROLE_ADMIN);
            UpdateProfilePictureRequest request = readBody(req, UpdateProfilePictureRequest.class);
            if (request == null || request.getProfilePictureUrl() == null || request.getProfilePictureUrl().isBlank())
                throw new ValidationException("profilePictureUrl is required.");
            userService.updateProfilePicture(userId, request.getProfilePictureUrl());
            writeStatusMessage(resp, "Profile picture updated.");
            return;
        }

        if ("/status".equals(path)) {
            RoleGuard.requireRole(req, ROLE_ADMIN);
            UpdateUserStatusRequest request = readBody(req, UpdateUserStatusRequest.class);
            if (request == null || request.getUserId() == 0)
                throw new ValidationException("userId is required.");
            userService.updateUserStatus(request.getUserId(), request.isActive());
            writeStatusMessage(resp, "User status updated.");
            return;
        }

        long userId = parseId(path);
        RoleGuard.requireUserOrRole(req, userId, ROLE_ADMIN);
        UpdateUserRequest request = readBody(req, UpdateUserRequest.class);
        if (request == null)
            throw new ValidationException("Request body is required.");

        User user = new User();
        user.setUserId(userId);
        user.setRoleId(request.getRoleId());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setProfilePictureUrl(request.getProfilePictureUrl());

        userService.updateUser(user);
        writeStatusMessage(resp, "User updated.");
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN);
        long userId = parseId(pathInfo(req));
        userService.deleteUser(userId);
        writeStatusMessage(resp, "User deleted.");
    }

    private long parseId(String path) {
        String id = path.substring(1);
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            throw new ValidationException("Invalid user id.");
        }
    }

    private int parsePathId(String path, String prefix) {
        String id = path.substring(prefix.length());
        try {
            return Integer.parseInt(id);
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid id.");
        }
    }
}

