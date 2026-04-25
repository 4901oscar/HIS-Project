package com.medframe.clinical.infrastructure.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an existing doctor.
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDoctorRequest {
    
    @NotBlank(message = "El nombre del doctor es obligatorio")
    private String name;
    
    @NotBlank(message = "La especialidad es obligatoria")
    private String specialty;
    
    @NotNull(message = "La hora de inicio del turno es obligatoria")
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "La hora de inicio debe estar en formato HH:mm (ej: 08:00)")
    private String shiftStart;
    
    @NotNull(message = "La hora de fin del turno es obligatoria")
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "La hora de fin debe estar en formato HH:mm (ej: 16:00)")
    private String shiftEnd;
}
