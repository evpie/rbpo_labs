package com.example.lab2.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleStatus(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of("error",
                ex.getReason() == null ? "Ошибка запроса" : ex.getReason()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleJson(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", "Некорректное JSON-тело"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccess(AccessDeniedException ex) {
        return ResponseEntity.status(403).body(Map.of("error", "Недостаточно прав"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleConflict(DataIntegrityViolationException ex) {
        return ResponseEntity.status(409).body(Map.of("error", "Нарушено ограничение данных: запись уже существует или используется"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntime(RuntimeException ex) {
        return ResponseEntity.internalServerError().body(Map.of("error", "Внутренняя ошибка сервера"));
    }
}
