package com.budgettravel.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey key;
    private final long ttlMillis;

    public JwtService(@Value("${app.jwt.secret:}") String secret, @Value("${app.jwt.hours:8}") long hours) {
        try {
            byte[] raw;
            if (secret == null || secret.isBlank()) {
                raw = new byte[32];
                new SecureRandom().nextBytes(raw);
            } else {
                raw = MessageDigest.getInstance("SHA-256").digest(secret.getBytes(StandardCharsets.UTF_8));
            }
            this.key = Keys.hmacShaKeyFor(raw);
        } catch (Exception e) { throw new IllegalStateException(e); }
        this.ttlMillis = hours * 3600_000L;
    }

    public long ttlSeconds() { return ttlMillis / 1000; }

    public String create(Long travelerId) {
        Date now = new Date();
        return Jwts.builder().subject(String.valueOf(travelerId)).issuedAt(now)
                .expiration(new Date(now.getTime() + ttlMillis)).signWith(key).compact();
    }

    /** Returns the traveler id, or null if the token is missing, invalid or expired. */
    public Long parse(String token) {
        try {
            String sub = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();
            return Long.valueOf(sub);
        } catch (JwtException | IllegalArgumentException e) { return null; }
    }
}
