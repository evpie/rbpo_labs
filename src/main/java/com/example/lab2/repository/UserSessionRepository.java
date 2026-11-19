package com.example.lab2.repository;

import com.example.lab2.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {
    // поиск сессии по refresh-токену
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UserSession> findByRefreshToken(String refreshToken);
}