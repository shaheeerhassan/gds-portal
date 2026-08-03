package com.school.dao.impl;

import com.school.dao.interfaces.UserDao;
import com.school.exception.DaoException;
import com.school.model.User;
import static com.school.config.DBConfig.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

public class UserDaoImpl implements UserDao {

    private static final String INSERT = "INSERT INTO users (role_id, username, email, password_hash, profile_picture_url, is_active, last_login_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_BY_ID = "SELECT * FROM users WHERE user_id = ?";
    private static final String SELECT_BY_USERNAME = "SELECT * FROM users WHERE username = ?";
    private static final String SELECT_BY_EMAIL = "SELECT * FROM users WHERE email = ?";
    private static final String SELECT_ALL = "SELECT * FROM users ORDER BY user_id";
    private static final String SELECT_BY_ROLE = "SELECT * FROM users WHERE role_id = ? ORDER BY user_id";
    private static final String UPDATE_USER = "UPDATE users SET role_id = ?, username = ?, email = ?, profile_picture_url = ?, is_active = ?, updated_at = ? WHERE user_id = ?";
    private static final String DELETE_USER = "UPDATE users SET is_active = FALSE, updated_at = ? WHERE user_id = ?";
    private static final String UPDATE_PASSWORD_HASH = "UPDATE users SET password_hash = ? WHERE user_id = ?";
    private static final String UPDATE_PROFILE_PICTURE = "UPDATE users SET profile_picture_url = ? WHERE user_id = ?";
    private static final String UPDATE_STATUS = "UPDATE users SET is_active = ? WHERE user_id = ?";
    private static final String UPDATE_LAST_LOGIN = "UPDATE users SET last_login_at = ? WHERE user_id = ?";

    @Override
    public boolean insertUser(User user) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, user.getRoleId());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPasswordHash());
            ps.setString(5, user.getProfilePictureUrl());
            ps.setBoolean(6, user.isActive());
            ps.setTimestamp(7, Timestamp.valueOf(user.getLastLoginAt()));

            int success =  ps.executeUpdate();

            try (ResultSet resultSet = ps.getGeneratedKeys()){
                if (resultSet.next())
                    user.setUserId(resultSet.getInt(1));
            }

            return success==1;
        } catch (SQLException e) {
            throw new DaoException("Error login user", e);
        }
    }

    @Override
    public User getUserById(long userId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_ID, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, userId);

            try (ResultSet resultSet = ps.executeQuery()){

                if (resultSet.next()) {
                    User user = new User();

                    user.setUserId(resultSet.getInt(1));
                    user.setRoleId(resultSet.getInt(2));
                    user.setUsername(resultSet.getString(3));
                    user.setEmail(resultSet.getString(4));
                    user.setPasswordHash(resultSet.getString(5));
                    user.setProfilePictureUrl(resultSet.getString(6));
                    user.setActive(resultSet.getBoolean(7));
                    user.setLastLoginAt(LocalDateTime.parse(resultSet.getTimestamp(8).toString()));
                    user.setCreatedAt(LocalDateTime.parse(resultSet.getTimestamp(9).toString()));
                    user.setCreatedAt(LocalDateTime.parse(resultSet.getTimestamp(10).toString()));

                    return user;
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DaoException("Error fetching user", e);
        }
    }

    @Override
    public User getUserByUsername(String username) {
        return null;
    }

    @Override
    public User getUserByEmail(String email) {
        return null;
    }

    @Override
    public List<User> getAllUsers() {
        return List.of();
    }

    @Override
    public List<User> getUsersByRole(int roleId) {
        return List.of();
    }

    @Override
    public boolean updateUser(User user) {
        return false;
    }

    @Override
    public boolean deleteUser(long userId) {
        return false;
    }

    @Override
    public boolean updatePasswordHash(long userId, String newPasswordHash) {
        return false;
    }

    @Override
    public boolean updateProfilePicture(long userId, String profilePictureUrl) {
        return false;
    }

    @Override
    public boolean updateUserStatus(long userId, boolean isActive) {
        return false;
    }

    @Override
    public boolean updateLastLogin(long userId) {
        return false;
    }
}
