package com.school.service.impl;

import com.school.dao.impl.RevokedTokenDaoImpl;
import com.school.dao.interfaces.RevokedTokenDao;
import com.school.service.interfaces.RevokedTokenService;

import java.time.LocalDateTime;

public class RevokedTokenServiceImpl implements RevokedTokenService {

    private final RevokedTokenDao revokedTokenDao;

    public RevokedTokenServiceImpl() {
        this.revokedTokenDao = new RevokedTokenDaoImpl();
    }

    @Override
    public boolean revoke(String jti, LocalDateTime expiresAt) {
        if (jti == null || jti.isBlank())
            return false;
        revokedTokenDao.deleteExpired();
        return revokedTokenDao.revoke(jti, expiresAt);
    }

    @Override
    public boolean isRevoked(String jti) {
        if (jti == null || jti.isBlank())
            return false;
        return revokedTokenDao.isRevoked(jti);
    }

    @Override
    public void cleanupExpired() {
        revokedTokenDao.deleteExpired();
    }
}
