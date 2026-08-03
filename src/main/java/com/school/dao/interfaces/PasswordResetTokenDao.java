package com.school.dao.interfaces;

import com.school.model.PasswordResetToken;

public interface PasswordResetTokenDao {
    boolean insertToken(PasswordResetToken token);
    PasswordResetToken getToken(String tokenValue);
    boolean markTokenAsUsed(String tokenValue);
    boolean invalidateUserTokens(long userId);
    boolean invalidateExpiredTokens();
}