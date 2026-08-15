package com.school.service.interfaces;

import com.school.model.User;

import java.sql.Connection;
import java.util.List;

public interface UserService {
    User createUser(User user, String password);
    User createUser(User user, String password, Connection connection);
    User getUserById(long userId);
    User getUserByEmail(String email);
    User getUserByUsername(String username);
    List<User> getAllUsers();
    List<User> getUsersByRole(int roleId);
    void updateUser(User user);
    void updateUserStatus(long userId, boolean isActive);
    void deleteUser(long userId);
    void updateProfilePicture(long userId, String profilePictureUrl);
}
