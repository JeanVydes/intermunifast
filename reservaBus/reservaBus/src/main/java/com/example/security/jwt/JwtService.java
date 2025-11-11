package com.example.security.jwt;

import java.time.Instant;
import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
    private final SecretKey secretSigningKey;
    private final long jwtExpirationMs;

    public JwtService(
            @Value("${security.jwt.signing-key}") String signingKey,
            @Value("${security.jwt.expiration-ms}") long expirationMs) {
        this.secretSigningKey = Keys.hmacShaKeyFor(signingKey.getBytes());
        this.jwtExpirationMs = expirationMs;
    }

    public String generateToken(
            UserDetails principal,
            Map<String, Object> extraClaims) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(principal.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(jwtExpirationMs)))
                .claims().add(extraClaims)
                .notBefore(Date.from(now))
                .and()
                .signWith(secretSigningKey)
                .compact();
    }

    public String extractSubject(String token) {
        return Jwts.parser().verifyWith(secretSigningKey).build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean isValidToken(String token, UserDetails principal) {
        String subject = extractSubject(token);
        return subject.equals(principal.getUsername());
    }

    public long getJwtExpirationMs() {
        return jwtExpirationMs;
    }
}
