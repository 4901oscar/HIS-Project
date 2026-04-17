package com.medframe.clinical.infrastructure.rest.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionRequest {
    
    @NotBlank(message = "Consultation ID es requerido")
    private String consultationId;
    
    @NotBlank(message = "Patient ID es requerido")
    private String patientId;
    
    @NotEmpty(message = "Debe incluir al menos un medicamento")
    @Valid
    private List<MedicationRequest> medications;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MedicationRequest {
        
        @NotBlank(message = "Nombre del medicamento es requerido")
        private String name;
        
        @NotBlank(message = "Dosis es requerida")
        private String dosage;
        
        @NotBlank(message = "Frecuencia es requerida")
        private String frequency;
        
        @NotNull(message = "Duración es requerida")
        @Positive(message = "Duración debe ser positiva")
        private Integer durationDays;
        
        @NotBlank(message = "Vía de administración es requerida")
        private String route;
        
        private String specialInstructions;
    }
}
