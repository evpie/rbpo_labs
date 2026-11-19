package com.example.lab2.controller;

import com.example.lab2.model.RegisterRequest;
import com.example.lab2.service.AuthService;
import com.example.lab2.service.TokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping({"/auth", "/api/auth"})
public class AuthController {
    private final AuthService authService;
    private final TokenService tokenService;

    public AuthController(AuthService authService, TokenService tokenService) {
        this.authService = authService;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");
        requireText(email, "email");
        requireText(password, "password");
        return ResponseEntity.ok(authService.login(email, password, request.get("deviceId")));
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody RegisterRequest request) {
        requireText(request.getName(), "name");
        requireText(request.getEmail(), "email");
        requireText(request.getPassword(), "password");
        requireText(request.getConfirmPassword(), "confirmPassword");
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(@RequestBody Map<String, String> request) {
        String refresh = request.get("refreshToken");
        requireText(refresh, "refreshToken");
        Map<String, String> pair = tokenService.refresh(refresh)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Refresh-токен уже использован"));
        return ResponseEntity.ok(pair);
    }

    private void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Не заполнено поле " + field);
        }
    }
}
