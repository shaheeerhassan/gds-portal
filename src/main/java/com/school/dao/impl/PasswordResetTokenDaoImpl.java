package com.school.dao.impl;

import com.school.dao.interfaces.PasswordResetTokenDao;
import com.school.exception.DaoException;
import com.school.model.PasswordResetToken;
import static com.school.config.DBConfig.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

public class PasswordResetTokenDaoImpl implements PasswordResetTokenDao {

    private static final String INSERT = "INSERT INTO password_reset_tokens (user_id, token_hash, expires_at, is_used, created_at) VALUES (?, ?, ?, ?, ?)";
    private static final String SELECT_BY_TOKEN = "SELECT * FROM password_reset_tokens WHERE token_hash = ?";
    private static final String MARK_USED = "UPDATE password_reset_tokens SET is_used = TRUE WHERE token_hash = ?";
    private static final String INVALIDATE_USER = "UPDATE password_reset_tokens SET is_used = TRUE WHERE user_id = ? AND is_used = FALSE";
    private static final String INVALIDATE_EXPIRED = "UPDATE password_reset_tokens SET is_used = TRUE WHERE expires_at < ? AND is_used = FALSE";

    @Override
    public boolean insertToken(PasswordResetToken token) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, token.getUserId());
            ps.setString(2, token.getTokenHash());
            if (token.getExpiresAt() != null) {
                ps.setTimestamp(3, Timestamp.valueOf(token.getExpiresAt()));
            } else {
                ps.setNull(3, Types.TIMESTAMP);
            }
            ps.setBoolean(4, token.isUsed());
            LocalDateTime now = LocalDateTime.now();
            ps.setTimestamp(5, Timestamp.valueOf(now));

            int success = ps.executeUpdate();

            try (ResultSet resultSet = ps.getGeneratedKeys()) {
                if (resultSet.next()) {
                    token.setTokenId(resultSet.getLong(1));
                    token.setCreatedAt(now);
                }
            }

            return success == 1;
        } catch (SQLException e) {
            throw new DaoException("Error inserting password reset token", e);
        }
    }

    @Override
    public PasswordResetToken getToken(String tokenValue) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_TOKEN)) {

            ps.setString(1, tokenValue);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next())
                    return mapRow(resultSet);
            }
            return null;
        } catch (SQLException e) {
            throw new DaoException("Error fetching password reset token", e);
        }
    }

    @Override
    public boolean markTokenAsUsed(String tokenValue) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(MARK_USED)) {

            ps.setString(1, tokenValue);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error marking token used", e);
        }
    }

    @Override
    public boolean invalidateUserTokens(long userId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(INVALIDATE_USER)) {

            ps.setLong(1, userId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error invalidating user tokens", e);
        }
    }

    @Override
    public boolean invalidateExpiredTokens() {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(INVALIDATE_EXPIRED)) {

            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error invalidating expired tokens", e);
        }
    }

    private PasswordResetToken mapRow(ResultSet resultSet) throws SQLException {
        PasswordResetToken token = new PasswordResetToken();
        token.setTokenId(resultSet.getLong("token_id"));
        token.setUserId(resultSet.getLong("user_id"));
        token.setTokenHash(resultSet.getString("token_hash"));
        Timestamp expiresAt = resultSet.getTimestamp("expires_at");
        if (expiresAt != null)
            token.setExpiresAt(expiresAt.toLocalDateTime());
        token.setUsed(resultSet.getBoolean("is_used"));
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        if (createdAt != null)
            token.setCreatedAt(createdAt.toLocalDateTime());
        return token;
    }
}