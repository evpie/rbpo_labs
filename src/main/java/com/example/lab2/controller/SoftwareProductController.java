package com.example.lab2.controller;

import com.example.lab2.model.SoftwareProduct;
import com.example.lab2.service.SoftwareProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/software-products")
public class SoftwareProductController {

    private final SoftwareProductService service;

    public SoftwareProductController(SoftwareProductService service) {
        this.service = service;
    }

    // 1. GET /api/software-products — все продукты (200 OK)
    @GetMapping
    public ResponseEntity<List<SoftwareProduct>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    // 2. GET /api/software-products/{id} — один продукт (200 OK)
    @GetMapping("/{id}")
    public ResponseEntity<SoftwareProduct> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. POST /api/software-products — создать (201 CREATED)
    @PostMapping
    public ResponseEntity<SoftwareProduct> create(@RequestBody SoftwareProduct product) {
        SoftwareProduct created = service.create(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // 4. PATCH /api/software-products/{id} — обновить (200 OK)
    @PatchMapping("/{id}")
    public ResponseEntity<SoftwareProduct> update(@PathVariable Long id, @RequestBody SoftwareProduct product) {
        return service.update(id, product)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 5. DELETE /api/software-products/{id} — удалить (204 NO CONTENT)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (service.delete(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}