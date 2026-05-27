package com.medframe.clinical.infrastructure.rest.controller;

import com.medframe.clinical.domain.model.VitalSigns;
import com.medframe.clinical.domain.port.in.RecordVitalSignsUseCase;
import com.medframe.clinical.infrastructure.rest.dto.request.VitalSignsRequest;
import com.medframe.clinical.infrastructure.rest.dto.response.VitalSignsResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clinical/vital-signs")
@Validated
@RequiredArgsConstructor
public class VitalSignsController {
    
    private final RecordVitalSignsUseCase recordVitalSignsUseCase;
    
    @PostMapping
    public ResponseEntity<VitalSignsResponse> recordVitalSigns(
            @Valid @RequestBody VitalSignsRequest request,
            @RequestHeader("X-User-Id") String userId) {
        
        VitalSigns vitalSigns = recordVitalSignsUseCase.recordVitalSigns(
            request.getAppointmentId(),
            request.getPatientId(),
            request.getSystolicPressure(),
            request.getDiastolicPressure(),
            request.getHeartRate(),
            request.getRespiratoryRate(),
            request.getTemperature(),
            request.getOxygenSaturation(),
            request.getWeight(),
            request.getHeight(),
            userId
        );
        
        VitalSignsResponse response = mapToResponse(vitalSigns);
        return ResponseEntity.ok(response);
    }
    
    private VitalSignsResponse mapToResponse(VitalSigns vitalSigns) {
        return new VitalSignsResponse(
            vitalSigns.getId(),
            vitalSigns.getPatientId(),
            vitalSigns.getSystolicPressure(),
            vitalSigns.getDiastolicPressure(),
            vitalSigns.getHeartRate(),
            vitalSigns.getRespiratoryRate(),
            vitalSigns.getTemperature(),
            vitalSigns.getOxygenSaturation(),
            vitalSigns.getWeight(),
            vitalSigns.getHeight(),
            vitalSigns.getBmi(),
            vitalSigns.getRecordedAt()
        );
    }
}
