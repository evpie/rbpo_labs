package com.example.lab2.service;

import com.example.lab2.model.Role;
import com.example.lab2.model.User;
import com.example.lab2.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // инициализация
    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setName("Главный Админ");
            admin.setEmail("admin@test.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            admin.setActive(true);
            userRepository.save(admin);
            System.out.println("Создан админ: admin@test.com / admin123");
        }
    }
}