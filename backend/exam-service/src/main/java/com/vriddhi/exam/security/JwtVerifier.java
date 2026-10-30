package com.vriddhi.exam.security;

import com.vriddhi.exam.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
public class JwtVerifier {

    private final SecretKey signingKey;

    public JwtVerifier(JwtProperties properties) {
        this.signingKey = Keys.hmacShaKeyFor(properties.secret().getBytes());
    }

    public Claims parseAndValidate(String token) throws JwtException {
        return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
    }
}
