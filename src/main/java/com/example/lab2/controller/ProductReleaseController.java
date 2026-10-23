package com.example.lab2.controller;

import com.example.lab2.model.ProductRelease;
import com.example.lab2.service.ProductReleaseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/releases")
public class ProductReleaseController {

    private final ProductReleaseService service;

    public ProductReleaseController(ProductReleaseService service) {
        this.service = service;
    }

    // 1. GET — все релизы (200 OK)
    @GetMapping
    public ResponseEntity<List<ProductRelease>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    // 2. GET — один релиз (200 OK)
    @GetMapping("/{id}")
    public ResponseEntity<ProductRelease> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. POST — создать (201 CREATED)
    @PostMapping
    public ResponseEntity<ProductRelease> create(@RequestBody ProductRelease release) {
        ProductRelease created = service.create(release);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // 4. PATCH — обновить (200 OK)
    @PatchMapping("/{id}")
    public ResponseEntity<ProductRelease> update(@PathVariable Long id, @RequestBody ProductRelease release) {
        return service.update(id, release)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 5. DELETE — удалить (204 NO CONTENT)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (service.delete(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}