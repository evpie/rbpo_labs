package com.example.lab2.controller;

import com.example.lab2.model.ReleaseCheck;
import com.example.lab2.service.ReleaseCheckService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/release-checks")
public class ReleaseCheckController {

    private final ReleaseCheckService service;

    public ReleaseCheckController(ReleaseCheckService service) {
        this.service = service;
    }

    // 1. GET — все проверки (200 OK)
    @GetMapping
    public ResponseEntity<List<ReleaseCheck>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    // 2. GET — одна проверка (200 OK)
    @GetMapping("/{id}")
    public ResponseEntity<ReleaseCheck> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. POST — создать (201 CREATED)
    @PostMapping
    public ResponseEntity<ReleaseCheck> create(@RequestBody ReleaseCheck check) {
        ReleaseCheck created = service.create(check);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // 4. PATCH — обновить (200 OK)
    @PatchMapping("/{id}")
    public ResponseEntity<ReleaseCheck> update(@PathVariable Long id, @RequestBody ReleaseCheck check) {
        return service.update(id, check)
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