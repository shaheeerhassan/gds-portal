package com.school.web.auth;

import com.school.exceptions.UnauthorizedException;

import jakarta.servlet.http.HttpServletRequest;

public final class RoleGuard {

    private RoleGuard() {
    }

    public static void requireRole(HttpServletRequest request, String... allowedRoles) {
        String role = AuthContext.getRole(request);
        if (role == null)
            throw new UnauthorizedException("Authentication required.");

        for (String allowed : allowedRoles) {
            if (allowed.equals(role))
                return;
        }
        throw new UnauthorizedException("Access denied. Required role: " + String.join(" or ", allowedRoles));
    }

    public static void requireUserOrRole(HttpServletRequest request, long targetUserId, String... allowedRoles) {
        long authUserId = AuthContext.getUserId(request);
        if (authUserId == targetUserId)
            return;
        requireRole(request, allowedRoles);
    }
}
