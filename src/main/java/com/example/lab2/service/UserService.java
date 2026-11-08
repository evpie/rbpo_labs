package com.example.lab2.service;

import com.example.lab2.model.User;
import com.example.lab2.repository.DevelopmentTaskRepository;
import com.example.lab2.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final DevelopmentTaskRepository taskRepository;

    public UserService(UserRepository userRepository, DevelopmentTaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User create(User user) {
        user.setId(null); // id присвоит база данных
        return userRepository.save(user);
    }

    public Optional<User> update(Long id, User updates) {
        return userRepository.findById(id).map(existing -> {
            if (updates.getName() != null) existing.setName(updates.getName());
            if (updates.getEmail() != null) existing.setEmail(updates.getEmail());
            return userRepository.save(existing);
        });
    }

    public boolean delete(Long id) {
        return userRepository.findById(id).map(user -> {
            userRepository.delete(user);
            return true;
        }).orElse(false);
    }

    public void fireUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Отвязываем его от всех задач
        taskRepository.findAll().stream()
                .filter(task -> user.equals(task.getAssignee()))
                .forEach(task -> {
                    task.setAssignee(null); // Убираем исполнителя
                    taskRepository.save(task); // Сохраняем обновленную задачу
                });

        userRepository.delete(user);
    }
}