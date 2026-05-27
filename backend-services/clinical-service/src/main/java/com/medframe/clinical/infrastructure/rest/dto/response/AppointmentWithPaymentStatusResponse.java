package com.medframe.clinical.infrastructure.rest.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Respuesta extendida de cita con información de estado de pago.
 * 
 * <p>Este DTO se utiliza en el endpoint GET /api/clinical/appointments/today
 * para proporcionar al personal de recepción información visual sobre el estado
 * de pago de cada cita, facilitando la toma de decisiones sobre activación.</p>
 * 
 * <p><strong>Requisitos relacionados:</strong></p>
 * <ul>
 *   <li>REQ-11.1-11.8: Reception UI con indicadores visuales de estado de pago</li>
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
public class AppointmentWithPaymentStatusResponse {
    
    // ========== Campos básicos de la cita ==========
    
    /**
     * ID de la cita.
     */
    private String id;
    
    /**
     * Nombre completo del paciente.
     */
    private String patientName;
    
    /**
     * DPI del paciente.
     */
    private String patientDpi;
    
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
    private String status;
    
    // ========== Campos de estado de pago ==========
    
    /**
     * Estado de pago de la factura.
     * Valores posibles: PAID, PENDING, CANCELLED, NO_INVOICE, ERROR
     */
    private String paymentStatus;
    
    /**
     * Etiqueta descriptiva del estado de pago en español.
     * Ejemplos: "PAGADA", "PENDIENTE", "CANCELADA", "SIN FACTURA"
     */
    private String paymentStatusLabel;
    
    /**
     * Color para indicador visual en la UI.
     * Valores: "green" (PAID), "orange" (PENDING), "red" (CANCELLED/ERROR), "gray" (NO_INVOICE)
     */
    private String paymentStatusColor;
    
    /**
     * Indica si el botón de activación debe estar habilitado.
     * true: Botón habilitado (puede activarse)
     * false: Botón deshabilitado (no puede activarse)
     */
    private Boolean canActivate;
    
    /**
     * Tooltip para mostrar al pasar el mouse sobre el botón de activación.
     * Proporciona contexto sobre por qué el botón está habilitado o deshabilitado.
     * Ejemplos:
     * - "Cita pagada - Puede activarse"
     * - "El paciente debe pagar en caja primero"
     * - "Factura cancelada - Contacte administración"
     * - "Cita sin factura - Activar bajo responsabilidad"
     */
    private String activateButtonTooltip;
    
    /**
     * Número de factura asociado a la cita (si existe).
     */
    private String invoiceNumber;
}
