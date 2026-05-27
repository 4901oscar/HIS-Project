package com.medframe.clinical.infrastructure.rest.controller;

import com.medframe.clinical.domain.model.Prescription;
import com.medframe.clinical.domain.port.in.GeneratePrescriptionUseCase;
import com.medframe.clinical.infrastructure.rest.dto.request.PrescriptionRequest;
import com.medframe.clinical.infrastructure.rest.dto.response.PrescriptionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/clinical/prescriptions")
@Validated
@RequiredArgsConstructor
public class PrescriptionController {
    
    private final GeneratePrescriptionUseCase generatePrescriptionUseCase;
    
    @PostMapping
    public ResponseEntity<PrescriptionResponse> generatePrescription(
            @Valid @RequestBody PrescriptionRequest request,
            @RequestHeader("X-User-Id") String userId) {
        
        // Map request medications to domain medications
        List<Prescription.Medication> medications = request.getMedications().stream()
            .map(med -> new Prescription.Medication(
                med.getName(),
                med.getDosage(),
                med.getFrequency(),
                med.getDurationDays(),
                med.getRoute(),
                med.getSpecialInstructions(),
                med.getDosageAmount(),
                med.getDosageUnit(),
                med.getFrequencyHours(),
                med.getTotalQuantity()
            ))
            .collect(Collectors.toList());
        
        Prescription prescription = generatePrescriptionUseCase.generatePrescription(
            request.getConsultationId(),
            request.getPatientId(),
            userId,
            medications
        );
        
        PrescriptionResponse response = mapToResponse(prescription);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    private PrescriptionResponse mapToResponse(Prescription prescription) {
        return new PrescriptionResponse(
            prescription.getId(),
            prescription.getPrescriptionCode(),
            prescription.getPatientId(),
            prescription.getDoctorId(),
            prescription.getMedications(),
            prescription.getStatus().name(),
            prescription.getIssuedAt()
        );
    }
}
