package com.example.lab2.repository;

import com.example.lab2.model.OpaqueToken;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class OpaqueTokenStore {
    private final Map<String, OpaqueToken> tokens = new ConcurrentHashMap<>();

    public void save(String value, OpaqueToken token) {
        tokens.put(value, token);
    }

    public Optional<OpaqueToken> find(String value) {
        return Optional.ofNullable(tokens.get(value));
    }

    public void remove(String value) {
        tokens.remove(value);
    }
}
