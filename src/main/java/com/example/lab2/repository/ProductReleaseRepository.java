package com.example.lab2.repository;

import com.example.lab2.model.ProductRelease;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductReleaseRepository extends JpaRepository<ProductRelease, Long> {
}