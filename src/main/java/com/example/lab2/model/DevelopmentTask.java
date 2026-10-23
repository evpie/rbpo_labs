package com.example.lab2.model;

public class DevelopmentTask {
    private Long id;
    private Long productId;   // Ссылка на SoftwareProduct
    private Long assigneeId;  // Ссылка на User (исполнитель)
    private String version;
    private String status;    // Например, "TODO", "IN_PROGRESS", "DONE"

    public DevelopmentTask() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}