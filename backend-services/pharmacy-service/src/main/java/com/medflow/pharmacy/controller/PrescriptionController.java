package com.medflow.pharmacy.controller;

import com.medflow.pharmacy.dto.request.PrescriptionNotificationRequest;
import com.medflow.pharmacy.dto.response.PrescriptionResponse;
import com.medflow.pharmacy.model.PrescriptionStatus;
import com.medflow.pharmacy.service.PrescriptionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestionar prescripciones médicas.
 */
@RestController
@RequestMapping("/api/pharmacy/prescriptions")
public class PrescriptionController {
    
    private final PrescriptionService prescriptionService;
    
    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }
    
    /**
     * POST /api/pharmacy/prescriptions/notify
     * Recibe notificación de nueva prescripción desde Clinical Service.
     */
    @PostMapping("/notify")
    public ResponseEntity<PrescriptionResponse> receivePrescription(
            @Valid @RequestBody PrescriptionNotificationRequest request) {
        PrescriptionResponse response = prescriptionService.receivePrescription(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * GET /api/pharmacy/prescriptions?status=PENDING
     * Lista prescripciones con filtro opcional por estado.
     */
    @GetMapping
    public ResponseEntity<List<PrescriptionResponse>> getPrescriptions(
            @RequestParam(required = false) PrescriptionStatus status) {
        List<PrescriptionResponse> prescriptions = prescriptionService.getPrescriptions(status);
        return ResponseEntity.ok(prescriptions);
    }
    
    /**
     * GET /api/pharmacy/prescriptions/{code}
     * Busca una prescripción por su código único.
     */
    @GetMapping("/{code}")
    public ResponseEntity<PrescriptionResponse> getPrescriptionByCode(@PathVariable String code) {
        PrescriptionResponse prescription = prescriptionService.getPrescriptionByCode(code);
        return ResponseEntity.ok(prescription);
    }
    
    /**
     * PUT /api/pharmacy/prescriptions/{id}/dispense
     * Despacha una prescripción (marca como DISPENSED y descuenta stock).
     */
    @PutMapping("/{id}/dispense")
    public ResponseEntity<PrescriptionResponse> dispense(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String dispensedBy) {
        PrescriptionResponse response = prescriptionService.dispense(id, dispensedBy);
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/pharmacy/prescriptions/patient/{patientId}
     * Obtiene las prescripciones de un paciente específico.
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<PrescriptionResponse>> getPatientPrescriptions(
            @PathVariable String patientId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Roles") String userRole) {
        List<PrescriptionResponse> prescriptions = 
                prescriptionService.getPatientPrescriptions(patientId, userId, userRole);
        return ResponseEntity.ok(prescriptions);
    }
}
