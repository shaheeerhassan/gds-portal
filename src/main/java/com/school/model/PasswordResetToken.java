package com.school.model;

import java.time.LocalDateTime;

public class PasswordResetToken {
    private long tokenId;
    private long userId;
    private String tokenHash;
    private LocalDateTime expiresAt;
    private boolean isUsed;
    private LocalDateTime createdAt;

    public PasswordResetToken() {}
    public PasswordResetToken(long tokenId, long userId, String tokenHash, LocalDateTime expiresAt, boolean isUsed, LocalDateTime createdAt) {
        this.tokenId = tokenId; this.userId = userId; this.tokenHash = tokenHash; this.expiresAt = expiresAt; this.isUsed = isUsed; this.createdAt = createdAt;
    }

    public long getTokenId() { return tokenId; }
    public void setTokenId(long tokenId) { this.tokenId = tokenId; }
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public boolean isUsed() { return isUsed; }
    public void setUsed(boolean used) { isUsed = used; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}