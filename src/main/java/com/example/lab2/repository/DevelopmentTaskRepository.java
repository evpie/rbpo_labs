package com.example.lab2.repository;

import com.example.lab2.model.DevelopmentTask;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class DevelopmentTaskRepository {
    private final Map<Long, DevelopmentTask> storage = new HashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public List<DevelopmentTask> findAll() {
        return new ArrayList<>(storage.values());
    }

    public Optional<DevelopmentTask> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public DevelopmentTask save(DevelopmentTask task) {
        if (task.getId() == null) {
            task.setId(idCounter.getAndIncrement());
        }
        storage.put(task.getId(), task);
        return task;
    }

    public boolean deleteById(Long id) {
        return storage.remove(id) != null;
    }
}