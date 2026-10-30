package com.vriddhi.content.config;

import com.vriddhi.content.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                // CORS is handled once, at the edge, by api-gateway's CorsWebFilter —
                // see auth-service's SecurityConfig for why a second CORS layer here
                // actively breaks browser requests rather than adding safety.
                .cors(cors -> cors.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/prometheus").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        // Matchers are evaluated top-down and the first match wins, so the
                        // specific learner-facing rules must come before the broad
                        // teacher/admin rules on the same path prefix.
                        .requestMatchers(HttpMethod.GET, "/api/categories", "/api/subjects/**", "/api/topics/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/questions/random", "/api/questions/*/practice").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/questions/*/practice/check").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/questions/*/review").hasAnyRole("CONTENT_REVIEWER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/questions/*/submit-for-review").hasAnyRole("TEACHER", "ADMIN")
                        // Authoring views include the correct answers, so they are NOT for learners.
                        .requestMatchers(HttpMethod.GET, "/api/questions", "/api/questions/**").hasAnyRole("TEACHER", "CONTENT_REVIEWER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/questions", "/api/questions/**").hasAnyRole("TEACHER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/questions/**").hasAnyRole("TEACHER", "ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // service-to-service only; exam-service presents a short-lived signed JWT with this role
                        .requestMatchers("/internal/**").hasAnyRole("EXAM_SERVICE", "ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
