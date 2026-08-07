package com.school.web.auth;

import com.school.config.EnvLoader;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.*;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

public final class JwtUtil {

    private static final String ISSUER = "gds-portal";
    private static final SecretKey SECRET_KEY = secretKey();

    private JwtUtil() {
    }

    public static String generateToken(long userId, String roleName, long expiresAtMillis) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .issuer(ISSUER)
                .subject(String.valueOf(userId))
                .id(UUID.randomUUID().toString())
                .claim("role", roleName)
                .issuedAt(new Date(now))
                .expiration(new Date(expiresAtMillis))
                .signWith(SECRET_KEY)
                .compact();
    }

    public static TokenClaims parse(String token) {
        if (token == null || token.isBlank())
            return null;

        try {
            Claims claims = Jwts.parser()
                    .requireIssuer(ISSUER)
                    .verifyWith(SECRET_KEY)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            TokenClaims result = new TokenClaims();
            result.setUserId(Long.parseLong(claims.getSubject()));
            result.setRoleName(claims.get("role", String.class));
            result.setExpiresAt(claims.getExpiration().getTime());
            result.setJti(claims.getId());
            return result;
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    private static SecretKey secretKey() {
        String secret = EnvLoader.get("JWT_SECRET", "change-this-secret-before-production");
        if (secret == null || secret.isBlank() || "change-this-secret-before-production".equals(secret))
            throw new IllegalStateException("JWT_SECRET is not configured. Set a base64 secret (>= 32 bytes) in the .env file.");
        byte[] keyBytes;
        try {
            keyBytes = Base64.getUrlDecoder().decode(secret);
        } catch (IllegalArgumentException e) {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        if (keyBytes.length < 32)
            throw new IllegalStateException("JWT_SECRET must decode to at least 32 bytes.");
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class TokenClaims {
        private long userId;
        private String roleName;
        private long expiresAt;
        private String jti;
    }
}
