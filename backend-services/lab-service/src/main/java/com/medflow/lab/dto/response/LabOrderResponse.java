package com.medflow.lab.dto.response;

import com.medflow.lab.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabOrderResponse {

    private String id;
    private String orderCode;
    private String patientId;
    private String doctorId;
    private List<String> testNames;
    private OrderStatus status;
    private LocalDateTime orderedAt;
    private LocalDateTime updatedAt;
    private String sampleId;
}
