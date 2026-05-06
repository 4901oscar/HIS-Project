package com.medflow.pharmacy.controller;

import com.medflow.pharmacy.dto.request.MedicationRequest;
import com.medflow.pharmacy.dto.request.StockUpdateRequest;
import com.medflow.pharmacy.dto.response.MedicationResponse;
import com.medflow.pharmacy.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pharmacy/medications")
public class MedicationController {

    private final InventoryService inventoryService;

    public MedicationController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public ResponseEntity<List<MedicationResponse>> getMedications() {
        return ResponseEntity.ok(inventoryService.getMedications());
    }

    @PostMapping
    public ResponseEntity<MedicationResponse> addMedication(@Valid @RequestBody MedicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.addMedication(request));
    }


    @PutMapping("/{id}")
    public ResponseEntity<MedicationResponse> updateMedication(
            @PathVariable String id, @Valid @RequestBody MedicationRequest request) {
        return ResponseEntity.ok(inventoryService.updateMedication(id, request));
    }
    @PutMapping("/{id}/stock")
    public ResponseEntity<MedicationResponse> updateStock(
            @PathVariable String id, @Valid @RequestBody StockUpdateRequest request) {
        return ResponseEntity.ok(inventoryService.updateStock(id, request.getNewStock()));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<MedicationResponse> toggleActive(@PathVariable String id) {
        return ResponseEntity.ok(inventoryService.toggleActive(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        inventoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<MedicationResponse>> getLowStockMedications() {
        return ResponseEntity.ok(inventoryService.getLowStockMedications());
    }
}
