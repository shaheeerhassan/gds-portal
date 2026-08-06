package com.school.web.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.school.exceptions.AccountDisableException;
import com.school.exceptions.BusinessRuleException;
import com.school.exceptions.DaoException;
import com.school.exceptions.DuplicateResourceException;
import com.school.exceptions.ResourceNotFoundException;
import com.school.exceptions.UnauthorizedException;
import com.school.exceptions.ValidationException;
import com.school.web.dto.ApiResponse;
import com.school.web.util.JsonUtil;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public final class GlobalExceptionHandler {

    private GlobalExceptionHandler() {
    }

    public static void handle(HttpServletResponse response, Exception e) throws IOException {
        if (e instanceof JsonProcessingException) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid request body.");
        } else if (e instanceof ValidationException) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } else if (e instanceof UnauthorizedException) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
        } else if (e instanceof AccountDisableException) {
            writeError(response, HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } else if (e instanceof ResourceNotFoundException) {
            writeError(response, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } else if (e instanceof DuplicateResourceException) {
            writeError(response, HttpServletResponse.SC_CONFLICT, e.getMessage());
        } else if (e instanceof BusinessRuleException) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } else if (e instanceof DaoException) {
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "A database error occurred.");
        } else {
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
        }
    }

    private static void writeError(HttpServletResponse response, int status, String message) throws IOException {
        JsonUtil.writeJson(response, status, ApiResponse.error(message, status));
    }
}
