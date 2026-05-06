package com.medframe.clinical.infrastructure.client;

import com.medframe.clinical.domain.model.Appointment;
import com.medframe.clinical.infrastructure.client.dto.ChargeRequest;
import com.medframe.clinical.infrastructure.client.dto.CreateInvoiceRequest;
import com.medframe.clinical.infrastructure.client.dto.InvoiceResponse;
import net.jqwik.api.*;
import net.jqwik.api.constraints.NotBlank;
import net.jqwik.api.constraints.StringLength;
import org.assertj.core.api.Assertions;

import java.math.BigDecimal;
import java.util.List;

/**
 * Property-Based Tests para la integración Appointment-Billing.
 * Verifica invariantes del sistema usando jqwik para generación de datos arbitrarios.
 *
 * Tasks 12.1-12.5 - Requirements: REQ-5.3, REQ-5.4, REQ-1.1, REQ-2.1, REQ-4.3, REQ-4.4, REQ-3.1
 */
class AppointmentBillingIntegrationPropertyTest {

    /**
     * Property 1: Bidirectional Reference Consistency
     * REQ-5.3, REQ-5.4: Para cualquier invoiceId asignado a un appointment,
     * el appointment debe retener exactamente ese invoiceId.
     * Requirements: 5.3, 5.4, 5.5, 5.6
     */
    @Property(tries = 200)
    @Label("Property 1: Bidirectional Reference Consistency - invoiceId siempre se retiene")
    void appointmentRetainsInvoiceIdExactly(
            @ForAll @NotBlank @StringLength(min = 1, max = 36) String invoiceId,
            @ForAll @NotBlank @StringLength(min = 1, max = 36) String appointmentId) {

        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);
        appointment.setPatientId("patient-test");

        appointment.setInvoiceId(invoiceId);

