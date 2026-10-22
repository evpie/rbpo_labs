package com.example.lab2.service;

import com.example.lab2.model.ProductRelease;
import com.example.lab2.repository.ProductReleaseRepository;
import com.example.lab2.repository.ReleaseCheckRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProductReleaseService {

    private final ProductReleaseRepository repository;
    private final ReleaseCheckRepository checkRepository;

    public ProductReleaseService(ProductReleaseRepository repository, ReleaseCheckRepository checkRepository) {
        this.repository = repository;
        this.checkRepository = checkRepository;
    }

    public List<ProductRelease> findAll() {
        return repository.findAll();
    }

    public Optional<ProductRelease> findById(Long id) {
        return repository.findById(id);
    }

    public ProductRelease create(ProductRelease release) {
        release.setId(null);
        // Условие из задания: текущее время задается сервером
        release.setReleaseDate(LocalDateTime.now());
        return repository.save(release);
    }

    public Optional<ProductRelease> update(Long id, ProductRelease updates) {
        return repository.findById(id).map(existing -> {
            if (updates.getProductId() != null) existing.setProductId(updates.getProductId());
            if (updates.getVersionNumber() != null) existing.setVersionNumber(updates.getVersionNumber());
            if (updates.getStatus() != null) existing.setStatus(updates.getStatus());
            // Дату выпуска при обновлении не трогаем, она фиксируется при создании
            return repository.save(existing);
        });
    }

    public boolean delete(Long id) {
        // Условие из задания: удаление связанных объектов не должно оставлять недействительных ссылок
        // Удаляем все проверки, привязанные к этому релизу
        checkRepository.findAll().stream()
                .filter(check -> id.equals(check.getReleaseId()))
                .forEach(check -> checkRepository.deleteById(check.getId()));

        return repository.deleteById(id);
    }
}