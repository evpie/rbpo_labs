package com.example.lab2.service;

import com.example.lab2.model.DevelopmentTask;
import com.example.lab2.model.SoftwareProduct;
import com.example.lab2.model.User;
import com.example.lab2.repository.DevelopmentTaskRepository;
import com.example.lab2.repository.SoftwareProductRepository;
import com.example.lab2.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DevelopmentTaskService {

    private final DevelopmentTaskRepository repository;
    private final UserRepository userRepository;
    private final SoftwareProductRepository productRepository;

    public DevelopmentTaskService(DevelopmentTaskRepository repository,
                                  UserRepository userRepository,
                                  SoftwareProductRepository productRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    public List<DevelopmentTask> findAll() { return repository.findAll(); }
    public Optional<DevelopmentTask> findById(Long id) { return repository.findById(id); }

    public DevelopmentTask create(DevelopmentTask task) {
        task.setId(null);
        return repository.save(task);
    }

    public Optional<DevelopmentTask> update(Long id, DevelopmentTask updates) {
        return repository.findById(id).map(existing -> {
            if (updates.getProduct() != null) existing.setProduct(updates.getProduct());
            if (updates.getAssignee() != null) existing.setAssignee(updates.getAssignee());
            if (updates.getVersion() != null) existing.setVersion(updates.getVersion());
            if (updates.getStatus() != null) existing.setStatus(updates.getStatus());
            return repository.save(existing);
        });
    }

    public boolean delete(Long id) {
        return repository.findById(id).map(task -> {
            repository.delete(task);
            return true;
        }).orElse(false);
    }

    public DevelopmentTask assignTask(Long taskId, Long userId) {
        DevelopmentTask task = repository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Задача не найдена"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if ("DONE".equals(task.getStatus())) {
            throw new RuntimeException("Нельзя назначить задачу, которая уже выполнена");
        }

        task.setAssignee(user);
        return repository.save(task);
    }

    public DevelopmentTask startTask(Long taskId) {
        DevelopmentTask task = repository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Задача не найдена"));

        // Берем продукт напрямую из задачи
        SoftwareProduct product = task.getProduct();

        if (product == null) {
            throw new RuntimeException("У задачи нет привязанного продукта");
        }

        if ("RELEASED".equals(product.getStatus())) {
            throw new RuntimeException("Нельзя взять в работу задачу выпущенного продукта");
        }

        task.setStatus("IN_PROGRESS");
        return repository.save(task);
    }
}