package com.example.lab2.service;

import com.example.lab2.model.SessionStatus;
import com.example.lab2.model.User;
import com.example.lab2.model.UserSession;
import com.example.lab2.repository.UserRepository;
import com.example.lab2.repository.UserSessionRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Service
public class TokenService {
    private final JwtService jwtService;
    private final UserSessionRepository sessionRepository;
    private final UserRepository userRepository;

    public TokenService(JwtService jwtService, UserSessionRepository sessionRepository,
                        UserRepository userRepository) {
        this.jwtService = jwtService;
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Map<String, String> issueTokens(String username, String deviceId) {
        String access = jwtService.generateAccessToken(username);
        String refresh = jwtService.generateRefreshToken(username);
        UserSession session = new UserSession();
        session.setUsername(username);
        session.setDeviceId(deviceId);
        session.setAccessToken(access);
        session.setRefreshToken(refresh);
        session.setAccessTokenExpiry(jwtService.validateAccessToken(access).getExpiration().toInstant());
        session.setRefreshTokenExpiry(jwtService.validateRefreshToken(refresh).getExpiration().toInstant());
        session.setStatus(SessionStatus.ACTIVE);
        sessionRepository.saveAndFlush(session);
        return Map.of("accessToken", access, "refreshToken", refresh);
    }

    @Transactional
    public Optional<Map<String, String>> refresh(String refreshToken) {
        Claims claims;
        try {
            claims = jwtService.validateRefreshToken(refreshToken);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный refresh-токен");
        }

        // Блокировка действует до конца транзакции. Второй запрос ждёт первый.
        UserSession old = sessionRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Сессия не найдена"));
        User user = userRepository.findByEmail(old.getUsername()).orElse(null);
        if (user == null || !user.isActive()
                || !old.getUsername().equals(claims.getSubject())
                || !old.getRefreshTokenExpiry().isAfter(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Сессия недействительна");
        }
        if (old.getStatus() == SessionStatus.USED) {
            old.setStatus(SessionStatus.REVOKED);
            sessionRepository.saveAndFlush(old);
            // Возвращаем отказ как результат, чтобы транзакция сохранила REVOKED.
            // Контроллер вернёт 401 уже после её завершения.
            return Optional.empty();
        }
        if (old.getStatus() != SessionStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Сессия отозвана");
        }
        old.setStatus(SessionStatus.USED);
        sessionRepository.save(old);
        // Ошибка сохранения новой пары откатит и изменение старой записи.
        return Optional.of(issueTokens(old.getUsername(), old.getDeviceId()));
    }
}
