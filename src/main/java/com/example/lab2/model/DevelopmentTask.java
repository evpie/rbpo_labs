package com.example.lab2.model;

import jakarta.persistence.*;

@Entity
public class DevelopmentTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Связь с продуктом
    @ManyToOne
    @JoinColumn(name = "product_id")
    private SoftwareProduct product;

    // Связь с юзером (исполнителем)
    @ManyToOne
    @JoinColumn(name = "assignee_id")
    private User assignee;

    private String version;
    private String status;

    public DevelopmentTask() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public SoftwareProduct getProduct() { return product; }
    public void setProduct(SoftwareProduct product) { this.product = product; }

    public User getAssignee() { return assignee; }
    public void setAssignee(User assignee) { this.assignee = assignee; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}