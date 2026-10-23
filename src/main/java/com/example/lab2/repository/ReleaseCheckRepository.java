package com.example.lab2.repository;

import com.example.lab2.model.ReleaseCheck;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class ReleaseCheckRepository {
    private final Map<Long, ReleaseCheck> storage = new HashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public List<ReleaseCheck> findAll() {
        return new ArrayList<>(storage.values());
    }

    public Optional<ReleaseCheck> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public ReleaseCheck save(ReleaseCheck check) {
        if (check.getId() == null) {
            check.setId(idCounter.getAndIncrement());
        }
        storage.put(check.getId(), check);
        return check;
    }

    public boolean deleteById(Long id) {
        return storage.remove(id) != null;
    }
}