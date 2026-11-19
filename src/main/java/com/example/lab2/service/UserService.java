package com.example.lab2.service;

import com.example.lab2.model.User;
import com.example.lab2.repository.DevelopmentTaskRepository;
import com.example.lab2.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.lab2.model.Role;
import com.example.lab2.model.RegisterRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final DevelopmentTaskRepository taskRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       DevelopmentTaskRepository taskRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.passwordEncoder = passwordEncoder;
    }


    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User create(User user) {
        user.setId(null);
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
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

    @Transactional
    public void fireUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

        taskRepository.findAll().stream()
                .filter(task -> task.getAssignee() != null && userId.equals(task.getAssignee().getId()))
                .forEach(task -> {
                    task.setAssignee(null);
                    taskRepository.save(task);
                });

        userRepository.delete(user);
    }

    // новый метод регистрации
    public User register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Пароли не совпадают");
        }

        if (request.getPassword().length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Пароль должен быть не менее 6 символов");
        }

        boolean emailExists = userRepository.existsByEmail(request.getEmail());
        if (emailExists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Пользователь с таким email уже существует");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.DEVELOPER);
        user.setActive(true);

        return userRepository.save(user);
    }

    public User login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный логин или пароль"));

        // Проверяем, что пароль совпадает с хэшем в базе
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный логин или пароль");
        }

        // Проверяем, не уволен ли он
        if (!user.isActive()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Учетная запись заблокирована");
        }

        return user;
    }
}
