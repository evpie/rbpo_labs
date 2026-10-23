package com.example.lab2.repository;

import com.example.lab2.model.User;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class UserRepository {
    private final Map<Long, User> storage = new HashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public List<User> findAll() {
        return new ArrayList<>(storage.values());
    }

    public Optional<User> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public User save(User user) {
        if (user.getId() == null) {
            user.setId(idCounter.getAndIncrement());
        }
        storage.put(user.getId(), user);
        return user;
    }

    public boolean deleteById(Long id) {
        return storage.remove(id) != null;
    }

    public boolean existsById(Long id) {
        return storage.containsKey(id);
    }
}