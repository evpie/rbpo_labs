package com.example.lab2.service;

import com.example.lab2.model.User;
import com.example.lab2.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User create(User user) {
        user.setId(null); // id присвоит репозиторий
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
        return userRepository.deleteById(id);
    }
}