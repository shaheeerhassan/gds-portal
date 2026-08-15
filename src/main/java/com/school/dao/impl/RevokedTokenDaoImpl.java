package com.school.dao.impl;

import com.school.dao.interfaces.RevokedTokenDao;
import com.school.exceptions.DaoException;
import static com.school.config.DBConfig.*;

import java.sql.*;
import java.time.LocalDateTime;

public class RevokedTokenDaoImpl implements RevokedTokenDao {

    private static final String INSERT = "INSERT INTO revoked_tokens (jti, expires_at, created_at) VALUES (?, ?, ?)";
    private static final String SELECT_BY_JTI = "SELECT COUNT(*) FROM revoked_tokens WHERE jti = ?";
    private static final String DELETE_EXPIRED = "DELETE FROM revoked_tokens WHERE expires_at < ?";

    @Override
    public boolean revoke(String jti, LocalDateTime expiresAt) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(INSERT)) {

            ps.setString(1, jti);
            if (expiresAt != null) {
                ps.setTimestamp(2, Timestamp.valueOf(expiresAt));
            } else {
                ps.setNull(2, Types.TIMESTAMP);
            }
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));

            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DaoException("Error revoking access token", e);
        }
    }

    @Override
    public boolean isRevoked(String jti) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_JTI)) {

            ps.setString(1, jti);

            try (ResultSet resultSet = ps.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new DaoException("Error checking revoked access token", e);
        }
    }

    @Override
    public int deleteExpired() {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(DELETE_EXPIRED)) {

            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));

            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Error deleting expired revoked tokens", e);
        }
    }
}
