package com.school.dao.impl;

import com.school.dao.interfaces.UserDao;
import com.school.exceptions.DaoException;
import com.school.model.User;
import static com.school.config.DBConfig.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserDaoImpl implements UserDao {

    private static final String INSERT = "INSERT INTO users (role_id, username, email, password_hash, profile_picture_url, is_active, last_login_at, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
            if (user.getLastLoginAt() != null) {
                ps.setTimestamp(7, Timestamp.valueOf(user.getLastLoginAt()));
            } else {
                ps.setNull(7, Types.TIMESTAMP);
            }
            LocalDateTime creationAndUpdateTime = user.getCreatedAt() == null || user.getUpdatedAt() == null ? LocalDateTime.now() : user.getCreatedAt();
            ps.setTimestamp(8, Timestamp.valueOf(creationAndUpdateTime));
            ps.setTimestamp(9, Timestamp.valueOf(creationAndUpdateTime));

            int success = ps.executeUpdate();

            try (ResultSet resultSet = ps.getGeneratedKeys()) {
                if (resultSet.next()) {
                    user.setUserId(resultSet.getLong("user_id"));
                    user.setCreatedAt(creationAndUpdateTime);
                    user.setUpdatedAt(creationAndUpdateTime);
                }
            }

            return success == 1;
        } catch (SQLException e) {
            throw new DaoException("Error inserting user", e);
        }
    }

    @Override
    public User getUserById(long userId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_ID)) {

            ps.setLong(1, userId);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next())
                    return mapRow(resultSet);
            }
            return null;
        } catch (SQLException e) {
            throw new DaoException("Error fetching user", e);
        }
    }

    @Override
    public User getUserByUsername(String username) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_USERNAME)) {

            ps.setString(1, username);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next())
                    return mapRow(resultSet);
            }
            return null;
        } catch (SQLException e) {
            throw new DaoException("Error fetching user", e);
        }
    }

    @Override
    public User getUserByEmail(String email) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_EMAIL)) {

            ps.setString(1, email);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next())
                    return mapRow(resultSet);
            }
            return null;
        } catch (SQLException e) {
            throw new DaoException("Error fetching user", e);
        }
    }

    @Override
    public List<User> getAllUsers() {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_ALL);
             ResultSet resultSet = ps.executeQuery()) {

            List<User> users = new ArrayList<>();
            while (resultSet.next())
                users.add(mapRow(resultSet));
            return users;
        } catch (SQLException e) {
            throw new DaoException("Error fetching users", e);
        }
    }

    @Override
    public List<User> getUsersByRole(int roleId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_ROLE)) {

            ps.setInt(1, roleId);
            try (ResultSet resultSet = ps.executeQuery()) {
                List<User> users = new ArrayList<>();
                while (resultSet.next())
                    users.add(mapRow(resultSet));
                return users;
            }
        } catch (SQLException e) {
            throw new DaoException("Error fetching users", e);
        }
    }

    @Override
    public boolean updateUser(User user) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(UPDATE_USER)) {

            ps.setInt(1, user.getRoleId());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getProfilePictureUrl());
            ps.setBoolean(5, user.isActive());
            ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(7, user.getUserId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error updating user", e);
        }
    }

    @Override
    public boolean deleteUser(long userId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(DELETE_USER)) {

            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(2, userId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error deleting user", e);
        }
    }

    @Override
    public boolean updatePasswordHash(long userId, String newPasswordHash) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(UPDATE_PASSWORD_HASH)) {

            ps.setString(1, newPasswordHash);
            ps.setLong(2, userId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error updating password", e);
        }
    }

    @Override
    public boolean updateProfilePicture(long userId, String profilePictureUrl) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(UPDATE_PROFILE_PICTURE)) {

            ps.setString(1, profilePictureUrl);
            ps.setLong(2, userId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error updating profile picture", e);
        }
    }

    @Override
    public boolean updateUserStatus(long userId, boolean isActive) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(UPDATE_STATUS)) {

            ps.setBoolean(1, isActive);
            ps.setLong(2, userId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error updating user status", e);
        }
    }

    @Override
    public boolean updateLastLogin(long userId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(UPDATE_LAST_LOGIN)) {

            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(2, userId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error updating last login", e);
        }
    }

    private User mapRow(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setUserId(resultSet.getLong("user_id"));
        user.setRoleId(resultSet.getInt("role_id"));
        user.setUsername(resultSet.getString("username"));
        user.setEmail(resultSet.getString("email"));
        user.setPasswordHash(resultSet.getString("password_hash"));
        user.setProfilePictureUrl(resultSet.getString("profile_picture_url"));
        user.setActive(resultSet.getBoolean("is_active"));
        Timestamp lastLoginAt = resultSet.getTimestamp("last_login_at");
        if (lastLoginAt != null)
            user.setLastLoginAt(lastLoginAt.toLocalDateTime());
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        if (createdAt != null)
            user.setCreatedAt(createdAt.toLocalDateTime());
        Timestamp updatedAt = resultSet.getTimestamp("updated_at");
        if (updatedAt != null)
            user.setUpdatedAt(updatedAt.toLocalDateTime());
        return user;
    }
}