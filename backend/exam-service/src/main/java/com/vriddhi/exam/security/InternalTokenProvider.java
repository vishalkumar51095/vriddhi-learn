package com.vriddhi.exam.security;

import com.vriddhi.exam.config.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * Service-to-service credential. Content-service only accepts signed JWTs, so
 * exam-service signs its own short-lived one (same shared secret as every other
 * service) carrying the EXAM_SERVICE role that guards content-service's
 * /internal/** endpoints. It is cached and re-minted shortly before expiry.
 */
@Component
public class InternalTokenProvider {

    private static final long TTL_SECONDS = 300;

    private final SecretKey key;
    private final String issuer;
    private String cached;
    private Instant expiresAt = Instant.EPOCH;

    public InternalTokenProvider(JwtProperties properties) {
        this.key = Keys.hmacShaKeyFor(properties.secret().getBytes());
        this.issuer = properties.issuer();
    }

    public synchronized String token() {
        Instant now = Instant.now();
        if (cached == null || now.isAfter(expiresAt.minusSeconds(30))) {
            expiresAt = now.plusSeconds(TTL_SECONDS);
            cached = Jwts.builder()
                    .issuer(issuer)
                    .subject("exam-service")
                    .claim("roles", List.of("EXAM_SERVICE"))
                    .issuedAt(Date.from(now))
                    .expiration(Date.from(expiresAt))
                    .signWith(key)
                    .compact();
        }
        return cached;
    }
}
