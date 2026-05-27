package com.medflow.pharmacy.service;

import com.medflow.pharmacy.dto.request.MedicationRequest;
import com.medflow.pharmacy.dto.response.MedicationResponse;
import com.medflow.pharmacy.model.Medication;
import com.medflow.pharmacy.model.MedicationStatus;
import com.medflow.pharmacy.repository.MedicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InventoryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);
    private final MedicationRepository medicationRepository;

    public InventoryService(MedicationRepository medicationRepository) {
        this.medicationRepository = medicationRepository;
    }

    public List<MedicationResponse> getMedications() {
        return medicationRepository.findByStatusNot(MedicationStatus.DELETED).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public MedicationResponse addMedication(MedicationRequest request, String userId) {
        Medication medication = new Medication();
        medication.setName(request.getName());
        medication.setDescription(request.getDescription());
        medication.setUnit(request.getUnit());
        medication.setCurrentStock(request.getCurrentStock());
        medication.setMinStock(request.getMinStock());
        medication.setStatus(MedicationStatus.ACTIVE);
        medication.setCreatedBy(userId != null ? userId : "internal");
        return mapToResponse(medicationRepository.save(medication));
    }

    @Transactional
    public MedicationResponse updateMedication(String id, MedicationRequest request, String userId) {
        Medication medication = medicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se encontro el medicamento: " + id));
        medication.setName(request.getName());
        medication.setDescription(request.getDescription());
        medication.setUnit(request.getUnit());
        medication.setMinStock(request.getMinStock());
        if (request.getStatus() != null) {
            medication.setStatus(request.getStatus());
        }
        medication.setUpdatedBy(userId != null ? userId : "internal");
        return mapToResponse(medicationRepository.save(medication));
    }

    @Transactional
    public MedicationResponse updateStock(String medicationId, Integer newStock, String userId) {
        if (newStock < 0) throw new IllegalArgumentException("El stock no puede ser negativo");
        Medication medication = medicationRepository.findById(medicationId)
                .orElseThrow(() -> new RuntimeException("No se encontro el medicamento: " + medicationId));
        medication.setCurrentStock(newStock);
        medication.setUpdatedBy(userId != null ? userId : "internal");
        return mapToResponse(medicationRepository.save(medication));
    }

    @Transactional
    public MedicationResponse toggleActive(String id, String userId) {
        Medication medication = medicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se encontro el medicamento: " + id));
        if (medication.getStatus() == MedicationStatus.DELETED) {
            throw new IllegalStateException("No se puede cambiar el estado de un medicamento eliminado");
        }
        MedicationStatus next = medication.getStatus() == MedicationStatus.ACTIVE
                ? MedicationStatus.INACTIVE
                : MedicationStatus.ACTIVE;
        medication.setStatus(next);
        medication.setUpdatedBy(userId != null ? userId : "internal");
        return mapToResponse(medicationRepository.save(medication));
    }

    @Transactional
    public void delete(String id, String userId) {
        Medication medication = medicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se encontro el medicamento: " + id));
        medication.setStatus(MedicationStatus.DELETED);
        medication.setUpdatedBy(userId != null ? userId : "internal");
        medicationRepository.save(medication);
    }

    public List<MedicationResponse> getLowStockMedications() {
        return medicationRepository.findByStatus(MedicationStatus.ACTIVE).stream()
                .filter(med -> med.getCurrentStock() < med.getMinStock())
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
        response.setStatus(medication.getStatus());
        response.setLowStock(medication.getCurrentStock() < medication.getMinStock());
        return response;
    }
}
