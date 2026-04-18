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

/**
 * Controlador REST para gestionar el inventario de medicamentos.
 */
@RestController
@RequestMapping("/api/pharmacy/medications")
public class MedicationController {
    
    private final InventoryService inventoryService;
    
    public MedicationController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }
    
    /**
     * GET /api/pharmacy/medications
     * Lista todos los medicamentos activos.
     */
    @GetMapping
    public ResponseEntity<List<MedicationResponse>> getMedications() {
        List<MedicationResponse> medications = inventoryService.getMedications();
        return ResponseEntity.ok(medications);
    }
    
    /**
     * POST /api/pharmacy/medications
     * Agrega un nuevo medicamento al catálogo.
     */
    @PostMapping
    public ResponseEntity<MedicationResponse> addMedication(
            @Valid @RequestBody MedicationRequest request) {
        MedicationResponse response = inventoryService.addMedication(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * PUT /api/pharmacy/medications/{id}/stock
     * Actualiza el stock de un medicamento.
     */
    @PutMapping("/{id}/stock")
    public ResponseEntity<MedicationResponse> updateStock(
            @PathVariable String id,
            @Valid @RequestBody StockUpdateRequest request) {
        MedicationResponse response = inventoryService.updateStock(id, request.getNewStock());
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/pharmacy/medications/low-stock
     * Obtiene medicamentos con stock bajo (menor al mínimo).
     */
    @GetMapping("/low-stock")
    public ResponseEntity<List<MedicationResponse>> getLowStockMedications() {
        List<MedicationResponse> medications = inventoryService.getLowStockMedications();
        return ResponseEntity.ok(medications);
    }
}
