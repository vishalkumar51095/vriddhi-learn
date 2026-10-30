package com.vriddhi.auth.security;

import com.vriddhi.auth.config.JwtProperties;
import com.vriddhi.auth.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Issues short-lived access tokens (carrying roles/permissions as claims so
 * downstream services can authorize without calling back to Auth) and opaque
 * refresh tokens (doc section 19: "JWT/session architecture with secure
 * refresh strategy").
 */
@Service
public class JwtService {

    private final SecretKey signingKey;
    private final JwtProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.signingKey = Keys.hmacShaKeyFor(properties.secret().getBytes());
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plus(properties.accessTokenTtlMinutes(), ChronoUnit.MINUTES);

        List<String> roles = user.getRoles().stream().map(r -> r.getName()).collect(Collectors.toList());

        return Jwts.builder()
                .issuer(properties.issuer())
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("roles", roles)
                .issuedAt(java.util.Date.from(now))
                .expiration(java.util.Date.from(expiry))
                .signWith(signingKey)
                .compact();
    }

    public long accessTokenTtlSeconds() {
        return properties.accessTokenTtlMinutes() * 60L;
    }

    public java.time.Duration refreshTokenTtl() {
        return java.time.Duration.ofDays(properties.refreshTokenTtlDays());
    }

    /** Opaque, high-entropy refresh token. Only a hash of this is persisted. */
    public String generateOpaqueRefreshToken() {
        byte[] bytes = new byte[64];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public Claims parseAndValidate(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
