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

    public List<SoftwareProduct> findAll() {
        return repository.findAll();
    }

    public Optional<SoftwareProduct> findById(Long id) {
        return repository.findById(id);
    }

    public SoftwareProduct create(SoftwareProduct product) {
        product.setId(null);
        return repository.save(product);
    }

    public Optional<SoftwareProduct> update(Long id, SoftwareProduct updates) {
        return repository.findById(id).map(existing -> {
            if (updates.getName() != null) existing.setName(updates.getName());
            if (updates.getResponsibleUserId() != null) existing.setResponsibleUserId(updates.getResponsibleUserId());
            if (updates.getCurrentVersion() != null) existing.setCurrentVersion(updates.getCurrentVersion());
            return repository.save(existing);
        });
    }

    public boolean delete(Long id) {
        // Условие из задания: удаление связанных объектов не должно оставлять недействительных ссылок
        // 1. Удаляем все задачи, привязанные к этому продукту
        taskRepository.findAll().stream()
                .filter(task -> id.equals(task.getProductId()))
                .forEach(task -> taskRepository.deleteById(task.getId()));

        // 2. Удаляем все релизы, привязанные к этому продукту
        releaseRepository.findAll().stream()
                .filter(release -> id.equals(release.getProductId()))
                .forEach(release -> releaseRepository.deleteById(release.getId()));

        // 3. Удаляем сам продукт
        return repository.deleteById(id);
    }
}