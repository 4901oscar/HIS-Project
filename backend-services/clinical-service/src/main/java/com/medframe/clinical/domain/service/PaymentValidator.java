package com.medframe.clinical.domain.service;

import com.medframe.clinical.infrastructure.client.BillingServiceClient;
import com.medframe.clinical.infrastructure.client.BillingServiceException;
import com.medframe.clinical.infrastructure.client.BillingServiceTimeoutException;
import com.medframe.clinical.infrastructure.client.dto.InvoiceResponse;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Validador de pago para activación de citas médicas.
 * 
 * <p>Este componente valida que una cita tenga su factura pagada antes de permitir
 * su activación. Implementa las siguientes reglas de negocio:</p>
 * 
 * <ul>
 *   <li>Solo citas con facturas en estado PAID pueden activarse</li>
 *   <li>Citas con invoice_id = NULL pueden activarse (caso de compensación)</li>
 *   <li>Facturas en estado PENDING o CANCELLED bloquean la activación</li>
 *   <li>La validación puede deshabilitarse mediante configuración</li>
 * </ul>
 * 
 * <p><strong>Requisitos relacionados:</strong></p>
 * <ul>
 *   <li>REQ-1.1-1.8: Validación de estado de pago al activar cita</li>
 *   <li>REQ-2.1-2.3: Manejo de citas sin factura (compensation case)</li>
 *   <li>REQ-3.1-3.5: Resiliencia ante fallos de Billing Service</li>
 *   <li>REQ-6.1-6.6: Validación de estados de factura</li>
 * </ul>
 * 
 * @author MedFlow Development Team
 * @version 1.0
 * @since 2025-01-20
 */
@Component
@Slf4j
public class PaymentValidator {
    
    private final BillingServiceClient billingServiceClient;
    private final PaymentValidationConfig config;
    private final MeterRegistry meterRegistry;
    
    // Micrometer metrics
    // REQ-2.6, REQ-3.8, REQ-6.7, NFR-16
    private final Counter validationSuccessCounter;
    private final Counter activationsWithoutInvoiceCounter;
    
    public PaymentValidator(
            BillingServiceClient billingServiceClient,
            PaymentValidationConfig config,
            MeterRegistry meterRegistry) {
        this.billingServiceClient = billingServiceClient;
        this.config = config;
        this.meterRegistry = meterRegistry;
        
        // Initialize metrics counters
        this.validationSuccessCounter = Counter.builder("payment_validation_results_total")
                .description("Total number of payment validation results")
                .tag("status", "success")
                .register(meterRegistry);
        
        this.activationsWithoutInvoiceCounter = Counter.builder("appointments_activated_without_invoice_total")
                .description("Total number of appointments activated without invoice (compensation case)")
                .register(meterRegistry);
    }
    
