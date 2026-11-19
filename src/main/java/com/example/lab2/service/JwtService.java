package com.example.lab2.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {
    private final SecretKey key;
    private final JwtParser parser;
    private final Duration accessTtl;
    private final Duration refreshTtl;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.access-ttl}") Duration accessTtl,
                      @Value("${jwt.refresh-ttl}") Duration refreshTtl) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length != 32) {
            throw new IllegalArgumentException("Ключ JWT должен занимать 32 байта");
        }
        if (accessTtl.getSeconds() < 1 || refreshTtl.getSeconds() < 1) {
            throw new IllegalArgumentException("Время жизни JWT должно быть не меньше секунды");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.accessTtl = accessTtl;
        this.refreshTtl = refreshTtl;
        this.parser = Jwts.parser().verifyWith(key).build();
    }

    public String generateAccessToken(String username) {
        return generateToken(username, "access", accessTtl);
    }

    public String generateRefreshToken(String username) {
        return generateToken(username, "refresh", refreshTtl);
    }

    private String generateToken(String username, String type, Duration ttl) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Не указан владелец токена");
        }
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        return Jwts.builder()
                .subject(username)
                .claim("type", type)
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(ttl.getSeconds())))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public Claims validateAccessToken(String token) {
        return validateToken(token, "access");
    }

    public Claims validateRefreshToken(String token) {
        return validateToken(token, "refresh");
    }

    private Claims validateToken(String token, String type) {
        var parsed = parser.parseSignedClaims(token);
        if (!"HS256".equals(parsed.getHeader().getAlgorithm())) {
            throw new JwtException("Разрешён только HS256");
        }
        Claims claims = parsed.getPayload();
        if (claims.getSubject() == null || claims.getSubject().isBlank()
                || claims.getId() == null || claims.getId().isBlank()
                || claims.getIssuedAt() == null || claims.getExpiration() == null
                || !type.equals(claims.get("type", String.class))) {
            throw new JwtException("Неверный тип токена или отсутствуют обязательные claims");
        }
        if (!claims.getExpiration().after(new Date())
                || !claims.getExpiration().after(claims.getIssuedAt())
                || claims.getIssuedAt().after(new Date())) {
            throw new JwtException("Недопустимое время действия токена");
        }
        return claims;
    }
}
