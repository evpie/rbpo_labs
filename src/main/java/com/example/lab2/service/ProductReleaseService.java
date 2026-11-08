package com.example.lab2.service;

import com.example.lab2.model.ProductRelease;
import com.example.lab2.model.ReleaseCheck;
import com.example.lab2.repository.DevelopmentTaskRepository;
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
    private final DevelopmentTaskRepository taskRepository;

    public ProductReleaseService(ProductReleaseRepository repository,
                                 ReleaseCheckRepository checkRepository,
                                 DevelopmentTaskRepository taskRepository) {
        this.repository = repository;
        this.checkRepository = checkRepository;
        this.taskRepository = taskRepository;
    }

    public List<ProductRelease> findAll() { return repository.findAll(); }
    public Optional<ProductRelease> findById(Long id) { return repository.findById(id); }

    public ProductRelease create(ProductRelease release) {
        release.setId(null);
        release.setReleaseDate(LocalDateTime.now());
        return repository.save(release);
    }

    public Optional<ProductRelease> update(Long id, ProductRelease updates) {
        return repository.findById(id).map(existing -> {
            if (updates.getProduct() != null) existing.setProduct(updates.getProduct());
            if (updates.getVersionNumber() != null) existing.setVersionNumber(updates.getVersionNumber());
            if (updates.getStatus() != null) existing.setStatus(updates.getStatus());
            return repository.save(existing);
        });
    }

    public boolean delete(Long id) {
        return repository.findById(id).map(release -> {
            // Каскадно удаляем проверки
            checkRepository.findAll().stream()
                    .filter(check -> release.equals(check.getRelease()))
                    .forEach(checkRepository::delete);

            repository.delete(release);
            return true;
        }).orElse(false);
    }

    public ProductRelease publishRelease(Long releaseId) {
        ProductRelease release = repository.findById(releaseId)
                .orElseThrow(() -> new RuntimeException("Релиз не найден"));

        boolean hasFailedMandatoryCheck = checkRepository.findAll().stream()
                .anyMatch(check -> release.equals(check.getRelease())
                        && check.isMandatory()
                        && !"PASSED".equals(check.getResult()));

        if (hasFailedMandatoryCheck) {
            throw new RuntimeException("Нельзя выпустить релиз: есть проваленные обязательные проверки");
        }

        // Проверяем задачи. Так как продукт теперь объект, сравниваем объекты
        boolean hasUnfinishedTasks = taskRepository.findAll().stream()
                .anyMatch(task -> release.getProduct() != null
                        && release.getProduct().equals(task.getProduct())
                        && release.getVersionNumber().equals(task.getVersion())
                        && !"DONE".equals(task.getStatus()));

        if (hasUnfinishedTasks) {
            throw new RuntimeException("Нельзя выпустить релиз: есть незавершенные задачи для этой версии");
        }

        release.setStatus("RELEASED");
        return repository.save(release);
    }

    public ProductRelease createReleaseWithChecks(ProductRelease release) {
        release.setId(null);
        release.setReleaseDate(LocalDateTime.now());
        if (release.getStatus() == null) release.setStatus("DRAFT");

        ProductRelease savedRelease = repository.save(release);

        String[] defaultChecks = {"Проверка кода", "Проверка на вирусы", "Проверка UI"};
        for (String checkName : defaultChecks) {
            ReleaseCheck check = new ReleaseCheck();
            check.setRelease(savedRelease); // Привязываем объект, а не ID
            check.setCheckName(checkName);
            check.setMandatory(true);
            check.setResult("PENDING");
            checkRepository.save(check);
        }

        return savedRelease;
    }
}