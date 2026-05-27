package com.medframe.clinical.infrastructure.rest.controller;

import com.medframe.clinical.domain.port.in.GetMedicalHistoryUseCase;
import com.medframe.clinical.domain.port.in.GetMedicalHistoryUseCase.MedicalHistory;
import com.medframe.clinical.infrastructure.rest.dto.response.MedicalHistoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clinical/history")
@Validated
@RequiredArgsConstructor
public class MedicalHistoryController {
    
    private final GetMedicalHistoryUseCase getMedicalHistoryUseCase;
    
    @GetMapping("/{patientId}")
    public ResponseEntity<MedicalHistoryResponse> getMedicalHistory(
            @PathVariable String patientId,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Roles") String roles) {
        
        MedicalHistory history = getMedicalHistoryUseCase.getMedicalHistory(
            patientId,
            userId,
            roles
        );
        
        MedicalHistoryResponse response = mapToResponse(history);
        return ResponseEntity.ok(response);
    }
    
    private MedicalHistoryResponse mapToResponse(MedicalHistory history) {
        return new MedicalHistoryResponse(
            history.patient(),
            history.consultations(),
            history.vitalSigns(),
            history.prescriptions(),
            history.labOrders()
        );
    }
}
