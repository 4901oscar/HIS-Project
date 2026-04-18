package com.medflow.pharmacy.service;

import com.medflow.pharmacy.dto.request.MedicationRequest;
import com.medflow.pharmacy.dto.response.MedicationResponse;
import com.medflow.pharmacy.model.Medication;
import com.medflow.pharmacy.repository.MedicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio para gestionar el inventario de medicamentos.
 */
@Service
public class InventoryService {
    
    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);
    
    private final MedicationRepository medicationRepository;
    
    public InventoryService(MedicationRepository medicationRepository) {
        this.medicationRepository = medicationRepository;
    }
    
    /**
     * Obtiene todos los medicamentos activos.
     */
    public List<MedicationResponse> getMedications() {
        List<Medication> medications = medicationRepository.findByActiveTrue();
        
        return medications.stream()
                .map(this::mapToResponse)
                .toList();
    }
    
    /**
     * Agrega un nuevo medicamento al catálogo.
     */
    @Transactional
    public MedicationResponse addMedication(MedicationRequest request) {
        logger.info("Adding new medication: {}", request.getName());
        
        Medication medication = new Medication();
        medication.setName(request.getName());
        medication.setDescription(request.getDescription());
        medication.setUnit(request.getUnit());
        medication.setCurrentStock(request.getCurrentStock());
        medication.setMinStock(request.getMinStock());
        medication.setActive(true);
        
        Medication saved = medicationRepository.save(medication);
        logger.info("Medication added successfully: {}", saved.getId());
        
        return mapToResponse(saved);
    }
    
    /**
     * Actualiza el stock de un medicamento.
     * 
     * BR6: Stock nunca puede ser negativo
     */
    @Transactional
    public MedicationResponse updateStock(String medicationId, Integer newStock) {
        logger.info("Updating stock for medication: {}", medicationId);
        
        // Validar que el stock no sea negativo (BR6)
        if (newStock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        
        Medication medication = medicationRepository.findById(medicationId)
                .orElseThrow(() -> new RuntimeException(
                        "No se encontró el medicamento con ID: " + medicationId));
        
        medication.setCurrentStock(newStock);
        Medication updated = medicationRepository.save(medication);
        
        logger.info("Stock updated successfully for {}: {}", medication.getName(), newStock);
        return mapToResponse(updated);
    }
    
    /**
     * Obtiene medicamentos con stock bajo (menor al mínimo).
     */
    public List<MedicationResponse> getLowStockMedications() {
        List<Medication> medications = medicationRepository.findAll();
        
        return medications.stream()
                .filter(med -> med.isActive() && med.getCurrentStock() < med.getMinStock())
                .map(this::mapToResponse)
                .toList();
    }
    
    private MedicationResponse mapToResponse(Medication medication) {
        MedicationResponse response = new MedicationResponse();
        response.setId(medication.getId());
        response.setName(medication.getName());
        response.setDescription(medication.getDescription());
        response.setUnit(medication.getUnit());
        response.setCurrentStock(medication.getCurrentStock());
        response.setMinStock(medication.getMinStock());
        response.setActive(medication.isActive());
        response.setLowStock(medication.getCurrentStock() < medication.getMinStock());
        
        return response;
    }
}
