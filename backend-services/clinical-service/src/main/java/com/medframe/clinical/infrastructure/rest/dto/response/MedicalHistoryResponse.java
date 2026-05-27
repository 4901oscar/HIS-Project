package com.medframe.clinical.infrastructure.rest.dto.response;

import com.medframe.clinical.domain.model.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicalHistoryResponse {
    
    private Object patient;
    private List<Consultation> consultations;
    private List<VitalSigns> vitalSigns;
    private List<Prescription> prescriptions;
    private List<LabOrder> labOrders;
}
