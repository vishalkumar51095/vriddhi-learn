package com.vriddhi.gateway.filter;

import com.vriddhi.gateway.config.JwtProperties;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

/**
 * Edge-level defense in depth: rejects requests to protected routes with no
 * (or an invalid) access token before they ever reach a downstream service,
 * so an unauthenticated flood can't burn backend capacity. Each service
 * still re-validates the token itself — the gateway is a first filter, not
 * the sole line of defense (doc section 16: API Gateway as the single edge
 * entry point).
 */
@Component
public class EdgeAuthFilter implements GlobalFilter, Ordered {

    private final SecretKey signingKey;

    private static final Set<String> PUBLIC_PREFIXES = Set.of(
            "/api/auth/register", "/api/auth/login", "/api/auth/refresh", "/api/public/",
            "/actuator"
    );

    public EdgeAuthFilter(JwtProperties properties) {
        this.signingKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (PUBLIC_PREFIXES.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }

        List<String> authHeaders = request.getHeaders().getOrEmpty("Authorization");
        String token = authHeaders.stream()
                .filter(h -> h.startsWith("Bearer "))
                .map(h -> h.substring(7))
                .findFirst()
                .orElse(null);

        if (token == null) {
            return reject(exchange, "Missing access token");
        }

        try {
            Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token);
            return chain.filter(exchange);
        } catch (JwtException | IllegalArgumentException e) {
            return reject(exchange, "Invalid or expired access token");
        }
    }

    private Mono<Void> reject(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        byte[] body = ("{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"" + message + "\"}")
                .getBytes(StandardCharsets.UTF_8);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body)));
    }

    @Override
    public int getOrder() {
        return -1; // run before routing
    }
}
