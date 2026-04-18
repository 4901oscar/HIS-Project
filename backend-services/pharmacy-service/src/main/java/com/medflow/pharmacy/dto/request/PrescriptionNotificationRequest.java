package com.medflow.pharmacy.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para recibir notificación de nueva prescripción desde Clinical Service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionNotificationRequest {
    
    @NotBlank(message = "El código de prescripción es obligatorio")
    private String prescriptionCode;
    
    @NotBlank(message = "El ID del paciente es obligatorio")
    private String patientId;
    
    @NotBlank(message = "El ID del doctor es obligatorio")
    private String doctorId;
    
    @NotBlank(message = "Los medicamentos son obligatorios")
    private String medicationsJson;
    
    @NotNull(message = "La fecha de emisión es obligatoria")
    private LocalDateTime issuedAt;
}
