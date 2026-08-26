package com.school.web.auth;

import com.school.service.impl.RevokedTokenServiceImpl;
import com.school.service.interfaces.RevokedTokenService;
import com.school.web.dto.ApiResponse;
import com.school.web.util.JsonUtil;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Set;

@WebFilter(urlPatterns = "/api/*")
public class JwtAuthFilter implements Filter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final RevokedTokenService revokedTokenService = new RevokedTokenServiceImpl();

    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/api/auth/login",
            "/api/auth/request-password-reset",
            "/api/auth/reset-password",
            "/api/auth/refresh",
            "/api/auth/logout",
            "/api/health"
    );

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(servletRequest, servletResponse);
            return;
        }

        String fullPath = request.getServletPath() + (request.getPathInfo() != null ? request.getPathInfo() : "");
        if (fullPath.length() > 1 && fullPath.endsWith("/"))
            fullPath = fullPath.substring(0, fullPath.length() - 1);

        if (isPublicPath(fullPath)) {
            chain.doFilter(servletRequest, servletResponse);
            return;
        }

        String header = request.getHeader(AUTH_HEADER);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            JsonUtil.writeJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                    ApiResponse.error("Missing or invalid Authorization header.", HttpServletResponse.SC_UNAUTHORIZED));
            return;
        }

        String token = header.substring(BEARER_PREFIX.length());
        JwtUtil.TokenClaims claims = JwtUtil.parse(token);
        if (claims == null) {
            JsonUtil.writeJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                    ApiResponse.error("Invalid or expired token.", HttpServletResponse.SC_UNAUTHORIZED));
            return;
        }

        if (revokedTokenService.isRevoked(claims.getJti())) {
            JsonUtil.writeJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                    ApiResponse.error("Token has been revoked.", HttpServletResponse.SC_UNAUTHORIZED));
            return;
        }

        request.setAttribute(AuthContext.ATTR_USER_ID, claims.getUserId());
        request.setAttribute(AuthContext.ATTR_ROLE, claims.getRoleName());

        chain.doFilter(servletRequest, servletResponse);
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.contains(path);
    }
}
