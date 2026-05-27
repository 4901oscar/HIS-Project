package com.medframe.clinical.infrastructure.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request DTO for marking days off for a doctor.
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarkDaysOffRequest {
    
    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate startDate;
    
    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDate endDate;
    
    @NotBlank(message = "El motivo es obligatorio")
    private String reason;
}
