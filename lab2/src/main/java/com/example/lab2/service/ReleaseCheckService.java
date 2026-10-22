package com.example.lab2.service;

import com.example.lab2.model.ReleaseCheck;
import com.example.lab2.repository.ReleaseCheckRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReleaseCheckService {

    private final ReleaseCheckRepository repository;

    public ReleaseCheckService(ReleaseCheckRepository repository) {
        this.repository = repository;
    }

    public List<ReleaseCheck> findAll() {
        return repository.findAll();
    }

    public Optional<ReleaseCheck> findById(Long id) {
        return repository.findById(id);
    }

    public ReleaseCheck create(ReleaseCheck check) {
        check.setId(null);
        return repository.save(check);
    }

    public Optional<ReleaseCheck> update(Long id, ReleaseCheck updates) {
        return repository.findById(id).map(existing -> {
            if (updates.getReleaseId() != null) existing.setReleaseId(updates.getReleaseId());
            if (updates.getCheckName() != null) existing.setCheckName(updates.getCheckName());
            // Для boolean обновляем всегда, так как false - это тоже значение
            existing.setMandatory(updates.isMandatory());
            if (updates.getResult() != null) existing.setResult(updates.getResult());
            return repository.save(existing);
        });
    }

    public boolean delete(Long id) {
        return repository.deleteById(id);
    }
}