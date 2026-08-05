package com.school.validations;

import com.school.exceptions.ValidationException;

public interface ValidatorUtil {

    static String validateEmail(String email) {
        if (email == null || email.isBlank())
            throw new ValidationException("Email is required.");
        if (!email.endsWith("@gmail.com"))
            throw new ValidationException("Invalid Email.");

        return email.toLowerCase();
    }

    static void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new ValidationException("Password is required.");
        }

        if (password.length() < 8) {
            throw new ValidationException("Password must be at least 8 characters long.");
        }

        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUppercase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowercase = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (!Character.isWhitespace(c)) { // Treats any symbol as special
                hasSpecial = true;
            }
        }

        if (!hasUppercase) {
            throw new ValidationException("Password must contain at least one uppercase letter.");
        }
        if (!hasLowercase) {
            throw new ValidationException("Password must contain at least one lowercase letter.");
        }
        if (!hasDigit) {
            throw new ValidationException("Password must contain at least one number.");
        }
        if (!hasSpecial) {
            throw new ValidationException("Password must contain at least one special character.");
        }
    }

    static String validateUsername(String username) {
        validateRequired(username, "username");
        for (char c : username.toCharArray()) {
            if (!Character.isLetterOrDigit(c) && !(c=='-' || c=='_'))
                throw new ValidationException("Username not allowed");
        }
        return username.toLowerCase();
    }

    static String validateRequired(String value, String fieldName) {
        if (value == null || value.isBlank())
            throw new ValidationException(fieldName + " is required.");
        return value.trim();
    }

    static void validateId(long... id) {
        for (long i : id)
            if (i <= 0)
                throw new ValidationException("Invalid identifier. ID must be a positive number.");
    }

    static int validateId(int id) {
        if (id <= 0)
            throw new ValidationException("Invalid identifier. ID must be a positive number.");
        return id;
    }

    static String validateName(String name, String fieldName) {
        String value = validateRequired(name, fieldName);
        if (!value.matches("[a-zA-Z][a-zA-Z .'-]*"))
            throw new ValidationException(fieldName + " must contain only letters.");
        return value;
    }

    static String validatePhone(String phone) {
        String value = validateRequired(phone, "Phone");
        if (value.startsWith("+"))
            value = value.substring(1);
        if (!value.matches("[0-9]{10,15}"))
            throw new ValidationException("Invalid phone number.");
        if (value.startsWith("03")) {
            if (value.length() != 11)
                throw new ValidationException("Invalid phone number.");
            return "92" + value.substring(1);
        }
        else if (value.startsWith("92")) {
            if (value.length() != 12)
                throw new ValidationException("Invalid phone number.");
            return value;
        } else {
            throw new ValidationException("Invalid phone number.");
        }
    }

    static void validateDateRange(java.time.LocalDate start, java.time.LocalDate end, String fieldName) {
        if (start == null || end == null)
            throw new ValidationException(fieldName + " requires both start and end dates.");
        if (end.isBefore(start))
            throw new ValidationException(fieldName + " end date cannot be before start date.");
    }
}
