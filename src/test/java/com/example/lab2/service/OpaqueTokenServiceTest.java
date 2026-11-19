package com.example.lab2.service;

import com.example.lab2.model.OpaqueToken;
import com.example.lab2.repository.OpaqueTokenStore;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class OpaqueTokenServiceTest {
    final OpaqueTokenStore store = new OpaqueTokenStore();
    final OpaqueTokenService service = new OpaqueTokenService(store);

    @Test
    void createsUniqueTokensAndChecksType() {
        String a = service.create("dev@test.com", "access", Duration.ofMinutes(15));
        String b = service.create("dev@test.com", "access", Duration.ofMinutes(15));
        assertNotEquals(a, b);
        assertEquals("dev@test.com", service.validate(a, "access").username());
        assertEquals(43, a.length());
        assertThrows(IllegalArgumentException.class, () -> service.validate(a, "refresh"));
    }

    @Test
    void rejectsExpiredUnknownAndRevokedTokens() {
        store.save("expired", new OpaqueToken("dev@test.com", "access", Instant.now().minusSeconds(1)));
        assertThrows(IllegalArgumentException.class, () -> service.validate("expired", "access"));
        assertTrue(store.find("expired").isEmpty());
        assertThrows(IllegalArgumentException.class, () -> service.validate("unknown", "access"));
        String token = service.create("dev@test.com", "refresh", Duration.ofDays(1));
        service.revoke(token);
        assertThrows(IllegalArgumentException.class, () -> service.validate(token, "refresh"));
    }
}
