package com.medframe.clinical.infrastructure.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationRequest {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceCharge {
        private String name;
        private BigDecimal price;
    }

    @NotBlank(message = "Patient ID es requerido")
    private String patientId;

    private String appointmentId;

    @NotBlank(message = "Motivo de consulta es requerido")
    private String chiefComplaint;

    private String symptoms;

    private String primaryDiagnosis;

    private List<String> secondaryDiagnoses;

    private String medicalNotes;

    private String treatmentPlan;

    private boolean hasLabOrders;

    private boolean hasPrescription;

    private List<ServiceCharge> labCharges;

    private List<ServiceCharge> pharmacyCharges;
}
