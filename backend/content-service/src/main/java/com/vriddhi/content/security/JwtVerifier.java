package com.vriddhi.content.security;

import com.vriddhi.content.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

/**
 * This service never issues tokens — only verifies ones Auth Service signed,
 * using the same shared secret. That's what lets every downstream service
 * authorize a request without a synchronous call back to Auth.
 */
@Component
public class JwtVerifier {

    private final SecretKey signingKey;

    public JwtVerifier(JwtProperties properties) {
        this.signingKey = Keys.hmacShaKeyFor(properties.secret().getBytes());
    }

    public Claims parseAndValidate(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
