package com.vriddhi.progress.security;

import com.vriddhi.progress.config.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * Service-to-service credential: a short-lived JWT signed with the shared secret and carrying the
 * PROGRESS_SERVICE role, which content-service's /internal/** endpoints accept. Cached and re-minted
 * shortly before expiry (same approach as exam-service).
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
                    .subject("progress-service")
                    .claim("roles", List.of("PROGRESS_SERVICE"))
                    .issuedAt(Date.from(now))
                    .expiration(Date.from(expiresAt))
                    .signWith(key)
                    .compact();
        }
        return cached;
    }
}