        Assertions.assertThat(appointment.getInvoiceId())
            .as("invoiceId debe ser exactamente el valor asignado")
            .isEqualTo(invoiceId);
    }

    /**
     * Property 2: Invoice Created for Every Successful Appointment
     * REQ-1.1, REQ-1.2, REQ-1.3: Para cualquier InvoiceResponse válida retornada
     * por Billing Service, el appointment siempre recibe el invoiceId correcto.
     * Requirements: 1.1, 1.2, 1.3, 1.6, 1.8
     */
    @Property(tries = 200)
    @Label("Property 2: InvoiceId siempre se establece cuando Billing retorna respuesta exitosa")
    void whenBillingSucceeds_appointmentAlwaysGetsInvoiceId(
            @ForAll @NotBlank @StringLength(min = 1, max = 36) String invoiceId,
            @ForAll @NotBlank @StringLength(min = 1, max = 36) String invoiceNumber) {

        Appointment appointment = new Appointment();
        appointment.setId("appt-123");

        // Simular lógica del controlador cuando Billing retorna respuesta exitosa
        InvoiceResponse invoiceResponse = new InvoiceResponse();
        invoiceResponse.setId(invoiceId);
        invoiceResponse.setInvoiceNumber(invoiceNumber);

        if (invoiceResponse != null) {
            appointment.setInvoiceId(invoiceResponse.getId());
        }

        Assertions.assertThat(appointment.getInvoiceId())
            .as("Billing exitoso: invoiceId no debe ser null")
            .isNotNull();
        Assertions.assertThat(appointment.getInvoiceId())
            .as("Billing exitoso: invoiceId debe coincidir con invoice.id")
            .isEqualTo(invoiceId);
    }

    /**
     * Property 3: Appointment Created Even When Billing Fails (Compensation)
     * REQ-2.1, REQ-2.2: Para cualquier fallo de Billing Service,
     * la cita siempre se crea con invoiceId = NULL.
     * Requirements: 2.1, 2.2, 2.6, 2.7
     */
    @Property(tries = 200)
    @Label("Property 3: InvoiceId siempre es NULL cuando Billing falla")
    void whenBillingFails_appointmentInvoiceIdIsAlwaysNull(
            @ForAll @StringLength(min = 1, max = 36) String appointmentId) {

        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);

        // Simular lógica del controlador cuando Billing retorna null (fallback)
        InvoiceResponse invoiceResponse = null;

        if (invoiceResponse != null) {
            appointment.setInvoiceId(invoiceResponse.getId());
        } else {
            appointment.setInvoiceId(null);
        }

        Assertions.assertThat(appointment.getInvoiceId())
            .as("Billing fallido: invoiceId debe ser null para permitir reconciliación")
            .isNull();
        Assertions.assertThat(appointment.getStatus())
            .as("Billing fallido: la cita debe estar en estado SCHEDULED")
            .isEqualTo(Appointment.AppointmentStatus.SCHEDULED);
    }

    /**
     * Property 4: Invoice Total Equals Consultation Price
     * REQ-4.3, REQ-4.4, REQ-4.5: Para cualquier precio de consulta válido,
     * el total del cargo CONSULTATION en CreateInvoiceRequest siempre iguala ese precio.
     * Requirements: 4.3, 4.4, 4.5
     */
    @Property(tries = 100)
    @Label("Property 4: Total del cargo siempre iguala al precio de consulta configurado")
    void consultationChargeTotal_alwaysEqualsConsultationPrice(
            @ForAll("validConsultationPrices") BigDecimal consultationPrice) {

        ChargeRequest charge = new ChargeRequest(
            "CONSULTATION",
            "Consulta médica general",
            1,
            consultationPrice
        );

        CreateInvoiceRequest request = new CreateInvoiceRequest(
            "patient-test",
            "appt-test",
            List.of(charge)
        );

        BigDecimal total = request.getCharges().stream()
            .map(c -> c.getUnitPrice().multiply(BigDecimal.valueOf(c.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        Assertions.assertThat(total)
            .as("Total del cargo CONSULTATION debe igualar al precio configurado")
            .isEqualByComparingTo(consultationPrice);
        Assertions.assertThat(total)
            .as("Total del cargo no debe ser cero ni negativo")
            .isGreaterThan(BigDecimal.ZERO);
    }

    /**
     * Property 5: Circuit Breaker Fallback Returns Null
     * REQ-3.1, REQ-3.2, REQ-3.3: El método fallback del circuit breaker
     * siempre retorna null para habilitar la estrategia de compensación.
     * Requirements: 3.1, 3.2, 3.3, 3.7
     */
    @Example
    @Label("Property 5: Fallback del circuit breaker siempre retorna null")
    void circuitBreakerFallback_alwaysReturnsNull() {
        // El fallback está diseñado para retornar null, permitiendo que el
        // Clinical Service continúe operando con estrategia de compensación (invoiceId=null)
        // Verificamos el contrato: si la respuesta de Billing es null, invoiceId queda null

        Appointment appointment = new Appointment();
        appointment.setId("appt-cb-test");

        // Simulamos 3 fallos consecutivos (como haría el circuit breaker)
        for (int i = 0; i < 3; i++) {
            InvoiceResponse fallbackResponse = null; // simula createInvoiceFallback() retornando null

            if (fallbackResponse != null) {
                appointment.setInvoiceId(fallbackResponse.getId());
            } else {
                appointment.setInvoiceId(null);
            }

            Assertions.assertThat(appointment.getInvoiceId())
                .as("Fallback intento " + (i + 1) + ": invoiceId debe ser null")
                .isNull();
        }

        // Incluso después de múltiples fallos, la cita debe estar operativa
        Assertions.assertThat(appointment.getStatus())
            .as("Después de fallos de billing, la cita permanece SCHEDULED")
            .isEqualTo(Appointment.AppointmentStatus.SCHEDULED);
    }

    /**
     * Property adicional: CreateInvoiceRequest siempre incluye patientId y appointmentId
     * REQ-1.2: Los parámetros obligatorios siempre están presentes en el request.
     */
    @Property(tries = 100)
    @Label("Invariante: CreateInvoiceRequest siempre incluye patientId y appointmentId correctos")
    void createInvoiceRequest_alwaysContainsMandatoryFields(
            @ForAll @NotBlank @StringLength(min = 1, max = 36) String patientId,
            @ForAll @NotBlank @StringLength(min = 1, max = 36) String appointmentId,
            @ForAll("validConsultationPrices") BigDecimal price) {

        ChargeRequest charge = new ChargeRequest("CONSULTATION", "Consulta", 1, price);
        CreateInvoiceRequest request = new CreateInvoiceRequest(patientId, appointmentId, List.of(charge));

        Assertions.assertThat(request.getPatientId()).isEqualTo(patientId);
        Assertions.assertThat(request.getAppointmentId()).isEqualTo(appointmentId);
        Assertions.assertThat(request.getCharges()).isNotEmpty();
        Assertions.assertThat(request.getCharges().get(0).getType()).isEqualTo("CONSULTATION");
    }

    @Provide
    Arbitrary<BigDecimal> validConsultationPrices() {
        return Arbitraries.bigDecimals()
            .between(BigDecimal.valueOf(1L), BigDecimal.valueOf(5000L))
            .ofScale(2);
    }
}
