package com.medframe.clinical.infrastructure.rest.controller;

import com.medframe.clinical.config.ConsultationPriceConfig;
import com.medframe.clinical.domain.model.Appointment;
import com.medframe.clinical.domain.port.in.GetAppointmentTriageUseCase;
import com.medframe.clinical.domain.port.in.ListPendingTriageAppointmentsUseCase;
import com.medframe.clinical.domain.port.in.ManageAppointmentUseCase;
import com.medframe.clinical.domain.port.out.DoctorRepository;
import com.medframe.clinical.domain.port.out.PatientServiceClient;
import com.medframe.clinical.domain.service.AppointmentManager;
import com.medframe.clinical.infrastructure.client.BillingServiceClient;
import com.medframe.clinical.infrastructure.client.dto.CreateInvoiceRequest;
import com.medframe.clinical.infrastructure.client.dto.InvoiceResponse;
import com.medframe.clinical.infrastructure.client.dto.PatientDTO;
import com.medframe.clinical.infrastructure.rest.dto.request.CreateAppointmentRequest;
import com.medframe.clinical.infrastructure.rest.dto.request.UpdateInvoiceIdRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests para la integración Appointment-Billing en AppointmentController.
 * Cubre tasks 9.4 (billing integration) y 15.3 (reconciliación manual).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AppointmentController - Billing Integration Tests")
class AppointmentControllerBillingTest {

    @Mock private ManageAppointmentUseCase manageAppointmentUseCase;
    @Mock private AppointmentManager appointmentManager;
    @Mock private PatientServiceClient patientServiceClient;
    @Mock private DoctorRepository doctorRepository;
    @Mock private GetAppointmentTriageUseCase getAppointmentTriageUseCase;
    @Mock private ListPendingTriageAppointmentsUseCase listPendingTriageAppointmentsUseCase;
    @Mock private BillingServiceClient billingServiceClient;
    @Mock private ConsultationPriceConfig consultationPriceConfig;

    @InjectMocks private AppointmentController controller;

    private PatientDTO mockPatient;
    private Appointment mockAppointment;
    private CreateAppointmentRequest createRequest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(controller, "billingServiceEnabled", true);

        mockPatient = new PatientDTO();
        mockPatient.setId("patient-456");
        mockPatient.setEmail("patient@test.com");
        mockPatient.setFirstName("Juan");
        mockPatient.setFullName("Juan Pérez");
        mockPatient.setDpi("2601234560101");

        mockAppointment = new Appointment();
        mockAppointment.setId("appt-id-123");
        mockAppointment.setPatientId("patient-456");
        mockAppointment.setDoctorId("doctor-789");
        mockAppointment.setAppointmentDate(LocalDate.now().plusDays(1));
        mockAppointment.setAppointmentTime(LocalTime.of(10, 0));

        createRequest = new CreateAppointmentRequest(
            "patient-456", "doctor-789",
            LocalDate.now().plusDays(1), LocalTime.of(10, 0),
            null, null
        );

