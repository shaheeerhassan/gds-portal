package com.school.web.controller;

import com.school.web.dto.ApiResponse;
import com.school.web.exception.GlobalExceptionHandler;
import com.school.web.util.JsonUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public abstract class BaseServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            super.service(req, resp);
        } catch (Exception e) {
            GlobalExceptionHandler.handle(resp, e);
        }
    }

    protected String readBody(HttpServletRequest req) throws IOException {
        StringBuilder body = new StringBuilder();
        try (var reader = req.getReader()) {
            char[] buffer = new char[4096];
            int read;
            while ((read = reader.read(buffer)) != -1)
                body.append(buffer, 0, read);
        }
        return body.toString();
    }

    protected <T> T readBody(HttpServletRequest req, Class<T> type) throws IOException {
        return JsonUtil.parse(readBody(req), type);
    }

    protected <T> void writeJson(HttpServletResponse resp, T data) throws IOException {
        JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, ApiResponse.success(data));
    }

    protected <T> void writeJson(HttpServletResponse resp, T data, String message) throws IOException {
        JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, ApiResponse.success(data, message));
    }

    protected void writeStatusMessage(HttpServletResponse resp, String message) throws IOException {
        JsonUtil.writeJson(resp, HttpServletResponse.SC_OK, ApiResponse.success(null, message));
    }

    protected String pathInfo(HttpServletRequest req) {
        return req.getPathInfo() != null ? req.getPathInfo() : "/";
    }

    protected long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new com.school.exceptions.ValidationException("Invalid numeric value: " + value);
        }
    }

    protected int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new com.school.exceptions.ValidationException("Invalid numeric value: " + value);
        }
    }
}
