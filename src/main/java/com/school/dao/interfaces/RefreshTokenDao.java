package com.school.dao.interfaces;

import com.school.model.RefreshToken;

public interface RefreshTokenDao {
    boolean insertToken(RefreshToken token);
    RefreshToken getToken(String tokenValue);
    boolean markTokenAsUsed(String tokenValue);
    boolean invalidateUserTokens(long userId);
    boolean invalidateExpiredTokens();
}
