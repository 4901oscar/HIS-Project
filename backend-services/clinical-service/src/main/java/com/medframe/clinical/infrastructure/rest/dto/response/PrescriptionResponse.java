package com.medframe.clinical.infrastructure.rest.dto.response;

import com.medframe.clinical.domain.model.Prescription;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionResponse {
    
    private String id;
    private String prescriptionCode;
    private String patientId;
    private String doctorId;
    private List<Prescription.Medication> medications;
    private String status;
    private LocalDateTime issuedAt;
}
