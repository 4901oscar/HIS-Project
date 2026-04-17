package com.medframe.clinical.infrastructure.rest.controller;

import com.medframe.clinical.domain.model.Consultation;
import com.medframe.clinical.domain.port.in.RegisterConsultationUseCase;
import com.medframe.clinical.infrastructure.rest.dto.request.ConsultationRequest;
import com.medframe.clinical.infrastructure.rest.dto.response.ConsultationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clinical/consultations")
@Validated
@RequiredArgsConstructor
public class ConsultationController {
    
    private final RegisterConsultationUseCase registerConsultationUseCase;
    
    @PostMapping
    public ResponseEntity<ConsultationResponse> registerConsultation(
            @Valid @RequestBody ConsultationRequest request,
            @RequestHeader("X-User-Id") String userId) {
        
        Consultation consultation = registerConsultationUseCase.registerConsultation(
            request.getPatientId(),
            userId,
            request.getAppointmentId(),
            request.getChiefComplaint(),
            request.getSymptoms(),
            request.getPrimaryDiagnosis(),
            request.getSecondaryDiagnoses(),
            request.getMedicalNotes(),
            request.getTreatmentPlan()
        );
        
        ConsultationResponse response = mapToResponse(consultation);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    private ConsultationResponse mapToResponse(Consultation consultation) {
        return new ConsultationResponse(
            consultation.getId(),
            consultation.getPatientId(),
            consultation.getDoctorId(),
            consultation.getChiefComplaint(),
            consultation.getPrimaryDiagnosis(),
            consultation.getSecondaryDiagnoses(),
            consultation.getConsultationDate()
        );
    }
}
