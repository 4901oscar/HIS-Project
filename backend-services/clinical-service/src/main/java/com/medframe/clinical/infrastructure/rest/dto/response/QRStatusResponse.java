package com.medframe.clinical.infrastructure.rest.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Respuesta del endpoint GET /api/clinical/appointments/{id}/qr-status.
 * 
 * <p>Este DTO proporciona información completa sobre el estado de pago de una cita
 * cuando se escanea su código QR, permitiendo al personal de recepción tomar decisiones
 * informadas sobre si activar o no la cita.</p>
 * 
 * <p><strong>Requisitos relacionados:</strong></p>
 * <ul>
 *   <li>REQ-10.1-10.8: QR inteligente con estado de pago</li>
 * </ul>
 * 
 * @author MedFlow Development Team
 * @version 1.0
 * @since 2025-01-20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QRStatusResponse {
    
    /**
     * ID de la cita.
     */
    private String appointmentId;
    
    /**
     * Nombre completo del paciente.
     */
    private String patientName;
    
    /**
     * Nombre del doctor asignado.
     */
    private String doctorName;
    
    /**
     * Fecha de la cita.
     */
    private LocalDate appointmentDate;
    
    /**
     * Hora de la cita.
     */
    private LocalTime appointmentTime;
    
    /**
     * Estado actual de la cita (SCHEDULED, ACTIVE, etc.).
     */
    private String appointmentStatus;
    
    /**
     * Estado de pago de la factura.
     * Valores posibles: PAID, PENDING, CANCELLED, NO_INVOICE, ERROR
     */
    private String paymentStatus;
    
    /**
     * Mensaje descriptivo en español sobre el estado de pago.
     * Ejemplos:
     * - "Cita pagada - Puede activarse"
     * - "Cita no pagada - Debe pagar en caja primero"
     * - "Factura cancelada - Contacte administración"
     * - "Cita sin factura - Activar bajo responsabilidad"
     */
    private String message;
    
    /**
     * Indica si la cita puede ser activada según el estado de pago.
     * true: La cita puede activarse
     * false: La cita NO puede activarse (debe pagar primero)
     */
    private Boolean canActivate;
    
    /**
     * Número de factura asociado a la cita (si existe).
     */
    private String invoiceNumber;
    
    /**
     * Mensaje de advertencia (opcional).
     * Se usa para casos especiales como citas sin factura.
     */
    private String warning;
}
