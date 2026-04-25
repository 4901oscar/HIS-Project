package com.medframe.clinical.infrastructure.client;

import com.medframe.clinical.infrastructure.client.dto.ChargeRequest;
import com.medframe.clinical.infrastructure.client.dto.CreateInvoiceRequest;
import com.medframe.clinical.infrastructure.client.dto.InvoiceResponse;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests para BillingServiceClient.
 * Verifica comportamiento HTTP con RestTemplate mockeado.
 *
 * Tasks 13.1-13.3 - Requirements: REQ-1.1, REQ-1.2, REQ-2.1, REQ-3.1, REQ-8.7, REQ-8.8
 *
 * Nota: @CircuitBreaker y @Retry son AOP-based. En estos tests se valida la lógica
 * HTTP del cliente directamente. Para tests del circuit breaker completo se requiere
 * Spring AOP context con @SpringBootTest.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BillingServiceClient - HTTP Integration Tests")
class BillingServiceClientTest {

    @Mock private RestTemplate restTemplate;

    private BillingServiceClient billingServiceClient;
    private SimpleMeterRegistry meterRegistry;

    private static final String BILLING_URL = "http://localhost:8086";

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        billingServiceClient = new BillingServiceClient(restTemplate, BILLING_URL, meterRegistry);
    }

    // ========== Task 13.1: End-to-end flow - Billing Service exitoso ==========

    @Test
    @DisplayName("REQ-1.1: createInvoice exitoso debe retornar InvoiceResponse con datos correctos")
    void createInvoice_withSuccessfulResponse_shouldReturnInvoiceResponse() {
        // Arrange
        InvoiceResponse expectedResponse = buildInvoiceResponse("invoice-id-123", "INV-20260424-0001");
        ResponseEntity<InvoiceResponse> httpResponse = ResponseEntity.status(HttpStatus.CREATED).body(expectedResponse);

        when(restTemplate.exchange(
            eq(BILLING_URL + "/api/billing/invoices"),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(InvoiceResponse.class)
        )).thenReturn(httpResponse);

        CreateInvoiceRequest request = buildCreateRequest("patient-456", "appt-123");

        // Act
        InvoiceResponse result = billingServiceClient.createInvoice(request, "user-123");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("invoice-id-123");
        assertThat(result.getInvoiceNumber()).isEqualTo("INV-20260424-0001");
    }

    @Test
    @DisplayName("REQ-8.7, REQ-8.8: Request debe incluir headers X-User-Id y Content-Type")
    void createInvoice_shouldIncludeRequiredHeaders() {
        // Arrange
        InvoiceResponse expectedResponse = buildInvoiceResponse("invoice-id-123", "INV-20260424-0001");
        ResponseEntity<InvoiceResponse> httpResponse = ResponseEntity.ok(expectedResponse);
        ArgumentCaptor<HttpEntity<CreateInvoiceRequest>> entityCaptor =
            ArgumentCaptor.forClass(HttpEntity.class);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), entityCaptor.capture(), eq(InvoiceResponse.class)))
            .thenReturn(httpResponse);

        CreateInvoiceRequest request = buildCreateRequest("patient-456", "appt-123");

        // Act
        billingServiceClient.createInvoice(request, "user-abc");

        // Assert
        HttpEntity<CreateInvoiceRequest> capturedEntity = entityCaptor.getValue();
        assertThat(capturedEntity.getHeaders().getFirst("X-User-Id"))
            .as("Header X-User-Id debe estar presente para auditoría")
            .isEqualTo("user-abc");
        assertThat(capturedEntity.getHeaders().getContentType())
            .as("Content-Type debe ser application/json")
            .hasToString("application/json");
    }

    @Test
    @DisplayName("REQ-1.1: URL del endpoint debe ser /api/billing/invoices")
    void createInvoice_shouldCallCorrectEndpoint() {
        // Arrange
        InvoiceResponse expectedResponse = buildInvoiceResponse("inv-1", "INV-001");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(InvoiceResponse.class)))
            .thenReturn(ResponseEntity.ok(expectedResponse));
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        billingServiceClient.createInvoice(buildCreateRequest("patient-1", "appt-1"), "user-1");

        // Assert
        verify(restTemplate).exchange(urlCaptor.capture(), eq(HttpMethod.POST), any(), eq(InvoiceResponse.class));
        assertThat(urlCaptor.getValue()).isEqualTo(BILLING_URL + "/api/billing/invoices");
    }

    // ========== Task 13.2: End-to-end flow - Billing Service fallido ==========

    @Test
    @DisplayName("REQ-3.1: Error HTTP 5xx debe lanzar BillingServiceException")
    void createInvoice_with5xxServerError_shouldThrowBillingServiceException() {
        // Arrange
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(InvoiceResponse.class)))
            .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Service unavailable"));

        CreateInvoiceRequest request = buildCreateRequest("patient-456", "appt-123");

        // Act & Assert
        assertThatThrownBy(() -> billingServiceClient.createInvoice(request, "user-123"))
            .isInstanceOf(BillingServiceException.class)
            .hasMessageContaining("Billing Service retornó error 5xx");
    }

    @Test
    @DisplayName("REQ-3.1: Timeout de red debe lanzar BillingServiceException")
    void createInvoice_withNetworkTimeout_shouldThrowBillingServiceException() {
        // Arrange
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(InvoiceResponse.class)))
            .thenThrow(new ResourceAccessException("Connection timeout"));

        CreateInvoiceRequest request = buildCreateRequest("patient-456", "appt-123");

        // Act & Assert
        assertThatThrownBy(() -> billingServiceClient.createInvoice(request, "user-123"))
            .isInstanceOf(BillingServiceException.class)
            .hasMessageContaining("Error de red o timeout");
    }

    @Test
    @DisplayName("REQ-3.1: Error inesperado debe lanzar BillingServiceException")
    void createInvoice_withUnexpectedError_shouldThrowBillingServiceException() {
        // Arrange
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(InvoiceResponse.class)))
            .thenThrow(new IllegalStateException("Unexpected error"));

        CreateInvoiceRequest request = buildCreateRequest("patient-456", "appt-123");

        // Act & Assert
        assertThatThrownBy(() -> billingServiceClient.createInvoice(request, "user-123"))
            .isInstanceOf(BillingServiceException.class)
            .hasMessageContaining("Error inesperado");
    }

    // ========== Task 13.3: Métricas y observabilidad ==========

    @Test
    @DisplayName("REQ-9.1: Llamada exitosa debe incrementar contador billing_service_calls_total[success]")
    void createInvoice_onSuccess_shouldIncrementSuccessCounter() {
        // Arrange
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(InvoiceResponse.class)))
            .thenReturn(ResponseEntity.ok(buildInvoiceResponse("inv-1", "INV-001")));

        double successBefore = meterRegistry.counter("billing_service_calls_total", "status", "success").count();

        // Act
        billingServiceClient.createInvoice(buildCreateRequest("patient-1", "appt-1"), "user-1");

        // Assert
        double successAfter = meterRegistry.counter("billing_service_calls_total", "status", "success").count();
        assertThat(successAfter).isGreaterThan(successBefore);
    }

    @Test
    @DisplayName("REQ-9.1: Fallo HTTP 5xx debe incrementar contador billing_service_calls_total[failure]")
    void createInvoice_on5xxFailure_shouldIncrementFailureCounter() {
        // Arrange
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(InvoiceResponse.class)))
            .thenThrow(new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE));

        double failureBefore = meterRegistry.counter("billing_service_calls_total", "status", "failure").count();

        // Act
        try {
            billingServiceClient.createInvoice(buildCreateRequest("patient-1", "appt-1"), "user-1");
        } catch (BillingServiceException ignored) {}

        // Assert
        double failureAfter = meterRegistry.counter("billing_service_calls_total", "status", "failure").count();
        assertThat(failureAfter).isGreaterThan(failureBefore);
    }

    @Test
    @DisplayName("REQ-12.3, REQ-12.4: getInvoice debe retornar InvoiceResponse del Billing Service")
    void getInvoice_withValidId_shouldReturnInvoiceResponse() {
        // Arrange
        InvoiceResponse expected = buildInvoiceResponse("invoice-id-abc", "INV-20260424-0002");
        when(restTemplate.exchange(
            eq(BILLING_URL + "/api/billing/invoices/invoice-id-abc"),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(InvoiceResponse.class)
        )).thenReturn(ResponseEntity.ok(expected));

        // Act
        InvoiceResponse result = billingServiceClient.getInvoice("invoice-id-abc");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("invoice-id-abc");
    }

    @Test
    @DisplayName("REQ-12.3: getInvoice con error de red debe lanzar BillingServiceTimeoutException")
    void getInvoice_withNetworkError_shouldThrowBillingServiceTimeoutException() {
        // Arrange
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(InvoiceResponse.class)
        )).thenThrow(new ResourceAccessException("Connection refused"));

        // Act & Assert - debe lanzar excepción, no retornar null
        assertThatThrownBy(() -> billingServiceClient.getInvoice("invoice-no-existe"))
            .isInstanceOf(BillingServiceTimeoutException.class)
            .hasMessageContaining("Billing Service timeout");
    }

    // ========== Helpers ==========

    private InvoiceResponse buildInvoiceResponse(String id, String number) {
        InvoiceResponse r = new InvoiceResponse();
        r.setId(id);
        r.setInvoiceNumber(number);
        r.setPatientId("patient-456");
        r.setAppointmentId("appt-123");
        return r;
    }

    private CreateInvoiceRequest buildCreateRequest(String patientId, String appointmentId) {
        ChargeRequest charge = new ChargeRequest("CONSULTATION", "Consulta médica general", 1, new BigDecimal("150.00"));
        return new CreateInvoiceRequest(patientId, appointmentId, List.of(charge));
    }
}
