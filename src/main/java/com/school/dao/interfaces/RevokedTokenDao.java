package com.school.dao.interfaces;

import java.time.LocalDateTime;

public interface RevokedTokenDao {
    boolean revoke(String jti, LocalDateTime expiresAt);
    boolean isRevoked(String jti);
    int deleteExpired();
}
