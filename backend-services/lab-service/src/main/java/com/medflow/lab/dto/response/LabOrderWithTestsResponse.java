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
public class LabOrderWithTestsResponse {

    private String id;
    private String orderCode;
    private String patientId;
    private String doctorId;
    private String appointmentId;
    private List<TestDetail> tests;
    private OrderStatus status;
    private LocalDateTime orderedAt;
}
