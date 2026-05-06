package com.medframe.clinical.infrastructure.client;

import com.medframe.clinical.infrastructure.client.dto.CreateInvoiceRequest;
import com.medframe.clinical.infrastructure.client.dto.InvoiceResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * Cliente HTTP para comunicación con el Billing Service.
 * 
 * <p>Este componente encapsula la lógica de comunicación REST con el Billing Service,
 * incluyendo patrones de resiliencia (circuit breaker, retry) para garantizar
 * disponibilidad del Clinical Service incluso cuando Billing Service falla.</p>
 * 
 * <p><strong>Patrones de resiliencia implementados:</strong></p>
 * <ul>
 *   <li><strong>Circuit Breaker:</strong> Previene llamadas repetidas a servicio fallido</li>
 *   <li><strong>Retry:</strong> Reintenta llamadas fallidas con backoff exponencial</li>
 *   <li><strong>Fallback:</strong> Retorna null cuando todos los reintentos fallan</li>
 * </ul>
 * 
 * <p><strong>Métricas expuestas:</strong></p>
 * <ul>
 *   <li><strong>billing_service_calls_total:</strong> Contador de llamadas por status (success, failure, timeout)</li>
 *   <li><strong>appointments_without_invoice_total:</strong> Contador de citas sin factura</li>
 *   <li><strong>billing_service_call_duration_seconds:</strong> Timer con percentiles de duración</li>
 * </ul>
 * 
 * <p><strong>Requisitos relacionados:</strong></p>
 * <ul>
 *   <li>REQ-1.1: Llamada REST síncrona a POST /api/billing/invoices</li>
 *   <li>REQ-1.2: Incluir patientId, appointmentId, charges en request</li>
 *   <li>REQ-3.1: Circuit breaker para prevenir cascading failures</li>
 *   <li>REQ-8.7: Header X-User-Id para auditoría</li>
 *   <li>REQ-8.8: Content-Type application/json</li>
 *   <li>REQ-9.1, REQ-9.3, REQ-9.4: Métricas de observabilidad</li>
 * </ul>
 * 
 * @author MedFlow Development Team
 * @version 1.0
 * @since 2024-04-24
 */
@Component
@Slf4j
public class BillingServiceClient {

    private final RestTemplate restTemplate;
    private final String billingServiceUrl;
    private final MeterRegistry meterRegistry;
    
    // Métricas
    private final Counter successCounter;
    private final Counter failureCounter;
    private final Counter timeoutCounter;
    private final Counter appointmentsWithoutInvoiceCounter;
    private final Timer callDurationTimer;

