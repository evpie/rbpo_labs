package com.example.lab2.repository;

import com.example.lab2.model.ProductRelease;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class ProductReleaseRepository {
    private final Map<Long, ProductRelease> storage = new HashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public List<ProductRelease> findAll() {
        return new ArrayList<>(storage.values());
    }

    public Optional<ProductRelease> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public ProductRelease save(ProductRelease release) {
        if (release.getId() == null) {
            release.setId(idCounter.getAndIncrement());
        }
        storage.put(release.getId(), release);
        return release;
    }

    public boolean deleteById(Long id) {
        return storage.remove(id) != null;
    }
}