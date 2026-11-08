package com.example.lab2.repository;

import com.example.lab2.model.SoftwareProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SoftwareProductRepository extends JpaRepository<SoftwareProduct, Long> {
}