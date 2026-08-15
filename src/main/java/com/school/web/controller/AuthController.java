package com.school.web.controller;

import com.school.exceptions.ValidationException;
import com.school.model.Role;
import com.school.model.User;
import com.school.service.impl.AuthenticationServiceImpl;
import com.school.service.impl.RefreshTokenServiceImpl;
import com.school.service.impl.RevokedTokenServiceImpl;
import com.school.service.interfaces.AuthenticationService;
import com.school.service.interfaces.RefreshTokenService;
import com.school.service.interfaces.RevokedTokenService;
import com.school.web.auth.AuthContext;
import com.school.web.auth.JwtUtil;
import com.school.web.dto.request.ChangePasswordRequest;
import com.school.web.dto.request.LoginRequest;
import com.school.web.dto.request.LogoutRequest;
import com.school.web.dto.request.RefreshTokenRequest;
import com.school.web.dto.request.RequestPasswordResetRequest;
import com.school.web.dto.request.ResetPasswordRequest;
import com.school.web.dto.response.LoginResponse;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

@WebServlet(urlPatterns = "/api/auth/*")
public class AuthController extends BaseServlet {

    private final AuthenticationService authenticationService;
    private final RefreshTokenService refreshTokenService;
    private final RevokedTokenService revokedTokenService;

    public AuthController() {
        this.authenticationService = new AuthenticationServiceImpl();
        this.refreshTokenService = new RefreshTokenServiceImpl();
        this.revokedTokenService = new RevokedTokenServiceImpl();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        if (path == null)
            throw new ValidationException("Unsupported auth action.");

        switch (path) {
            case "/login":
                login(req, resp);
                break;
            case "/request-password-reset":
                requestPasswordReset(req, resp);
                break;
            case "/reset-password":
                resetPassword(req, resp);
                break;
            case "/change-password":
                changePassword(req, resp);
                break;
            case "/refresh":
                refresh(req, resp);
                break;
            case "/logout":
                logout(req, resp);
                break;
            default:
                throw new ValidationException("Unsupported auth action: " + path);
        }
    }

    private void login(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        LoginRequest request = readBody(req, LoginRequest.class);
        if (request == null)
            throw new ValidationException("Request body is required.");

        User user = authenticationService.login(request.getEmail(), request.getPassword());

        Map<String, Object> profile = authenticationService.getUserProfile(user.getUserId());
        String roleName = extractRoleName(profile);

        long expiresInMillis = tokenExpiryMillis();
        String token = JwtUtil.generateToken(user.getUserId(), roleName, System.currentTimeMillis() + expiresInMillis);
        String refreshToken = refreshTokenService.generateToken(user.getUserId());

        LoginResponse response = new LoginResponse(token, expiresInMillis, refreshToken, user, profile);
        writeJson(resp, response, "Login successful.");
    }

    private void requestPasswordReset(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RequestPasswordResetRequest request = readBody(req, RequestPasswordResetRequest.class);
        if (request == null)
            throw new ValidationException("Request body is required.");

        authenticationService.requestPasswordReset(request.getEmail());

        writeStatusMessage(resp, true, "If an account exists for this email, a reset link has been initiated.");
    }

    private void resetPassword(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ResetPasswordRequest request = readBody(req, ResetPasswordRequest.class);
        if (request == null)
            throw new ValidationException("Request body is required.");

        authenticationService.resetPassword(request.getToken(), request.getNewPassword());
        writeStatusMessage(resp, true, "Password reset successfully.");
    }

    private void changePassword(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long userId = AuthContext.getUserId(req);
        if (userId == 0)
            throw new ValidationException("Authentication required.");

        ChangePasswordRequest request = readBody(req, ChangePasswordRequest.class);
        if (request == null)
            throw new ValidationException("Request body is required.");

        authenticationService.changePassword(userId, request.getCurrentPassword(), request.getNewPassword());
        writeStatusMessage(resp, true, "Password changed successfully.");
    }

    private void refresh(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RefreshTokenRequest request = readBody(req, RefreshTokenRequest.class);
        if (request == null)
            throw new ValidationException("Request body is required.");

        Map<String, Object> result = refreshTokenService.refresh(request.getRefreshToken());
        long userId = ((Number) result.get("userId")).longValue();
        String newRefreshToken = (String) result.get("refreshToken");

        Map<String, Object> profile = authenticationService.getUserProfile(userId);
        String roleName = extractRoleName(profile);

        long expiresInMillis = tokenExpiryMillis();
        String token = JwtUtil.generateToken(userId, roleName, System.currentTimeMillis() + expiresInMillis);

        LoginResponse response = new LoginResponse(token, expiresInMillis, newRefreshToken, null, null);
        writeJson(resp, response, "Token refreshed.");
    }

    private void logout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        LogoutRequest request = readBody(req, LogoutRequest.class);
        if (request == null)
            throw new ValidationException("Request body is required.");

        refreshTokenService.revoke(request.getRefreshToken());

        String accessToken = request.getAccessToken();
        if (accessToken != null && !accessToken.isBlank()) {
            JwtUtil.TokenClaims claims = JwtUtil.parse(accessToken);
            if (claims != null) {
                LocalDateTime expiresAt = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(claims.getExpiresAt()), ZoneId.systemDefault());
                revokedTokenService.revoke(claims.getJti(), expiresAt);
            }
        }

        writeStatusMessage(resp, true, "Logged out successfully.");
    }

    private String extractRoleName(Map<String, Object> profile) {
        Object role = profile.get("role");
        if (role instanceof Role)
            return ((Role) role).getRoleName();
        return "UNKNOWN";
    }

    private long tokenExpiryMillis() {
        String value = System.getenv().getOrDefault("JWT_EXPIRY_HOURS", "24");
        try {
            return Long.parseLong(value) * 60 * 60 * 1000;
        } catch (NumberFormatException e) {
            return 24L * 60 * 60 * 1000;
        }
    }

    private void writeStatusMessage(HttpServletResponse resp, boolean success, String message) throws IOException {
        if (success) {
            com.school.web.util.JsonUtil.writeJson(resp, HttpServletResponse.SC_OK,
                    com.school.web.dto.ApiResponse.success(null, message));
        } else {
            com.school.web.util.JsonUtil.writeJson(resp, HttpServletResponse.SC_BAD_REQUEST,
                    com.school.web.dto.ApiResponse.error(message, HttpServletResponse.SC_BAD_REQUEST));
        }
    }
}
