package com.example.lab2.controller;

import com.example.lab2.model.DevelopmentTask;
import com.example.lab2.service.DevelopmentTaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/development-tasks")
public class DevelopmentTaskController {

    private final DevelopmentTaskService service;

    public DevelopmentTaskController(DevelopmentTaskService service) {
        this.service = service;
    }

    // 1. GET — все задачи (200 OK)
    @GetMapping
    public ResponseEntity<List<DevelopmentTask>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    // 2. GET — одна задача (200 OK)
    @GetMapping("/{id}")
    public ResponseEntity<DevelopmentTask> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. POST — создать (201 CREATED)
    @PostMapping
    public ResponseEntity<DevelopmentTask> create(@RequestBody DevelopmentTask task) {
        DevelopmentTask created = service.create(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // 4. PATCH — обновить (200 OK)
    @PatchMapping("/{id}")
    public ResponseEntity<DevelopmentTask> update(@PathVariable Long id, @RequestBody DevelopmentTask task) {
        return service.update(id, task)
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