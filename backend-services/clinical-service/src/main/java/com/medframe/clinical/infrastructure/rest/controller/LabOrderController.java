package com.medframe.clinical.infrastructure.rest.controller;

import com.medframe.clinical.domain.model.LabOrder;
import com.medframe.clinical.domain.port.in.GenerateLabOrderUseCase;
import com.medframe.clinical.infrastructure.rest.dto.request.LabOrderRequest;
import com.medframe.clinical.infrastructure.rest.dto.response.LabOrderResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clinical/lab-orders")
@Validated
@RequiredArgsConstructor
public class LabOrderController {
    
    private final GenerateLabOrderUseCase generateLabOrderUseCase;
    
    @PostMapping
    public ResponseEntity<LabOrderResponse> generateLabOrder(
            @Valid @RequestBody LabOrderRequest request,
            @RequestHeader("X-User-Id") String userId) {
        
        LabOrder labOrder = generateLabOrderUseCase.generateLabOrder(
            request.getConsultationId(),
            request.getPatientId(),
            userId,
            request.getAppointmentId(),
            request.getTestNames()
        );
        
        LabOrderResponse response = mapToResponse(labOrder);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    private LabOrderResponse mapToResponse(LabOrder labOrder) {
        return new LabOrderResponse(
            labOrder.getId(),
            labOrder.getOrderCode(),
            labOrder.getPatientId(),
            labOrder.getDoctorId(),
            labOrder.getTestNames(),
            labOrder.getStatus().name(),
            labOrder.getOrderedAt()
        );
    }
}
