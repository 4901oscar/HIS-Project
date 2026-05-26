package com.medflow.pharmacy.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medflow.pharmacy.dto.request.PrescriptionNotificationRequest;
import com.medflow.pharmacy.dto.response.PrescriptionResponse;
import com.medflow.pharmacy.exception.InsufficientStockException;
import com.medflow.pharmacy.exception.InvalidPrescriptionStatusException;
import com.medflow.pharmacy.exception.PrescriptionNotFoundException;
import com.medflow.pharmacy.exception.UnauthorizedException;
import com.medflow.pharmacy.model.Dispensation;
import com.medflow.pharmacy.model.Medication;
import com.medflow.pharmacy.model.Prescription;
import com.medflow.pharmacy.model.PrescriptionStatus;
import com.medflow.pharmacy.repository.DispensationRepository;
import com.medflow.pharmacy.repository.MedicationRepository;
import com.medflow.pharmacy.repository.PrescriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Servicio para gestionar prescripciones médicas.
 */
@Service
public class PrescriptionService {
    
    private static final Logger logger = LoggerFactory.getLogger(PrescriptionService.class);
    
    private final PrescriptionRepository prescriptionRepository;
    private final MedicationRepository medicationRepository;
    private final DispensationRepository dispensationRepository;
    private final ObjectMapper objectMapper;
    
