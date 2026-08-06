package com.school.web.auth;

import jakarta.servlet.http.HttpServletRequest;

public final class AuthContext {

    public static final String ATTR_USER_ID = "authUserId";
    public static final String ATTR_ROLE = "authRole";

    private AuthContext() {
    }

    public static long getUserId(HttpServletRequest request) {
        Object value = request.getAttribute(ATTR_USER_ID);
        return value instanceof Long ? (Long) value : 0L;
    }

    public static String getRole(HttpServletRequest request) {
        Object value = request.getAttribute(ATTR_ROLE);
        return value instanceof String ? (String) value : null;
    }

    public static boolean isAuthenticated(HttpServletRequest request) {
        return getUserId(request) > 0;
    }

    public static boolean hasRole(HttpServletRequest request, String role) {
        return role != null && role.equals(getRole(request));
    }
}
