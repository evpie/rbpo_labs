package com.example.lab2.service;

import com.example.lab2.model.SoftwareProduct;
import com.example.lab2.repository.DevelopmentTaskRepository;
import com.example.lab2.repository.ProductReleaseRepository;
import com.example.lab2.repository.SoftwareProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SoftwareProductService {

    private final SoftwareProductRepository repository;
    private final DevelopmentTaskRepository taskRepository;
    private final ProductReleaseRepository releaseRepository;

    public SoftwareProductService(SoftwareProductRepository repository,
                                  DevelopmentTaskRepository taskRepository,
                                  ProductReleaseRepository releaseRepository) {
        this.repository = repository;
        this.taskRepository = taskRepository;
        this.releaseRepository = releaseRepository;
    }

    public List<SoftwareProduct> findAll() { return repository.findAll(); }
    public Optional<SoftwareProduct> findById(Long id) { return repository.findById(id); }

    public SoftwareProduct create(SoftwareProduct product) {
        product.setId(null);
        return repository.save(product);
    }

    public Optional<SoftwareProduct> update(Long id, SoftwareProduct updates) {
        return repository.findById(id).map(existing -> {
            if (updates.getName() != null) existing.setName(updates.getName());
            if (updates.getResponsibleUserId() != null) existing.setResponsibleUserId(updates.getResponsibleUserId());
            if (updates.getCurrentVersion() != null) existing.setCurrentVersion(updates.getCurrentVersion());
            if (updates.getStatus() != null) existing.setStatus(updates.getStatus());
            return repository.save(existing);
        });
    }

    public boolean delete(Long id) {
        return repository.findById(id).map(product -> {
            // Каскадное удаление задач
            taskRepository.findAll().stream()
                    .filter(task -> product.equals(task.getProduct()))
                    .forEach(taskRepository::delete);

            // Каскадное удаление релизов
            releaseRepository.findAll().stream()
                    .filter(release -> product.equals(release.getProduct()))
                    .forEach(releaseRepository::delete);

            repository.delete(product);
            return true;
        }).orElse(false);
    }
}