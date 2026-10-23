package com.example.lab2.repository;

import com.example.lab2.model.SoftwareProduct;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class SoftwareProductRepository {
    private final Map<Long, SoftwareProduct> storage = new HashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public List<SoftwareProduct> findAll() {
        return new ArrayList<>(storage.values());
    }

    public Optional<SoftwareProduct> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public SoftwareProduct save(SoftwareProduct product) {
        if (product.getId() == null) {
            product.setId(idCounter.getAndIncrement());
        }
        storage.put(product.getId(), product);
        return product;
    }

    public boolean deleteById(Long id) {
        return storage.remove(id) != null;
    }
}