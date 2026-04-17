package com.medframe.clinical.infrastructure.rest.controller;

import com.medframe.clinical.domain.model.Triage;
import com.medframe.clinical.domain.port.in.PerformTriageUseCase;
import com.medframe.clinical.infrastructure.rest.dto.request.TriageRequest;
import com.medframe.clinical.infrastructure.rest.dto.response.TriageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clinical/triage")
@Validated
@RequiredArgsConstructor
public class TriageController {
    
    private final PerformTriageUseCase performTriageUseCase;
    
    @PostMapping
    public ResponseEntity<TriageResponse> performTriage(
            @Valid @RequestBody TriageRequest request,
            @RequestHeader("X-User-Id") String userId) {
        
        Triage triage = performTriageUseCase.performTriage(
            request.getPatientId(),
            userId,
            request.getMotifId(),
            request.getDiscriminatorIds()
        );
        
        TriageResponse response = mapToResponse(triage);
        return ResponseEntity.ok(response);
    }
    
    private TriageResponse mapToResponse(Triage triage) {
        return new TriageResponse(
            triage.getId(),
            triage.getPatientId(),
            triage.getPriorityLevel().name(),
            triage.getPriorityLevel().getDescription(),
            triage.getMaxWaitTimeMinutes(),
            triage.getPerformedAt()
        );
    }
}
