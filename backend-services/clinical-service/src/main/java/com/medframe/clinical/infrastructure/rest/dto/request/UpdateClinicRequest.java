package com.medframe.clinical.infrastructure.rest.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an existing clinic.
 * 
 * <p>All fields are optional. Only provided fields will be updated.
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateClinicRequest {
    
    @Pattern(regexp = "^[0-9]{1,6}$", message = "El código debe contener solo números, máximo 6 dígitos")
    private String codigo;

    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ0-9\\s\\-]{1,32}$", message = "El nombre debe contener solo letras, números y guiones, máximo 32 caracteres")
    private String nombre;

    @jakarta.validation.constraints.Size(max = 256, message = "La descripción no puede superar 256 caracteres")
    private String descripcion;
    
    @Pattern(regexp = "^(ACTIVE|INACTIVE|DELETED)$", message = "El estado debe ser ACTIVE, INACTIVE o DELETED")
    private String estado;
}