        lenient().when(patientServiceClient.getPatient(anyString())).thenReturn(mockPatient);
        lenient().when(consultationPriceConfig.getPrice()).thenReturn(new BigDecimal("150.00"));
        lenient().when(appointmentManager.createAppointment(
            anyString(), anyString(), any(), any(), any(), anyString(), anyBoolean()))
            .thenReturn(mockAppointment);
        lenient().when(doctorRepository.findById(anyString())).thenReturn(Optional.empty());
    }

    // ========== Task 9.4: Billing Integration ==========

    @Test
    @DisplayName("REQ-1.1: Billing exitoso debe guardar invoiceId en la cita")
    void createAppointment_withBillingSuccess_shouldSetInvoiceIdOnAppointment() {
        InvoiceResponse invoiceResp = buildInvoiceResponse("invoice-id-001", "INV-20260424-0001", "patient-456");
        when(billingServiceClient.createInvoice(any(CreateInvoiceRequest.class), anyString()))
            .thenReturn(invoiceResp);

        ResponseEntity<?> response = controller.createAppointment(createRequest, "user-123");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(mockAppointment.getInvoiceId()).isEqualTo("invoice-id-001");
    }

    @Test
    @DisplayName("REQ-2.1: Fallback de Billing (null) debe dejar invoiceId en NULL")
    void createAppointment_withBillingFallbackNull_shouldLeaveInvoiceIdNull() {
        when(billingServiceClient.createInvoice(any(CreateInvoiceRequest.class), anyString()))
            .thenReturn(null);

        ResponseEntity<?> response = controller.createAppointment(createRequest, "user-123");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(mockAppointment.getInvoiceId()).isNull();
    }

    @Test
    @DisplayName("REQ-1.2: Debe llamar a billingServiceClient con patientId, appointmentId y cargo CONSULTATION")
    void createAppointment_shouldCallBillingClientWithCorrectParameters() {
        InvoiceResponse invoiceResp = buildInvoiceResponse("invoice-id-001", "INV-20260424-0001", "patient-456");
        when(billingServiceClient.createInvoice(any(CreateInvoiceRequest.class), anyString()))
            .thenReturn(invoiceResp);
        ArgumentCaptor<CreateInvoiceRequest> captor = ArgumentCaptor.forClass(CreateInvoiceRequest.class);

        controller.createAppointment(createRequest, "user-123");

        verify(billingServiceClient).createInvoice(captor.capture(), eq("user-123"));
        CreateInvoiceRequest captured = captor.getValue();
        assertThat(captured.getPatientId()).isEqualTo("patient-456");
        assertThat(captured.getAppointmentId()).isEqualTo("appt-id-123");
        assertThat(captured.getCharges()).hasSize(1);
        assertThat(captured.getCharges().get(0).getType()).isEqualTo("CONSULTATION");
        assertThat(captured.getCharges().get(0).getUnitPrice()).isEqualByComparingTo("150.00");
        assertThat(captured.getCharges().get(0).getQuantity()).isEqualTo(1);
    }

    @Test
    @DisplayName("REQ-11.4: Feature flag desactivado no debe llamar a Billing Service")
    void createAppointment_withBillingDisabled_shouldSkipBillingCall() {
        ReflectionTestUtils.setField(controller, "billingServiceEnabled", false);

        controller.createAppointment(createRequest, "user-123");

        verify(billingServiceClient, never()).createInvoice(any(), any());
        assertThat(mockAppointment.getInvoiceId()).isNull();
    }

    @Test
    @DisplayName("REQ-2.1: Excepción inesperada en Billing no debe bloquear creación de cita")
    void createAppointment_withBillingException_shouldCreateAppointmentWithoutInvoice() {
        when(billingServiceClient.createInvoice(any(CreateInvoiceRequest.class), anyString()))
            .thenThrow(new RuntimeException("Connection refused"));

        ResponseEntity<?> response = controller.createAppointment(createRequest, "user-123");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(mockAppointment.getInvoiceId()).isNull();
    }

    // ========== Task 15.3: Reconciliación Manual ==========

    @Test
    @DisplayName("REQ-12.1: missingInvoice=true debe retornar solo citas sin factura")
    void listAll_withMissingInvoiceFilter_shouldReturnOnlyAppointmentsWithoutInvoice() {
        Appointment noInvoiceAppt = buildAppointment("appt-no-invoice", "patient-111");
        when(manageAppointmentUseCase.listAppointmentsWithoutInvoice())
            .thenReturn(List.of(noInvoiceAppt));

        ResponseEntity<?> response = controller.listAll(true);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(manageAppointmentUseCase).listAppointmentsWithoutInvoice();
        verify(manageAppointmentUseCase, never()).listAll();
    }

    @Test
    @DisplayName("REQ-12.2: PATCH invoice debe actualizar correctamente y llamar a Billing Service")
    void updateInvoiceId_withValidMatchingInvoice_shouldReturnUpdatedAppointment() {
        Appointment existing = buildAppointment("appt-id-123", "patient-456");
        Appointment updated = buildAppointment("appt-id-123", "patient-456");
        updated.setInvoiceId("invoice-id-999");
        InvoiceResponse invoice = buildInvoiceResponse("invoice-id-999", "INV-20260424-0001", "patient-456");
        UpdateInvoiceIdRequest patchRequest = new UpdateInvoiceIdRequest("invoice-id-999");

        when(manageAppointmentUseCase.listAll()).thenReturn(List.of(existing));
        when(billingServiceClient.getInvoice("invoice-id-999")).thenReturn(invoice);
        when(manageAppointmentUseCase.updateInvoiceId("appt-id-123", "invoice-id-999")).thenReturn(updated);
        when(billingServiceClient.updateInvoiceAppointmentId("invoice-id-999", "appt-id-123", "user-123"))
            .thenReturn(true);

        ResponseEntity<?> response = controller.updateInvoiceId("appt-id-123", patchRequest, "user-123");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(manageAppointmentUseCase).updateInvoiceId("appt-id-123", "invoice-id-999");
        verify(billingServiceClient).updateInvoiceAppointmentId("invoice-id-999", "appt-id-123", "user-123");
    }

    @Test
    @DisplayName("REQ-12.3: PATCH con factura inexistente debe lanzar excepción")
    void updateInvoiceId_withNonExistentInvoice_shouldThrowRuntimeException() {
        Appointment existing = buildAppointment("appt-id-123", "patient-456");
        UpdateInvoiceIdRequest patchRequest = new UpdateInvoiceIdRequest("invoice-no-existe");

        when(manageAppointmentUseCase.listAll()).thenReturn(List.of(existing));
        when(billingServiceClient.getInvoice("invoice-no-existe")).thenReturn(null);

        assertThatThrownBy(() -> controller.updateInvoiceId("appt-id-123", patchRequest, "user-123"))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Factura no encontrada en Billing Service");
    }

    @Test
    @DisplayName("REQ-12.4: PATCH con patientId diferente debe lanzar excepción")
    void updateInvoiceId_withPatientIdMismatch_shouldThrowRuntimeException() {
        Appointment existing = buildAppointment("appt-id-123", "patient-456");
        InvoiceResponse invoiceOtherPatient = buildInvoiceResponse("invoice-id-999", "INV-20260424-0001", "patient-DIFERENTE");
        UpdateInvoiceIdRequest patchRequest = new UpdateInvoiceIdRequest("invoice-id-999");

        when(manageAppointmentUseCase.listAll()).thenReturn(List.of(existing));
        when(billingServiceClient.getInvoice("invoice-id-999")).thenReturn(invoiceOtherPatient);

        assertThatThrownBy(() -> controller.updateInvoiceId("appt-id-123", patchRequest, "user-123"))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("La factura no corresponde al mismo paciente de la cita");
    }

    // ========== Helpers ==========

    private InvoiceResponse buildInvoiceResponse(String id, String number, String patientId) {
        InvoiceResponse r = new InvoiceResponse();
        r.setId(id);
        r.setInvoiceNumber(number);
        r.setPatientId(patientId);
        return r;
    }

    private Appointment buildAppointment(String id, String patientId) {
        Appointment a = new Appointment();
        a.setId(id);
        a.setPatientId(patientId);
        a.setDoctorId("doctor-789");
        a.setAppointmentDate(LocalDate.now());
        a.setAppointmentTime(LocalTime.of(10, 0));
        return a;
    }
}
