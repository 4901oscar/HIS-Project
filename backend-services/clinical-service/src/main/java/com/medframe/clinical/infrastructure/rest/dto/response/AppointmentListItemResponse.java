package com.medframe.clinical.infrastructure.rest.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * DTO unificado para listado de citas en todos los portales del sistema.
 * 
 * <p>Este DTO proporciona una estructura de datos consistente para todos los endpoints
 * de listado de citas, eliminando la necesidad de múltiples DTOs específicos por portal.
 * Cada portal puede usar los campos que necesite e ignorar el resto.</p>
 * 
 * <p><strong>Portales que usan este DTO:</strong></p>
 * <ul>
 *   <li>Admisión: patient, doctor, appointmentDate/Time, payment</li>
 *   <li>Caja: patient, payment.amount, payment.status</li>
 *   <li>Triaje: patient, doctor, clinical.hasVitalSigns, clinical.manchesterLevel</li>
 *   <li>Laboratorio: patient, doctor, clinical.hasLabOrders</li>
 *   <li>Farmacia: patient, doctor, clinical.hasPrescriptions</li>
 *   <li>Doctor: patient, appointmentDate/Time, clinical</li>
 *   <li>Paciente: doctor, appointmentDate/Time, qr, payment</li>
 * </ul>
 * 
 * @author MedFlow Development Team
 * @version 1.0
 * @since 2026-04-27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppointmentListItemResponse {
    
    // ═══════════════════════════════════════════════════════════
    // INFORMACIÓN BÁSICA (siempre presente)
    // ═══════════════════════════════════════════════════════════
    
    private String id;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private String status;
    private String statusLabel;
    private String statusColor;
    private String notes;
    private LocalDateTime createdAt;
    private String prescriptionCode;
    
    // ═══════════════════════════════════════════════════════════
    // INFORMACIÓN DEL PACIENTE (siempre presente)
    // ═══════════════════════════════════════════════════════════
    
    private PatientInfo patient;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PatientInfo {
        private String id;
        private String fullName;
        private String dpi;
        private String phone;
        private String email;
    }
    
    // ═══════════════════════════════════════════════════════════
    // INFORMACIÓN DEL DOCTOR (siempre presente)
    // ═══════════════════════════════════════════════════════════
    
    private DoctorInfo doctor;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DoctorInfo {
        private String id;
        private String name;
    }
    
    // ═══════════════════════════════════════════════════════════
    // INFORMACIÓN DE PAGO (siempre presente, puede tener valores null)
    // ═══════════════════════════════════════════════════════════
    
    private PaymentInfo payment;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PaymentInfo {
        private String invoiceId;
        private String invoiceNumber;
        private String status;              // PAID, PENDING, CANCELLED, NO_INVOICE, ERROR
        private String statusLabel;         // PAGADA, PENDIENTE, CANCELADA, SIN FACTURA
        private String statusColor;         // green, orange, red, gray
        private BigDecimal amount;
        private Boolean canActivate;
        private String tooltip;
    }
    
    // ═══════════════════════════════════════════════════════════
    // INFORMACIÓN CLÍNICA (opcional, según contexto)
    // ═══════════════════════════════════════════════════════════
    
    private ClinicalInfo clinical;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ClinicalInfo {
        private Boolean hasVitalSigns;
        private Boolean hasTriage;
        private String manchesterLevel;     // ROJO, NARANJA, AMARILLO, VERDE, AZUL
        private Boolean hasLabOrders;
        private Boolean hasPrescriptions;
        private Boolean hasConsultation;
    }
    
    // ═══════════════════════════════════════════════════════════
    // INFORMACIÓN DE QR (opcional)
    // ═══════════════════════════════════════════════════════════
    
    private QRInfo qr;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class QRInfo {
        private Boolean hasQR;
        private String qrCodeBase64;        // Solo si se solicita explícitamente
    }
    
    // ═══════════════════════════════════════════════════════════
    // METADATOS (útiles para UI)
    // ═══════════════════════════════════════════════════════════
    
    private MetadataInfo metadata;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MetadataInfo {
        private Boolean isToday;
        private Boolean isPast;
        private Boolean isUpcoming;
        private Boolean canEdit;
        private Boolean canCancel;
        private Boolean canActivate;
    }
}
