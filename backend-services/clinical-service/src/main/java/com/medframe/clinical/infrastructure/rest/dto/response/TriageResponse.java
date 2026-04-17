package com.medframe.clinical.infrastructure.rest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TriageResponse {
    
    private String id;
    private String patientId;
    private String priorityLevel;
    private String priorityDescription;
    private Integer maxWaitTimeMinutes;
    private LocalDateTime performedAt;
}
