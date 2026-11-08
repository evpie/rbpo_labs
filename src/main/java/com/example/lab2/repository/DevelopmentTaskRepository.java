package com.example.lab2.repository;

import com.example.lab2.model.DevelopmentTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DevelopmentTaskRepository extends JpaRepository<DevelopmentTask, Long> {
}