    public PrescriptionService(
            PrescriptionRepository prescriptionRepository,
            MedicationRepository medicationRepository,
            DispensationRepository dispensationRepository,
            ObjectMapper objectMapper) {
        this.prescriptionRepository = prescriptionRepository;
        this.medicationRepository = medicationRepository;
        this.dispensationRepository = dispensationRepository;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Recibe una nueva prescripción desde Clinical Service.
     */
    @Transactional
    public PrescriptionResponse receivePrescription(PrescriptionNotificationRequest request) {
        logger.info("Receiving prescription with code: {}", request.getPrescriptionCode());
        
        // Verificar si ya existe
        if (prescriptionRepository.existsByPrescriptionCode(request.getPrescriptionCode())) {
            logger.warn("Prescription already exists: {}", request.getPrescriptionCode());
            return getPrescriptionByCode(request.getPrescriptionCode());
        }
        
        Prescription prescription = new Prescription();
        prescription.setPrescriptionCode(request.getPrescriptionCode());
        prescription.setPatientId(request.getPatientId());
        prescription.setDoctorId(request.getDoctorId());
        prescription.setMedicationsJson(request.getMedicationsJson());
        prescription.setStatus(PrescriptionStatus.PENDING);
        prescription.setIssuedAt(request.getIssuedAt());
        
        Prescription saved = prescriptionRepository.save(prescription);
        logger.info("Prescription saved successfully: {}", saved.getId());
        
        return mapToResponse(saved);
    }
    
    /**
     * Obtiene todas las prescripciones, opcionalmente filtradas por estado.
     */
    public List<PrescriptionResponse> getPrescriptions(PrescriptionStatus status) {
        List<Prescription> prescriptions;
        
        if (status != null) {
            prescriptions = prescriptionRepository.findByStatus(status);
        } else {
            prescriptions = prescriptionRepository.findAll();
        }
        
        return prescriptions.stream()
                .map(this::mapToResponse)
                .toList();
    }
    
    /**
     * Busca una prescripción por su código único.
     */
    public PrescriptionResponse getPrescriptionByCode(String code) {
        Prescription prescription = prescriptionRepository.findByPrescriptionCode(code)
                .orElseThrow(() -> new PrescriptionNotFoundException(
                        "No se encontró la receta con código: " + code));
        
        return mapToResponse(prescription);
    }
    
    /**
     * Despacha una prescripción (lógica crítica con @Transactional).
     * 
     * BR1: Solo recetas PENDING pueden ser despachadas
     * BR2: No se puede despachar si stock insuficiente
     * BR6: Stock nunca puede ser negativo
     */
    @Transactional
    public PrescriptionResponse dispense(String prescriptionId, String dispensedBy) {
        logger.info("Dispensing prescription: {}", prescriptionId);
        
        // 1. Buscar prescripción
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new PrescriptionNotFoundException(
                        "No se encontró la receta con ID: " + prescriptionId));
        
        // 2. Validar estado (BR1)
        if (prescription.getStatus() != PrescriptionStatus.PENDING) {
            throw new InvalidPrescriptionStatusException(
                    "Solo se pueden despachar recetas con estado PENDIENTE");
        }
        
        // 3. Parsear medicamentos desde JSON
        List<Map<String, Object>> medications;
        try {
            medications = objectMapper.readValue(
                    prescription.getMedicationsJson(),
                    new TypeReference<List<Map<String, Object>>>() {}
            );
        } catch (Exception e) {
            logger.error("Error parsing medications JSON", e);
            throw new RuntimeException("Error al procesar los medicamentos de la receta");
        }
        
        // 4. Validar stock para cada medicamento (BR2)
        for (Map<String, Object> med : medications) {
            String medicationName = (String) med.get("name");
            
            medicationRepository.findByNameIgnoreCase(medicationName)
                    .ifPresent(medication -> {
                        if (medication.getCurrentStock() < 1) {
                            throw new InsufficientStockException(
                                    "Stock insuficiente para: " + medicationName);
                        }
                    });
        }
        
        // 5. Descontar stock de cada medicamento (BR6: nunca negativo)
        for (Map<String, Object> med : medications) {
            String medicationName = (String) med.get("name");
            
            medicationRepository.findByNameIgnoreCase(medicationName)
                    .ifPresent(medication -> {
                        int newStock = medication.getCurrentStock() - 1;
                        if (newStock < 0) {
                            throw new InsufficientStockException(
                                    "Stock insuficiente para: " + medicationName);
                        }
                        medication.setCurrentStock(newStock);
                        medicationRepository.save(medication);
                        logger.info("Stock updated for {}: {}", medicationName, newStock);
                    });
        }
        
        // 6. Actualizar estado de prescripción
        prescription.setStatus(PrescriptionStatus.DISPENSED);
        Prescription updated = prescriptionRepository.save(prescription);
        
        // 7. Crear registro de dispensación
        Dispensation dispensation = new Dispensation();
        dispensation.setPrescriptionId(prescriptionId);
        dispensation.setPatientId(prescription.getPatientId());
        dispensation.setDispensedAt(LocalDateTime.now());
        dispensation.setDispensedBy(dispensedBy);
        dispensation.setDispensedMedicationsJson(prescription.getMedicationsJson());
        dispensationRepository.save(dispensation);
        
        logger.info("Prescription dispensed successfully: {}", prescriptionId);
        return mapToResponse(updated);
    }
    
    /**
     * Obtiene las prescripciones de un paciente.
     * 
     * BR3: Paciente solo puede ver sus propias recetas
     */
    public List<PrescriptionResponse> getPatientPrescriptions(String patientId, String userId, String userRole) {
        // Validar acceso (BR3)
        if ("PATIENT".equals(userRole) && !patientId.equals(userId)) {
            throw new UnauthorizedException(
                    "No tiene permiso para ver las recetas de otro paciente");
        }
        
        List<Prescription> prescriptions = prescriptionRepository.findByPatientIdOrderByIssuedAtDesc(patientId);
        
        return prescriptions.stream()
                .map(this::mapToResponse)
                .toList();
    }
    
    private PrescriptionResponse mapToResponse(Prescription prescription) {
        return new PrescriptionResponse(
                prescription.getId(),
                prescription.getPrescriptionCode(),
                prescription.getPatientId(),
                prescription.getDoctorId(),
                prescription.getMedicationsJson(),
                prescription.getStatus(),
                prescription.getIssuedAt(),
                prescription.getUpdatedAt()
        );
    }
}
