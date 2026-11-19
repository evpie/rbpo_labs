package com.example.lab2.service;

import com.example.lab2.model.OpaqueToken;
import com.example.lab2.repository.OpaqueTokenStore;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

@Service
public class OpaqueTokenService {
    private final SecureRandom random = new SecureRandom();
    private final OpaqueTokenStore store;

    public OpaqueTokenService(OpaqueTokenStore store) {
        this.store = store;
    }

    public String create(String username, String type, Duration ttl) {
        if (username == null || username.isBlank()
                || !("access".equals(type) || "refresh".equals(type))
                || ttl == null || ttl.isNegative() || ttl.isZero()) {
            throw new IllegalArgumentException("Неверные параметры Opaque-токена");
        }
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String value = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        store.save(value, new OpaqueToken(username, type, Instant.now().plus(ttl)));
        return value;
    }

    public OpaqueToken validate(String value, String expectedType) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Не указан Opaque-токен");
        }
        OpaqueToken token = store.find(value)
                .orElseThrow(() -> new IllegalArgumentException("Неизвестный Opaque-токен"));
        if (!token.expiresAt().isAfter(Instant.now())) {
            store.remove(value);
            throw new IllegalArgumentException("Opaque-токен истёк");
        }
        if (!token.type().equals(expectedType)) {
            throw new IllegalArgumentException("Неверный тип Opaque-токена");
        }
        return token;
    }

    public void revoke(String value) {
        store.remove(value);
    }
}
