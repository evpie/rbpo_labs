package com.example.lab2.model;

import java.time.Instant;

public record OpaqueToken(String username, String type, Instant expiresAt) {
}
