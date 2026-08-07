package com.school.service.interfaces;

import java.time.LocalDateTime;

public interface RevokedTokenService {
    boolean revoke(String jti, LocalDateTime expiresAt);
    boolean isRevoked(String jti);
    void cleanupExpired();
}
