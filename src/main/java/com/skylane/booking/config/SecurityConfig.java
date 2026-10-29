package com.skylane.booking.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * API Gateway responsibility: terminate auth (JWT) for web + mobile clients.
 */
@Configuration
public class SecurityConfig {

    @Value("${skylane.jwt.secret}")
    private String jwtSecret;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> {})
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**", "/health").permitAll()
                .requestMatchers("/api/search/**", "/api/quote/**").permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(new JwtAuthFilter(jwtSecret),
                    UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    static class JwtAuthFilter extends OncePerRequestFilter {
        private final byte[] key;

        JwtAuthFilter(String secret) {
            this.key = secret.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res,
                                        FilterChain chain) throws ServletException, IOException {
            String header = req.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                try {
                    Claims claims = Jwts.parserBuilder()
                            .setSigningKey(Keys.hmacShaKeyFor(key))
                            .build()
                            .parseClaimsJws(header.substring(7))
                            .getBody();
                    String subject = claims.getSubject();
                    var authToken = new UsernamePasswordAuthenticationToken(
                            subject, null,
                            List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } catch (Exception ignored) {
                    // invalid token -> anonymous; secured endpoints will 401
                }
            }
            chain.doFilter(req, res);
        }
    }
}
