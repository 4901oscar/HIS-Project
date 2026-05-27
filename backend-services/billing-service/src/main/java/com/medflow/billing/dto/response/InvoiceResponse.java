package com.medflow.billing.dto.response;

import com.medflow.billing.model.InvoiceStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for invoice information.
 * Contains complete invoice details including all charges.
 * 
 * <p><strong>Requisitos relacionados:</strong></p>
 * <ul>
 *   <li>REQ-7.6: Incluir appointmentId en respuesta de factura</li>
 *   <li>REQ-8.3: appointmentId es parte del contrato de respuesta</li>
 * </ul>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse {
    
    private String id;
    private String invoiceNumber;
    private String patientId;
    
    /**
     * ID de la cita médica asociada a esta factura (opcional).
     * 
     * <p>Este campo permite al Clinical Service confirmar que la factura
     * fue creada correctamente y está vinculada a la cita correspondiente.</p>
     * 
     * <p>Será null para facturas creadas manualmente desde el módulo de caja
     * que no están asociadas a una cita médica.</p>
     */
    private String appointmentId;
    
    private List<ChargeResponse> charges;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal total;
    private InvoiceStatus status;
    private LocalDateTime createdAt;
    private String createdBy;
    private String customerNit;
    private String customerName;
    private LocalDateTime updatedAt;
}
