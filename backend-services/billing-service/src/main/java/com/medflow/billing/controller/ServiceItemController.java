package com.medflow.billing.controller;

import com.medflow.billing.dto.request.ServiceItemRequest;
import com.medflow.billing.dto.response.ServiceItemResponse;
import com.medflow.billing.service.ServiceItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/billing/services")
@RequiredArgsConstructor
public class ServiceItemController {

    private final ServiceItemService service;

    @GetMapping
    public ResponseEntity<List<ServiceItemResponse>> getAll(
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(service.getAll(category));
    }

    @PostMapping
    public ResponseEntity<ServiceItemResponse> create(@Valid @RequestBody ServiceItemRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceItemResponse> update(@PathVariable String id, @Valid @RequestBody ServiceItemRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ServiceItemResponse> toggle(@PathVariable String id) {
        return ResponseEntity.ok(service.toggleActive(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
