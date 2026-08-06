package com.school.service.interfaces;

import java.util.Map;

public interface RefreshTokenService {
    String generateToken(long userId);
    Map<String, Object> refresh(String refreshToken);
    void revoke(String refreshToken);
}
