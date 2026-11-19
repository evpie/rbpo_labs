package com.example.lab2.service;

import com.example.lab2.repository.OpaqueTokenStore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TokenPerformanceTest {
    static final int WARMUP = 1000;
    static final int ITERATIONS = 3000;
    static final int ROUNDS = 3;
    static final Path RESULT = Path.of("target", "token-performance.csv");
    static volatile Object result;
    final JwtService jwt = new JwtService(JwtServiceTest.SECRET, Duration.ofMinutes(15), Duration.ofDays(7));
    final OpaqueTokenService opaque = new OpaqueTokenService(new OpaqueTokenStore());

    @BeforeAll
    static void prepareFile() throws Exception {
        Files.createDirectories(RESULT.getParent());
        Files.writeString(RESULT, "operation,type,implementation,round,iterations,ns_per_operation\n");
    }

    void measure(String operation, String type, String implementation, Runnable action) throws Exception {
        for (int i = 0; i < WARMUP; i++) action.run();
        for (int round = 1; round <= ROUNDS; round++) {
            long start = System.nanoTime();
            for (int i = 0; i < ITERATIONS; i++) action.run();
            double average = (System.nanoTime() - start) / (double) ITERATIONS;
            Files.writeString(RESULT, String.format(Locale.ROOT, "%s,%s,%s,%d,%d,%.1f%n",
                    operation, type, implementation, round, ITERATIONS, average), StandardOpenOption.APPEND);
        }
    }

    @Test
    void compareCreation() throws Exception {
        for (String type : new String[]{"access", "refresh"}) {
            measure("create", type, "JWT", () -> result = type.equals("access")
                    ? jwt.generateAccessToken("dev@test.com") : jwt.generateRefreshToken("dev@test.com"));
            Duration ttl = type.equals("access") ? Duration.ofMinutes(15) : Duration.ofDays(7);
            measure("create", type, "Opaque", () -> result = opaque.create("dev@test.com", type, ttl));
        }
    }

    @Test
    void compareValidation() throws Exception {
        for (String type : new String[]{"access", "refresh"}) {
            String signed = type.equals("access") ? jwt.generateAccessToken("dev@test.com")
                    : jwt.generateRefreshToken("dev@test.com");
            String random = opaque.create("dev@test.com", type, Duration.ofDays(7));
            assertEquals("dev@test.com", opaque.validate(random, type).username());
            measure("validate", type, "JWT", () -> result = type.equals("access")
                    ? jwt.validateAccessToken(signed) : jwt.validateRefreshToken(signed));
            measure("validate", type, "Opaque", () -> result = opaque.validate(random, type));
        }
    }
}
