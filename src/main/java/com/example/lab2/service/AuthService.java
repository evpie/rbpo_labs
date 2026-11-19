package com.example.lab2.service;

import com.example.lab2.model.RegisterRequest;
import com.example.lab2.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class AuthService {
    private final UserService userService;
    private final TokenService tokenService;

    public AuthService(UserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    @Transactional
    public Map<String, String> login(String email, String password, String deviceId) {
        User user = userService.login(email, password);
        return tokenService.issueTokens(user.getEmail(), deviceId);
    }

    @Transactional
    public Map<String, String> register(RegisterRequest request) {
        User user = userService.register(request);
        return tokenService.issueTokens(user.getEmail(), request.getDeviceId());
    }
}
