package com.school.dao.impl;

import com.school.model.User;

import java.util.List;

public interface UserDao {
    long insertUser(User user);

    User getUserById(long userId);
    User getUserByUsername(String username);
    User getUserByEmail(String email);
    List<User> getAllUsers();
    List<User> getUsersByRole(int roleId);
    boolean updateUser(User user);
    boolean deleteUser(long userId); // Soft delete via is_active

    boolean updatePasswordHash(long userId, String newPasswordHash);
    boolean updateProfilePicture(long userId, String profilePictureUrl);
    boolean updateUserStatus(long userId, boolean isActive);
    boolean updateLastLogin(long userId);
}
