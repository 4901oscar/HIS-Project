package com.medframe.clinical.infrastructure.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new clinic.
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateClinicRequest {
    
    @NotBlank(message = "El código de la clínica es obligatorio")
    @Pattern(regexp = "^[0-9]{1,6}$", message = "El código debe contener solo números, máximo 6 dígitos")
    private String codigo;

    @NotBlank(message = "El nombre de la clínica es obligatorio")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ0-9\\s\\-]{1,32}$", message = "El nombre debe contener solo letras, números y guiones, máximo 32 caracteres")
    private String nombre;

    @NotBlank(message = "La descripción de la clínica es obligatoria")
    @jakarta.validation.constraints.Size(max = 256, message = "La descripción no puede superar 256 caracteres")
    private String descripcion;
}
