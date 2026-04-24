package com.medframe.clinical.infrastructure.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TriageRequest {
    
    @NotBlank(message = "appointmentId es requerido")
    private String appointmentId;
    
    @NotBlank(message = "Patient ID es requerido")
    private String patientId;
    
    @NotBlank(message = "Motif ID es requerido")
    private String motifId;
    
    @NotEmpty(message = "Debe seleccionar al menos un discriminador")
    private List<String> discriminatorIds;
}
