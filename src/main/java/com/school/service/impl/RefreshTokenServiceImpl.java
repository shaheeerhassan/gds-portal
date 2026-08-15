package com.school.service.impl;

import com.school.dao.impl.RefreshTokenDaoImpl;
import com.school.dao.impl.UserDaoImpl;
import com.school.dao.interfaces.RefreshTokenDao;
import com.school.dao.interfaces.UserDao;
import com.school.exceptions.AccountDisableException;
import com.school.exceptions.UnauthorizedException;
import com.school.model.RefreshToken;
import com.school.model.User;
import com.school.service.interfaces.RefreshTokenService;
import com.school.utils.TokenGenerator;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.school.validations.ValidatorUtil.*;

public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenDao refreshTokenDao;
    private final UserDao userDao;

    public RefreshTokenServiceImpl() {
        refreshTokenDao = new RefreshTokenDaoImpl();
        userDao = new UserDaoImpl();
    }

    @Override
    public String generateToken(long userId) {
        validateId(userId);

        String rawToken = TokenGenerator.generateToken();

        RefreshToken token = new RefreshToken();
        token.setUserId(userId);
        token.setTokenHash(TokenGenerator.sha256(rawToken));
        token.setExpiresAt(LocalDateTime.now().plusDays(refreshTokenExpiryDays()));
        token.setUsed(false);

        if (!refreshTokenDao.insertToken(token))
            throw new IllegalStateException("Failed to create refresh token.");

        return rawToken;
    }

    @Override
    public Map<String, Object> refresh(String refreshToken) {
        refreshToken = validateRequired(refreshToken, "Refresh token");

        String tokenHash = TokenGenerator.sha256(refreshToken);
        RefreshToken stored = refreshTokenDao.getToken(tokenHash);

        if (stored == null || stored.isUsed())
            throw new UnauthorizedException("Invalid or already used refresh token.");

        if (stored.getExpiresAt() == null || stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenDao.markTokenAsUsed(tokenHash);
            throw new UnauthorizedException("Refresh token has expired.");
        }

        User user = userDao.getUserById(stored.getUserId());
        if (user == null) {
            refreshTokenDao.markTokenAsUsed(tokenHash);
            throw new UnauthorizedException("Invalid refresh token.");
        }
        if (!user.isActive()) {
            refreshTokenDao.markTokenAsUsed(tokenHash);
            throw new AccountDisableException("Account is disabled. Contact the administrator.");
        }

        if (!refreshTokenDao.markTokenAsUsed(tokenHash))
            throw new UnauthorizedException("Invalid or already used refresh token.");
        String newRawToken = generateToken(stored.getUserId());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("refreshToken", newRawToken);
        result.put("userId", stored.getUserId());
        return result;
    }

    @Override
    public void revoke(String refreshToken) {
        refreshToken = validateRequired(refreshToken, "Refresh token");
        refreshTokenDao.markTokenAsUsed(TokenGenerator.sha256(refreshToken));
    }

    private long refreshTokenExpiryDays() {
        String value = System.getenv().getOrDefault("REFRESH_TOKEN_EXPIRY_DAYS", "30");
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return 30L;
        }
    }
}