    /**
     * Valida el pago de una cita antes de permitir su activación.
     * 
     * <p>Este método implementa la lógica de validación de pago con las siguientes reglas:</p>
     * <ol>
     *   <li>Si invoice_id es NULL → Permitir activación con advertencia (caso compensación)</li>
     *   <li>Si validación está deshabilitada → Permitir activación con advertencia</li>
     *   <li>Si factura está PAID → Permitir activación</li>
     *   <li>Si factura está PENDING → Rechazar activación</li>
     *   <li>Si factura está CANCELLED → Rechazar activación</li>
     *   <li>Si Billing Service falla → Rechazar activación</li>
     * </ol>
     * 
     * @param invoiceId ID de la factura a validar (puede ser null)
     * @param appointmentId ID de la cita (para logging)
     * @return PaymentValidationResult con el resultado de la validación
     */
    public PaymentValidationResult validatePayment(String invoiceId, String appointmentId) {
        // Caso 1: No hay factura (caso de compensación) - permitir activación
        if (invoiceId == null) {
            log.warn("Activating appointment {} without invoice validation (invoice_id = NULL)", 
                     appointmentId);
            activationsWithoutInvoiceCounter.increment();
            return PaymentValidationResult.allowedWithoutInvoice();
        }
        
        // Caso 2: Validación deshabilitada por configuración
        if (!config.isEnabled()) {
            log.warn("Payment validation is DISABLED. Allowing activation of appointment {} " +
                     "without checking invoice {}", appointmentId, invoiceId);
            return PaymentValidationResult.allowedWithoutValidation();
        }
        
        // Caso 3: Validar estado de la factura
        try {
            log.info("Validating payment for appointment {}, invoice {}", appointmentId, invoiceId);
            
            InvoiceResponse invoice = billingServiceClient.getInvoice(invoiceId);
            
            if (invoice == null) {
                log.error("Invoice {} not found for appointment {}", invoiceId, appointmentId);
                recordFailure("INVOICE_NOT_FOUND");
                return PaymentValidationResult.failed(
                    PaymentValidationError.INVOICE_NOT_FOUND,
                    "La factura asociada a esta cita no existe en el sistema."
                );
            }
            
            String status = invoice.getStatus();
            
            if ("PAID".equals(status)) {
                log.info("Payment validation SUCCESS for appointment {}, invoice {} is PAID", 
                         appointmentId, invoiceId);
                validationSuccessCounter.increment();
                return PaymentValidationResult.success(invoice);
            }
            
            if ("PENDING".equals(status)) {
                log.error("Payment validation FAILED for appointment {}, invoice {} is PENDING",
                          appointmentId, invoiceId);
                recordFailure("PAYMENT_PENDING");
                return PaymentValidationResult.failedWithInvoice(
                    PaymentValidationError.PAYMENT_PENDING,
                    "La cita no puede activarse. El paciente debe pagar en caja primero.",
                    invoice
                );
            }
            
            if ("CANCELLED".equals(status)) {
                log.error("Payment validation FAILED for appointment {}, invoice {} is CANCELLED", 
                          appointmentId, invoiceId);
                recordFailure("INVOICE_CANCELLED");
                return PaymentValidationResult.failed(
                    PaymentValidationError.INVOICE_CANCELLED,
                    "La cita no puede activarse. La factura ha sido cancelada."
                );
            }
            
            // Estado desconocido
            log.error("Unknown invoice status {} for appointment {}, invoice {}", 
                      status, appointmentId, invoiceId);
            recordFailure("UNKNOWN_STATUS");
            return PaymentValidationResult.failed(
                PaymentValidationError.UNKNOWN_STATUS,
                "Estado de factura desconocido: " + status
            );
            
        } catch (BillingServiceTimeoutException e) {
            log.error("Billing Service timeout while validating payment for appointment {}: {}", 
                      appointmentId, e.getMessage());
            recordFailure("SERVICE_TIMEOUT");
            return PaymentValidationResult.failed(
                PaymentValidationError.SERVICE_TIMEOUT,
                "El sistema de facturación no está disponible. Intente nuevamente en unos momentos."
            );
            
        } catch (BillingServiceException e) {
            log.error("Billing Service error while validating payment for appointment {}: {}", 
                      appointmentId, e.getMessage(), e);
            recordFailure("SERVICE_ERROR");
            return PaymentValidationResult.failed(
                PaymentValidationError.SERVICE_ERROR,
                "El sistema de facturación no está disponible. Intente nuevamente en unos momentos."
            );
        }
    }
    
    /**
     * Registra una falla de validación en las métricas.
     * 
     * @param reason Razón de la falla (PAYMENT_PENDING, INVOICE_CANCELLED, SERVICE_TIMEOUT, etc.)
     */
    private void recordFailure(String reason) {
        Counter.builder("payment_validation_failures_total")
                .description("Total number of payment validation failures by reason")
                .tag("reason", reason)
                .register(meterRegistry)
                .increment();
    }
}
