package com.medframe.clinical.infrastructure.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO para actualizar el invoiceId de una cita durante reconciliación manual.
 * 
 * <p>Este DTO se utiliza cuando el Billing Service no estaba disponible durante
 * la creación de la cita y se necesita vincular manualmente una factura existente.</p>
 * 
 * <p><strong>Requisitos relacionados:</strong></p>
 * <ul>
 *   <li>REQ-12.2: Actualizar invoiceId de cita manualmente</li>
 * </ul>
 * 
 * @author MedFlow Development Team
 * @version 1.0
 * @since 2024-04-24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateInvoiceIdRequest {
    
    /**
     * ID de la factura a vincular con la cita.
     * Debe ser un UUID válido de una factura existente en el Billing Service.
     */
    @NotBlank(message = "El ID de la factura es obligatorio")
    private String invoiceId;
}
