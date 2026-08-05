package com.school.service.interfaces;

import com.school.model.User;

import java.util.Map;

public interface AuthenticationService {
    User login(String email, String password);
    void changePassword(long userId, String currentPassword, String newPassword);
    boolean requestPasswordReset(String email);
    void resetPassword(String token, String newPassword);
    Map<String, Object> getUserProfile(long userId);
}
