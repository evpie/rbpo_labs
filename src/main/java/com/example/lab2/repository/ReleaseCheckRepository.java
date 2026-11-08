package com.example.lab2.repository;

import com.example.lab2.model.ReleaseCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReleaseCheckRepository extends JpaRepository<ReleaseCheck, Long> {
}