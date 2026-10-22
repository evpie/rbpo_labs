package com.example.lab2.service;

import com.example.lab2.model.DevelopmentTask;
import com.example.lab2.repository.DevelopmentTaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DevelopmentTaskService {

    private final DevelopmentTaskRepository repository;

    public DevelopmentTaskService(DevelopmentTaskRepository repository) {
        this.repository = repository;
    }

    public List<DevelopmentTask> findAll() {
        return repository.findAll();
    }

    public Optional<DevelopmentTask> findById(Long id) {
        return repository.findById(id);
    }

    public DevelopmentTask create(DevelopmentTask task) {
        task.setId(null); // id присвоит репозиторий
        return repository.save(task);
    }

    public Optional<DevelopmentTask> update(Long id, DevelopmentTask updates) {
        return repository.findById(id).map(existing -> {
            if (updates.getProductId() != null) existing.setProductId(updates.getProductId());
            if (updates.getAssigneeId() != null) existing.setAssigneeId(updates.getAssigneeId());
            if (updates.getVersion() != null) existing.setVersion(updates.getVersion());
            if (updates.getStatus() != null) existing.setStatus(updates.getStatus());
            return repository.save(existing);
        });
    }

    public boolean delete(Long id) {
        return repository.deleteById(id);
    }
}