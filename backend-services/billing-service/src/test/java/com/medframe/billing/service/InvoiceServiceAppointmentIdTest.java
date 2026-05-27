package com.medflow.billing.service;

import com.medflow.billing.dto.request.ChargeRequest;
import com.medflow.billing.dto.request.CreateInvoiceRequest;
import com.medflow.billing.dto.response.InvoiceResponse;
import com.medflow.billing.model.ChargeType;
import com.medflow.billing.model.Invoice;
import com.medflow.billing.repository.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests para la integración de appointmentId en InvoiceService.
 * Task 10.4 - Requirements: REQ-1.8, REQ-5.4, REQ-7.3, REQ-7.6, REQ-8.3
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("InvoiceService - AppointmentId Integration Tests")
class InvoiceServiceAppointmentIdTest {

    @Mock private InvoiceRepository invoiceRepository;
    @InjectMocks private InvoiceService invoiceService;

    private ChargeRequest consultationCharge;

    @BeforeEach
    void setUp() {
        consultationCharge = new ChargeRequest(
            ChargeType.CONSULTATION,
            "Consulta médica general",
            1,
            new BigDecimal("150.00")
        );
    }

    @Test
    @DisplayName("REQ-1.8, REQ-5.4: Crear factura con appointmentId debe persistirlo correctamente")
    void createInvoice_withAppointmentId_shouldSaveAndReturnAppointmentId() {
        String expectedAppointmentId = "appt-id-abc-123";
        CreateInvoiceRequest request = new CreateInvoiceRequest(
            "patient-456",
            expectedAppointmentId,
            List.of(consultationCharge)
        );

        when(invoiceRepository.findAll()).thenReturn(List.of());
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> {
            Invoice inv = invocation.getArgument(0);
            inv.setId("invoice-id-001");
            return inv;
        });

        ArgumentCaptor<Invoice> captor = ArgumentCaptor.forClass(Invoice.class);

        InvoiceResponse response = invoiceService.createInvoice(request, "user-123");

        verify(invoiceRepository).save(captor.capture());
        assertThat(captor.getValue().getAppointmentId()).isEqualTo(expectedAppointmentId);
        assertThat(response.getAppointmentId()).isEqualTo(expectedAppointmentId);
    }

    @Test
    @DisplayName("REQ-8.1: Crear factura sin appointmentId debe aceptar NULL (compatibilidad caja)")
    void createInvoice_withoutAppointmentId_shouldAcceptNullAppointmentId() {
        CreateInvoiceRequest request = new CreateInvoiceRequest(
            "patient-456",
            List.of(consultationCharge)
        );

        when(invoiceRepository.findAll()).thenReturn(List.of());
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> {
            Invoice inv = invocation.getArgument(0);
            inv.setId("invoice-id-002");
            return inv;
        });

        ArgumentCaptor<Invoice> captor = ArgumentCaptor.forClass(Invoice.class);

        InvoiceResponse response = invoiceService.createInvoice(request, "cashier-001");

        verify(invoiceRepository).save(captor.capture());
        assertThat(captor.getValue().getAppointmentId()).isNull();
        assertThat(response.getAppointmentId()).isNull();
        assertThat(response.getPatientId()).isEqualTo("patient-456");
    }

    @Test
    @DisplayName("REQ-5.4: AppointmentId en respuesta debe coincidir con el del request")
    void createInvoice_appointmentIdInResponseMatchesRequest() {
        String appointmentId = "appt-xyz-789";
        CreateInvoiceRequest request = new CreateInvoiceRequest(
            "patient-111",
            appointmentId,
            List.of(consultationCharge)
        );

        when(invoiceRepository.findAll()).thenReturn(List.of());
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> {
            Invoice inv = invocation.getArgument(0);
            inv.setId("invoice-id-003");
            return inv;
        });

        InvoiceResponse response = invoiceService.createInvoice(request, "user-456");

        assertThat(response.getAppointmentId()).isEqualTo(appointmentId);
        assertThat(response.getAppointmentId()).isNotNull();
    }
}