    /**
     * Constructor con inyección de dependencias.
     * 
     * @param restTemplate RestTemplate configurado con timeouts
     * @param billingServiceUrl URL base del Billing Service (desde application.yml)
     * @param meterRegistry Registro de métricas de Micrometer
     */
    public BillingServiceClient(
            RestTemplate restTemplate,
            @Value("${billing.service.url}") String billingServiceUrl,
            MeterRegistry meterRegistry) {
        this.restTemplate = restTemplate;
        this.billingServiceUrl = billingServiceUrl;
        this.meterRegistry = meterRegistry;
        
        // Inicializar métricas
        // REQ-9.1: Contador de llamadas por status
        this.successCounter = Counter.builder("billing_service_calls_total")
                .tag("status", "success")
                .description("Total de llamadas exitosas al Billing Service")
                .register(meterRegistry);
        
        this.failureCounter = Counter.builder("billing_service_calls_total")
                .tag("status", "failure")
                .description("Total de llamadas fallidas al Billing Service")
                .register(meterRegistry);
        
        this.timeoutCounter = Counter.builder("billing_service_calls_total")
                .tag("status", "timeout")
                .description("Total de timeouts al llamar al Billing Service")
                .register(meterRegistry);
        
        // REQ-9.3: Contador de citas sin factura
        this.appointmentsWithoutInvoiceCounter = Counter.builder("appointments_without_invoice_total")
                .description("Total de citas creadas sin factura (Billing Service no disponible)")
                .register(meterRegistry);
        
        // REQ-9.4: Timer de duración de llamadas
        this.callDurationTimer = Timer.builder("billing_service_call_duration_seconds")
                .description("Duración de llamadas al Billing Service")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);
    }

    /**
     * Crea una factura en el Billing Service.
     * 
     * <p>Este método realiza una llamada HTTP POST síncrona al endpoint
     * {@code POST /api/billing/invoices} del Billing Service. Incluye
     * circuit breaker y retry para manejar fallos transitorios.</p>
     * 
     * <p><strong>Comportamiento de resiliencia:</strong></p>
     * <ul>
     *   <li>Reintenta hasta 3 veces con backoff exponencial (1s, 2s, 4s)</li>
     *   <li>Si circuit breaker está OPEN, falla rápido sin llamar al servicio</li>
     *   <li>Si todos los reintentos fallan, ejecuta fallback (retorna null)</li>
     * </ul>
     * 
     * <p><strong>Headers incluidos:</strong></p>
     * <ul>
     *   <li><code>X-User-Id</code>: ID del usuario que crea la cita (para auditoría)</li>
     *   <li><code>Content-Type</code>: application/json</li>
     * </ul>
     * 
     * @param request Datos de la factura a crear (patientId, appointmentId, charges)
     * @param userId ID del usuario que crea la cita (para auditoría)
     * @return InvoiceResponse con datos de la factura creada, o null si falla
     * @throws BillingServiceException Si ocurre un error de comunicación
     */
    @CircuitBreaker(name = "billingService", fallbackMethod = "createInvoiceFallback")
    @Retry(name = "billingService")
    public InvoiceResponse createInvoice(CreateInvoiceRequest request, String userId) {
        // Medir duración de la llamada
        return callDurationTimer.record(() -> {
            try {
                log.debug("Llamando a Billing Service para crear factura. AppointmentId: {}, PatientId: {}",
                        request.getAppointmentId(), request.getPatientId());

                // Construir headers
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("X-User-Id", userId);

                // Construir request entity
                HttpEntity<CreateInvoiceRequest> requestEntity = new HttpEntity<>(request, headers);

                // Realizar llamada HTTP POST
                String url = billingServiceUrl + "/api/billing/invoices";
                ResponseEntity<InvoiceResponse> response = restTemplate.exchange(
                        url,
                        HttpMethod.POST,
                        requestEntity,
                        InvoiceResponse.class
                );

                InvoiceResponse invoiceResponse = response.getBody();
                
                if (invoiceResponse != null) {
                    // Incrementar contador de éxito
                    successCounter.increment();
                    
                    log.info("Factura creada exitosamente en Billing Service. InvoiceId: {}, InvoiceNumber: {}",
                            invoiceResponse.getId(), invoiceResponse.getInvoiceNumber());
                }

                return invoiceResponse;

            } catch (ResourceAccessException e) {
                // Timeouts o errores de red
                timeoutCounter.increment();
                log.error("Error de red o timeout al llamar a Billing Service", e);
                throw new BillingServiceException("Error de red o timeout al comunicarse con Billing Service", e);

            } catch (HttpServerErrorException e) {
                // Errores 5xx del Billing Service
                failureCounter.increment();
                log.error("Error HTTP 5xx al llamar a Billing Service. Status: {}, Body: {}",
                        e.getStatusCode(), e.getResponseBodyAsString(), e);
                throw new BillingServiceException("Billing Service retornó error 5xx: " + e.getStatusCode(), e);

            } catch (Exception e) {
                // Otros errores inesperados
                failureCounter.increment();
                log.error("Error inesperado al llamar a Billing Service", e);
                throw new BillingServiceException("Error inesperado al comunicarse con Billing Service", e);
            }
        });
    }

    /**
     * Método fallback ejecutado cuando circuit breaker está abierto o reintentos se agotan.
     * 
     * <p>Este método es invocado automáticamente por Resilience4j cuando:</p>
     * <ul>
     *   <li>El circuit breaker está en estado OPEN (demasiados fallos recientes)</li>
     *   <li>Todos los reintentos (3 intentos) se agotaron sin éxito</li>
     *   <li>Ocurre una excepción no recuperable</li>
     * </ul>
     * 
     * <p><strong>Comportamiento:</strong></p>
     * <ul>
     *   <li>Retorna {@code null} para indicar que la factura no pudo crearse</li>
     *   <li>Registra log de nivel ERROR con detalles del fallo</li>
     *   <li>Permite que el Clinical Service continúe creando la cita sin factura</li>
     * </ul>
     * 
     * <p><strong>Requisitos relacionados:</strong></p>
     * <ul>
     *   <li>REQ-2.1: Clinical Service debe continuar funcionando si Billing Service falla</li>
     *   <li>REQ-2.2: Cita se crea con invoiceId NULL si billing falla</li>
     *   <li>REQ-3.3: Fallback debe ejecutarse cuando circuit breaker está abierto</li>
     * </ul>
     * 
     * @param request Request original (no utilizado en fallback)
     * @param userId User ID original (no utilizado en fallback)
     * @param throwable Excepción que causó el fallback
     * @return null para indicar que la factura no pudo crearse
     */
    /**
     * Método fallback ejecutado cuando circuit breaker está abierto o reintentos se agotan.
     * 
     * <p>Este método es invocado automáticamente por Resilience4j cuando:</p>
     * <ul>
     *   <li>El circuit breaker está en estado OPEN (demasiados fallos recientes)</li>
     *   <li>Todos los reintentos (3 intentos) se agotaron sin éxito</li>
     *   <li>Ocurre una excepción no recuperable</li>
     * </ul>
     * 
     * <p><strong>Comportamiento:</strong></p>
     * <ul>
     *   <li>Retorna {@code null} para indicar que la factura no pudo crearse</li>
     *   <li>Registra log de nivel ERROR con detalles del fallo</li>
     *   <li>Permite que el Clinical Service continúe creando la cita sin factura</li>
     * </ul>
     * 
     * <p><strong>Requisitos relacionados:</strong></p>
     * <ul>
     *   <li>REQ-2.1: Clinical Service debe continuar funcionando si Billing Service falla</li>
     *   <li>REQ-2.2: Cita se crea con invoiceId NULL si billing falla</li>
     *   <li>REQ-3.3: Fallback debe ejecutarse cuando circuit breaker está abierto</li>
     * </ul>
     * 
     * @param request Request original (no utilizado en fallback)
     * @param userId User ID original (no utilizado en fallback)
     * @param throwable Excepción que causó el fallback
     * @return null para indicar que la factura no pudo crearse
     */
    private InvoiceResponse createInvoiceFallback(
            CreateInvoiceRequest request,
            String userId,
            Throwable throwable) {
        
        // Incrementar contador de citas sin factura (null-safe)
        if (appointmentsWithoutInvoiceCounter != null) {
            appointmentsWithoutInvoiceCounter.increment();
        }
        
        log.error("Fallback ejecutado para createInvoice. Billing Service no disponible. " +
                        "AppointmentId: {}, PatientId: {}, Error: {}",
                request.getAppointmentId(),
                request.getPatientId(),
                throwable.getMessage(),
                throwable);

        // Retornar null indica que la factura no pudo crearse
        // El AppointmentController manejará este caso creando la cita sin invoiceId
        return null;
    }
    
    /**
     * Obtiene una factura del Billing Service por su ID.
     * 
     * <p>Este método se utiliza durante la reconciliación manual para validar
     * que la factura existe y obtener sus datos (especialmente el patientId).</p>
     * 
     * <p><strong>Requisitos relacionados:</strong></p>
     * <ul>
     *   <li>REQ-12.3: Validar que factura existe</li>
     *   <li>REQ-12.4: Validar que patientId coincide</li>
     * </ul>
     * 
     * @param invoiceId ID de la factura a obtener
     * @return InvoiceResponse con los datos de la factura, o null si no existe
     */
    /**
     * Obtiene una factura por su ID desde el Billing Service.
     * 
     * <p>Este método implementa patrones de resiliencia:</p>
     * <ul>
     *   <li><strong>Circuit Breaker:</strong> Se abre después de 5 fallos consecutivos</li>
     *   <li><strong>Retry:</strong> 2 intentos con backoff exponencial (500ms, 1s)</li>
     *   <li><strong>Timeout:</strong> 3 segundos por request</li>
     * </ul>
     * 
     * <p><strong>Métricas:</strong></p>
     * <ul>
     *   <li><strong>billing_service_get_invoice_duration_seconds:</strong> Timer con percentiles de duración</li>
     * </ul>
     * 
     * @param invoiceId ID de la factura a obtener
     * @return InvoiceResponse con los detalles de la factura, o null si no se encuentra
     * @throws BillingServiceException si la llamada falla después de los reintentos
     * @throws BillingServiceTimeoutException si la llamada excede el timeout
     */
    @CircuitBreaker(name = "billingService", fallbackMethod = "getInvoiceFallback")
    @Retry(name = "billingServiceGet")
    public InvoiceResponse getInvoice(String invoiceId) {
        log.info("Calling Billing Service to get invoice: {}", invoiceId);
        
        // Medir duración de la llamada GET
        // REQ-3.8, NFR-16
        Timer getInvoiceTimer = Timer.builder("billing_service_get_invoice_duration_seconds")
                .description("Duración de llamadas GET al Billing Service para obtener facturas")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);
        
        return getInvoiceTimer.record(() -> {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Service-Name", "clinical-service");
            headers.set("Accept", "application/json");
            
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            try {
                ResponseEntity<InvoiceResponse> response = restTemplate.exchange(
                    billingServiceUrl + "/api/billing/invoices/" + invoiceId,
                    HttpMethod.GET,
                    entity,
                    InvoiceResponse.class
                );
                
                log.info("Invoice retrieved successfully: {}", invoiceId);
                return response.getBody();
                
            } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
                log.error("Invoice not found: {}", invoiceId);
                return null; // Signals invoice not found
                
            } catch (ResourceAccessException e) {
                log.error("Timeout calling Billing Service for invoice {}: {}", invoiceId, e.getMessage());
                throw new BillingServiceTimeoutException("Billing Service timeout", e);
                
            } catch (Exception e) {
                log.error("Error calling Billing Service for invoice {}: {}", invoiceId, e.getMessage(), e);
                throw new BillingServiceException("Failed to get invoice from Billing Service", e);
            }
        });
    }
    
    /**
     * Método fallback cuando el circuit breaker está abierto o todos los reintentos fallan.
     * Retorna null para señalar que la factura no pudo ser obtenida.
     * 
     * @param invoiceId ID de la factura
     * @param e Excepción que causó el fallback
     * @return null
     */
    private InvoiceResponse getInvoiceFallback(String invoiceId, Exception e) {
        log.error("Circuit breaker OPEN or retries exhausted for Billing Service. " +
                  "Invoice: {}, Error: {}", invoiceId, e.getMessage());
        throw new BillingServiceException("Billing Service unavailable", e);
    }
    
    /**
     * Actualiza el appointmentId de una factura en el Billing Service.
     * 
     * <p>Este método se utiliza durante la reconciliación manual para establecer
     * la referencia bidireccional entre la cita y la factura.</p>
     * 
     * <p><strong>Requisitos relacionados:</strong></p>
     * <ul>
     *   <li>REQ-12.5: Actualizar appointmentId en factura</li>
     * </ul>
     * 
     * @param invoiceId ID de la factura a actualizar
     * @param appointmentId ID de la cita a vincular
     * @param userId ID del usuario que realiza la operación
     * @return true si la actualización fue exitosa, false en caso contrario
     */
    public boolean updateInvoiceAppointmentId(String invoiceId, String appointmentId, String userId) {
        try {
            log.debug("Actualizando appointmentId en factura. InvoiceId: {}, AppointmentId: {}",
                    invoiceId, appointmentId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-User-Id", userId);
            
            java.util.Map<String, String> body = java.util.Collections.singletonMap("appointmentId", appointmentId);
            HttpEntity<java.util.Map<String, String>> requestEntity = new HttpEntity<>(body, headers);
            
            String url = billingServiceUrl + "/api/billing/invoices/" + invoiceId + "/appointment";
            restTemplate.exchange(url, HttpMethod.PATCH, requestEntity, Void.class);
            
            log.info("AppointmentId actualizado exitosamente en factura. InvoiceId: {}, AppointmentId: {}",
                    invoiceId, appointmentId);
            
            return true;
            
        } catch (Exception e) {
            log.error("Error al actualizar appointmentId en factura. InvoiceId: {}, AppointmentId: {}",
                    invoiceId, appointmentId, e);
            return false;
        }
    }
}
