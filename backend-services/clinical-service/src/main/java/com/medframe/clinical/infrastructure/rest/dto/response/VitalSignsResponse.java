package com.medframe.clinical.infrastructure.rest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VitalSignsResponse {
    
    private String id;
    private String patientId;
    private Integer systolicPressure;
    private Integer diastolicPressure;
    private Integer heartRate;
    private Integer respiratoryRate;
    private Double temperature;
    private Integer oxygenSaturation;
    private Double weight;
    private Double height;
    private Double bmi;
    private LocalDateTime recordedAt;
}
