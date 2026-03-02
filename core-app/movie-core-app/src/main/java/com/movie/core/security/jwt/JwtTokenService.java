package com.movie.core.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtTokenService {

    private final SecretKey secretKey;

    public JwtTokenService(@Value("${auth.jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(
            Long accountId,
            String email,
            String role,
            String provider,
            String sessionId,
            Instant now,
            Instant expiresAt
    ) {
        return Jwts.builder()
                .subject(String.valueOf(accountId))
                .claims(Map.of(
                        "email", email,
                        "role", role,
                        "provider", provider,
                        "sid", sessionId,
                        "typ", "access"
                ))
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
    }

    public String createRefreshToken(
            Long accountId,
            String sessionId,
            Instant now,
            Instant expiresAt
    ) {
        return Jwts.builder()
                .subject(String.valueOf(accountId))
                .claims(Map.of(
                        "sid", sessionId,
                        "typ", "refresh",
                        "jti", UUID.randomUUID().toString()
                ))
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
    }
}
