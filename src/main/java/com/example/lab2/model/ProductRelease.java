package com.example.lab2.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class ProductRelease {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Связь с продуктом
    @ManyToOne
    @JoinColumn(name = "product_id")
    private SoftwareProduct product;

    private String versionNumber;
    private String status;
    private LocalDateTime releaseDate;

    public ProductRelease() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public SoftwareProduct getProduct() { return product; }
    public void setProduct(SoftwareProduct product) { this.product = product; }

    public String getVersionNumber() { return versionNumber; }
    public void setVersionNumber(String versionNumber) { this.versionNumber = versionNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getReleaseDate() { return releaseDate; }
    public void setReleaseDate(LocalDateTime releaseDate) { this.releaseDate = releaseDate; }
}