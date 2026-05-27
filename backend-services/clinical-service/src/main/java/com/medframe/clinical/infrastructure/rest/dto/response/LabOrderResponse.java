package com.medframe.clinical.infrastructure.rest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LabOrderResponse {
    
    private String id;
    private String orderCode;
    private String patientId;
    private String doctorId;
    private List<String> testNames;
    private String status;
    private LocalDateTime orderedAt;
}
