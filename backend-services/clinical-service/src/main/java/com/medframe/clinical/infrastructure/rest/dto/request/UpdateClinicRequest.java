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
    
    @Pattern(regexp = "^[0-9]+$", message = "El código debe contener solo caracteres numéricos")
    private String codigo;
    
    @Pattern(regexp = "^[a-zA-Z0-9\\s]+$", message = "El nombre debe contener solo caracteres alfanuméricos")
    private String nombre;
    
    @Pattern(regexp = "^[a-zA-Z0-9\\s]+$", message = "La descripción debe contener solo caracteres alfanuméricos")
    private String descripcion;
    
    @Pattern(regexp = "^(ACTIVE|INACTIVE|DELETED)$", message = "El estado debe ser ACTIVE, INACTIVE o DELETED")
    private String estado;
}
