package com.example.lab2.service;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
    static final String SECRET = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";
    final JwtService jwt = new JwtService(SECRET, Duration.ofMinutes(15), Duration.ofDays(7));

    JwtBuilder builder() {
        return Jwts.builder().subject("dev@test.com").id("test-id").claim("type", "refresh")
                .issuedAt(Date.from(Instant.now().minusSeconds(10)))
                .expiration(Date.from(Instant.now().plusSeconds(60)));
    }

    String sign(JwtBuilder builder) {
        return builder.signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256).compact();
    }

    @Test
    void issuesUniqueHs256TokensWithWholeSecondExpiry() {
        String a = jwt.generateAccessToken("dev@test.com");
        String r = jwt.generateRefreshToken("dev@test.com");
        var access = jwt.validateAccessToken(a);
        var refresh = jwt.validateRefreshToken(r);
        assertNotEquals(access.getId(), refresh.getId());
        assertNotEquals(a, jwt.generateAccessToken("dev@test.com"));
        assertEquals(900, Duration.between(access.getIssuedAt().toInstant(), access.getExpiration().toInstant()).getSeconds());
        assertEquals(604800, Duration.between(refresh.getIssuedAt().toInstant(), refresh.getExpiration().toInstant()).getSeconds());
        var parsed = Jwts.parser().verifyWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8))).build().parseSignedClaims(a);
        assertEquals("HS256", parsed.getHeader().getAlgorithm());
        assertEquals(0, access.getExpiration().toInstant().getNano());
    }

    @Test
    void requiresEveryClaimAndCorrectType() {
        for (String claim : new String[]{"sub", "jti", "iat", "exp", "type"}) {
            var b = builder();
            b.claim(claim, null);
            String token = sign(b);
            assertThrows(JwtException.class, () -> jwt.validateRefreshToken(token), claim);
        }
        assertThrows(JwtException.class, () -> jwt.validateRefreshToken(sign(builder().subject(" "))));
        assertThrows(JwtException.class, () -> jwt.validateRefreshToken(jwt.generateAccessToken("dev@test.com")));
        assertThrows(JwtException.class, () -> jwt.validateAccessToken(jwt.generateRefreshToken("dev@test.com")));
    }

    @Test
    void rejectsExpiredFutureAndTamperedTokens() {
        assertThrows(JwtException.class, () -> jwt.validateRefreshToken(sign(builder().expiration(Date.from(Instant.now().minusSeconds(1))))));
        assertThrows(JwtException.class, () -> jwt.validateRefreshToken(sign(builder().issuedAt(Date.from(Instant.now().plusSeconds(30))))));
        String wrongSignature = builder().signWith(Jwts.SIG.HS256.key().build()).compact();
        assertThrows(JwtException.class, () -> jwt.validateRefreshToken(wrongSignature));
        String wrongAlgorithm = builder().signWith(Jwts.SIG.HS512.key().build(), Jwts.SIG.HS512).compact();
        assertThrows(JwtException.class, () -> jwt.validateRefreshToken(wrongAlgorithm));
        assertThrows(JwtException.class, () -> jwt.validateRefreshToken(builder().compact()));
    }

    @Test
    void rejectsInvalidSettings() {
        assertThrows(IllegalArgumentException.class, () -> new JwtService("short", Duration.ofMinutes(1), Duration.ofDays(1)));
        assertThrows(IllegalArgumentException.class, () -> new JwtService(SECRET, Duration.ZERO, Duration.ofDays(1)));
    }
}
