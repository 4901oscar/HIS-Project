package com.medflow.lab.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabResultResponse {

    private String id;
    private String orderId;
    private String patientId;
    private String resultFilePath;
    private LocalDateTime uploadedAt;
    private String uploadedBy;
}
