package com.example.lab2.model;

import jakarta.persistence.*;

@Entity
public class ReleaseCheck {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Связь с релизом
    @ManyToOne
    @JoinColumn(name = "release_id")
    private ProductRelease release;

    private String checkName;
    private boolean isMandatory;
    private String result;

    public ReleaseCheck() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ProductRelease getRelease() { return release; }
    public void setRelease(ProductRelease release) { this.release = release; }

    public String getCheckName() { return checkName; }
    public void setCheckName(String checkName) { this.checkName = checkName; }

    public boolean isMandatory() { return isMandatory; }
    public void setMandatory(boolean mandatory) { isMandatory = mandatory; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
}