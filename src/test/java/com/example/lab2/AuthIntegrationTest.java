package com.example.lab2;

import com.example.lab2.model.*;
import com.example.lab2.repository.UserRepository;
import com.example.lab2.repository.UserSessionRepository;
import com.example.lab2.service.JwtService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthIntegrationTest {
    @LocalServerPort int port;
    @Autowired UserRepository users;
    @MockitoSpyBean UserSessionRepository sessions;
    @Autowired PasswordEncoder encoder;
    @Autowired JwtService jwt;
    @Autowired PlatformTransactionManager transactionManager;
    final HttpClient client = HttpClient.newHttpClient();
    final ObjectMapper json = new ObjectMapper();

    record Reply(int status, JsonNode body) { }

    @BeforeEach
    void prepare() {
        sessions.deleteAll();
        users.deleteAll();
        User user = new User();
        user.setName("Тест");
        user.setEmail("dev@test.com");
        user.setPassword(encoder.encode("secret123"));
        user.setRole(Role.DEVELOPER);
        users.save(user);
    }

    Reply request(String method, String path, Object body, String access) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path));
        if (access != null) builder.header("Authorization", "Bearer " + access);
        if (body != null) builder.header("Content-Type", "application/json");
        builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)));
        var response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        return new Reply(response.statusCode(), response.body().isEmpty()
                ? json.nullNode() : json.readTree(response.body()));
    }

    Reply login() throws Exception {
        Reply r = request("POST", "/auth/login", Map.of("email", "dev@test.com",
                "password", "secret123", "deviceId", "test-device"), null);
        assertEquals(200, r.status());
        return r;
    }

    Reply refresh(String token) throws Exception {
        return request("POST", "/auth/refresh", Map.of("refreshToken", token), null);
    }

    Map<String, String> registration(String email) {
        return Map.of("name", "Новый", "email", email, "password", "secret123",
                "confirmPassword", "secret123", "deviceId", "phone", "role", "ADMIN");
    }

    void assertError(Reply reply, int status) {
        assertEquals(status, reply.status(), reply.body().toString());
        assertFalse(reply.body().has("accessToken"));
        assertFalse(reply.body().has("refreshToken"));
        assertTrue(reply.body().has("error"));
    }

    UserSession findSession(String token) {
        return new TransactionTemplate(transactionManager).execute(status ->
                sessions.findByRefreshToken(token).orElseThrow());
    }

    @Test
    void loginRotationAndReplayKeepHistory() throws Exception {
        Reply first = login();
        String oldAccess = first.body().get("accessToken").asText();
        String oldRefresh = first.body().get("refreshToken").asText();
        UserSession original = findSession(oldRefresh);
        assertEquals(SessionStatus.ACTIVE, original.getStatus());
        assertEquals("test-device", original.getDeviceId());
        assertEquals(jwt.validateAccessToken(oldAccess).getExpiration().toInstant(), original.getAccessTokenExpiry());
        assertEquals(jwt.validateRefreshToken(oldRefresh).getExpiration().toInstant(), original.getRefreshTokenExpiry());
        assertEquals(0, original.getAccessTokenExpiry().getNano());
        assertEquals(200, request("GET", "/api/users", null, oldAccess).status());
        Reply rotated = refresh(oldRefresh);
        assertEquals(200, rotated.status());
        assertNotEquals(oldAccess, rotated.body().get("accessToken").asText());
        String newRefresh = rotated.body().get("refreshToken").asText();
        assertNotEquals(oldRefresh, newRefresh);
        assertEquals(SessionStatus.USED, findSession(oldRefresh).getStatus());
        assertEquals(SessionStatus.ACTIVE, findSession(newRefresh).getStatus());
        assertEquals("test-device", findSession(newRefresh).getDeviceId());
        assertError(refresh(oldRefresh), 401);
        assertEquals(SessionStatus.REVOKED, findSession(oldRefresh).getStatus());
        assertEquals(SessionStatus.ACTIVE, findSession(newRefresh).getStatus());
        assertError(refresh(oldRefresh), 401);
        assertEquals(2, sessions.count());
        assertEquals(200, request("GET", "/api/users", null, oldAccess).status());
        assertEquals(200, refresh(newRefresh).status());
    }

    @Test
    void registrationCreatesDeveloperAndPairWithoutExposingPassword() throws Exception {
        Reply r = request("POST", "/auth/register", registration("new@test.com"), null);
        assertEquals(201, r.status());
        assertEquals(Role.DEVELOPER, users.findByEmail("new@test.com").orElseThrow().getRole());
        assertTrue(encoder.matches("secret123", users.findByEmail("new@test.com").orElseThrow().getPassword()));
        assertEquals(1, sessions.count());
        Reply list = request("GET", "/api/users", null, r.body().get("accessToken").asText());
        assertEquals(200, list.status());
        assertFalse(list.body().toString().contains("password"));
        assertError(request("POST", "/auth/register", registration("new@test.com"), null), 409);
        assertEquals(1, sessions.count());
    }

    @Test
    void missingAndBlankFieldsReturn400() throws Exception {
        for (Map<String, String> body : List.of(Map.<String, String>of(),
                Map.of("email", "", "password", "secret123"), Map.of("email", "dev@test.com"))) {
            assertError(request("POST", "/auth/login", body, null), 400);
        }
        for (Map<String, String> body : List.of(Map.<String, String>of(), Map.of("refreshToken", "  "))) {
            assertError(request("POST", "/auth/refresh", body, null), 400);
        }
        for (String field : List.of("name", "email", "password", "confirmPassword")) {
            var body = new java.util.HashMap<>(registration("new@test.com"));
            body.remove(field);
            assertError(request("POST", "/auth/register", body, null), 400);
            body.put(field, " ");
            assertError(request("POST", "/auth/register", body, null), 400);
        }
        assertEquals(0, sessions.count());
    }

    @Test
    void wrongCredentialsAndRegistrationRules() throws Exception {
        for (String email : List.of("dev@test.com", "unknown@test.com")) {
            assertError(request("POST", "/auth/login", Map.of("email", email, "password", "wrong"), null), 401);
        }
        var body = new java.util.HashMap<>(registration("new@test.com"));
        body.put("confirmPassword", "other123");
        assertError(request("POST", "/auth/register", body, null), 400);
        body.put("password", "short");
        body.put("confirmPassword", "short");
        assertError(request("POST", "/auth/register", body, null), 400);
    }

    @Test
    void tokenTypesAndUnknownTokensAreRejected() throws Exception {
        Reply r = login();
        assertError(refresh(r.body().get("accessToken").asText()), 401);
        assertError(request("GET", "/api/users", null, r.body().get("refreshToken").asText()), 401);
        assertError(request("GET", "/api/users", null, null), 401);
        assertError(refresh("garbage"), 401);
        assertError(refresh(jwt.generateRefreshToken("dev@test.com")), 401);
        assertEquals(1, sessions.count());
    }

    @Test
    void inactiveAndDeletedUsersCannotRefreshOrUseAccess() throws Exception {
        Reply r = login();
        User user = users.findByEmail("dev@test.com").orElseThrow();
        user.setActive(false);
        users.save(user);
        assertError(refresh(r.body().get("refreshToken").asText()), 401);
        assertError(request("GET", "/api/users", null, r.body().get("accessToken").asText()), 401);
        assertError(request("POST", "/auth/login", Map.of("email", "dev@test.com", "password", "secret123"), null), 401);
        users.deleteAll();
        assertError(refresh(r.body().get("refreshToken").asText()), 401);
        assertEquals(1, sessions.count());
    }

    @Test
    void expiredSessionAndWrongOwnerAreRejected() throws Exception {
        Reply r = login();
        String token = r.body().get("refreshToken").asText();
        UserSession s = findSession(token);
        s.setRefreshTokenExpiry(Instant.now().minusSeconds(1));
        sessions.saveAndFlush(s);
        assertError(refresh(token), 401);
        s.setRefreshTokenExpiry(Instant.now().plusSeconds(100));
        s.setUsername("someone-else@test.com");
        sessions.saveAndFlush(s);
        assertError(refresh(token), 401);
        assertEquals(1, sessions.count());
    }

    @Test
    void failedSessionInsertRollsBackRegistration() throws Exception {
        doThrow(new IllegalStateException("Test insert failure")).when(sessions).saveAndFlush(any(UserSession.class));
        assertError(request("POST", "/auth/register", registration("rollback@test.com"), null), 500);
        assertFalse(users.existsByEmail("rollback@test.com"));
        assertEquals(0, sessions.count());
    }

    @Test
    void failedRotationRollsBackUsedStatus() throws Exception {
        String token = login().body().get("refreshToken").asText();
        doThrow(new IllegalStateException("Test insert failure")).when(sessions).saveAndFlush(any(UserSession.class));
        assertError(refresh(token), 500);
        assertEquals(SessionStatus.ACTIVE, findSession(token).getStatus());
        assertEquals(1, sessions.count());
    }

    @Test
    void onlyOneConcurrentRefreshSucceeds() throws Exception {
        String token = login().body().get("refreshToken").asText();
        var start = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(2)) {
            var one = pool.submit(() -> { start.await(); return refresh(token); });
            var two = pool.submit(() -> { start.await(); return refresh(token); });
            start.countDown();
            var statuses = List.of(one.get().status(), two.get().status()).stream().sorted().toList();
            assertEquals(List.of(200, 401), statuses);
        }
        assertEquals(2, sessions.count());
        assertEquals(SessionStatus.REVOKED, findSession(token).getStatus());
        assertEquals(1, sessions.findAll().stream().filter(s -> s.getStatus() == SessionStatus.ACTIVE).count());
    }

    @Test
    void oldAuthPathsAndRoleChecksWork() throws Exception {
        Reply r = request("POST", "/api/auth/login", Map.of("email", "dev@test.com", "password", "secret123"), null);
        assertEquals(200, r.status());
        String access = r.body().get("accessToken").asText();
        assertError(request("POST", "/api/software-products", Map.of("name", "Forbidden"), access), 403);
        assertEquals(200, request("POST", "/api/auth/refresh", Map.of("refreshToken", r.body().get("refreshToken").asText()), null).status());
    }
}
