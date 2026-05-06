package com.medframe.clinical.infrastructure.rest.controller;

import com.medframe.clinical.config.ConsultationPriceConfig;
import com.medframe.clinical.domain.exception.InvalidAppointmentStatusException;
import com.medframe.clinical.domain.model.Appointment;
import com.medframe.clinical.domain.model.Appointment.AppointmentStatus;
import com.medframe.clinical.domain.port.in.GetAppointmentTriageUseCase;
import com.medframe.clinical.domain.port.in.ListPendingTriageAppointmentsUseCase;
import com.medframe.clinical.domain.port.in.ManageAppointmentUseCase;
import com.medframe.clinical.domain.port.out.AppointmentRepository;
import com.medframe.clinical.domain.port.out.DoctorRepository;
import com.medframe.clinical.domain.port.out.PatientServiceClient;
import com.medframe.clinical.domain.port.out.VitalSignsRepository;
import com.medframe.clinical.domain.service.AppointmentManager;
import com.medframe.clinical.domain.service.PaymentValidator;
import com.medframe.clinical.infrastructure.client.BillingServiceClient;
import com.medframe.clinical.infrastructure.rest.dto.response.AppointmentListItemResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for lab workflow endpoints in AppointmentController.
 * Tests Task 4.1: Add PUT /api/clinical/appointments/{id}/lab/collect-samples endpoint
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AppointmentController - Lab Workflow Tests")
class AppointmentControllerLabWorkflowTest {

    @Mock private ManageAppointmentUseCase manageAppointmentUseCase;
    @Mock private AppointmentManager appointmentManager;
    @Mock private PatientServiceClient patientServiceClient;
    @Mock private DoctorRepository doctorRepository;
    @Mock private GetAppointmentTriageUseCase getAppointmentTriageUseCase;
    @Mock private ListPendingTriageAppointmentsUseCase listPendingTriageAppointmentsUseCase;
    @Mock private BillingServiceClient billingServiceClient;
    @Mock private ConsultationPriceConfig consultationPriceConfig;
    @Mock private PaymentValidator paymentValidator;
    @Mock private AppointmentRepository appointmentRepository;
    @Mock private VitalSignsRepository vitalSignsRepository;

    @InjectMocks private AppointmentController controller;

    private Appointment mockAppointment;

    @BeforeEach
    void setUp() {
        mockAppointment = new Appointment();
        mockAppointment.setId("appt-123");
        mockAppointment.setPatientId("patient-456");
        mockAppointment.setDoctorId("doctor-789");
        mockAppointment.setAppointmentDate(LocalDate.now());
        mockAppointment.setAppointmentTime(LocalTime.of(10, 0));
        mockAppointment.setStatus(AppointmentStatus.LAB_SAMPLE_COLLECTION);
    }

    // ========== Task 4.1: PUT /api/clinical/appointments/{id}/lab/collect-samples ==========

    @Test
    @DisplayName("REQ-4.5: Should successfully collect lab samples and transition to LAB_SAMPLE_PENDING")
    void collectLabSamples_withValidState_shouldTransitionToLabSamplePending() {
        // Arrange
        when(appointmentRepository.findById("appt-123")).thenReturn(Optional.of(mockAppointment));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment saved = invocation.getArgument(0);
            return saved;
        });

        // Act
        ResponseEntity<AppointmentListItemResponse> response = 
            controller.collectLabSamples("appt-123", "user-123");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(mockAppointment.getStatus()).isEqualTo(AppointmentStatus.LAB_SAMPLE_PENDING);
        verify(appointmentRepository).findById("appt-123");
        verify(appointmentRepository).save(mockAppointment);
    }

    @Test
    @DisplayName("REQ-9.3: Should return 400 when appointment is not in LAB_SAMPLE_COLLECTION state")
    void collectLabSamples_withInvalidState_shouldThrowInvalidAppointmentStatusException() {
        // Arrange
        mockAppointment.setStatus(AppointmentStatus.SCHEDULED); // Wrong state
        when(appointmentRepository.findById("appt-123")).thenReturn(Optional.of(mockAppointment));

        // Act & Assert
        assertThatThrownBy(() -> controller.collectLabSamples("appt-123", "user-123"))
            .isInstanceOf(InvalidAppointmentStatusException.class)
            .hasMessageContaining("No se pueden recolectar muestras");
        
        verify(appointmentRepository).findById("appt-123");
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("REQ-12.6: Should return 404 when appointment not found")
    void collectLabSamples_withNonExistentAppointment_shouldThrowRuntimeException() {
        // Arrange
        when(appointmentRepository.findById("non-existent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> controller.collectLabSamples("non-existent", "user-123"))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Cita no encontrada");
        
        verify(appointmentRepository).findById("non-existent");
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should save appointment after successful state transition")
    void collectLabSamples_shouldSaveAppointmentAfterTransition() {
        // Arrange
        when(appointmentRepository.findById("appt-123")).thenReturn(Optional.of(mockAppointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(mockAppointment);

        // Act
        controller.collectLabSamples("appt-123", "user-123");

        // Assert
        verify(appointmentRepository).save(mockAppointment);
        assertThat(mockAppointment.getStatus()).isEqualTo(AppointmentStatus.LAB_SAMPLE_PENDING);
    }

    // ========== Task 4.2: PUT /api/clinical/appointments/{id}/lab/accept-samples ==========

    @Test
    @DisplayName("REQ-5.4: Should successfully accept lab samples and transition to LAB_PROCESSING")
    void acceptLabSamples_withValidState_shouldTransitionToLabProcessing() {
        // Arrange
        mockAppointment.setStatus(AppointmentStatus.LAB_SAMPLE_PENDING);
        when(appointmentRepository.findById("appt-123")).thenReturn(Optional.of(mockAppointment));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment saved = invocation.getArgument(0);
            return saved;
        });

        // Act
        ResponseEntity<AppointmentListItemResponse> response = 
            controller.acceptLabSamples("appt-123", "user-123");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(mockAppointment.getStatus()).isEqualTo(AppointmentStatus.LAB_PROCESSING);
        verify(appointmentRepository).findById("appt-123");
        verify(appointmentRepository).save(mockAppointment);
    }

    @Test
    @DisplayName("REQ-9.3: Should return 400 when appointment is not in LAB_SAMPLE_PENDING state for accept")
    void acceptLabSamples_withInvalidState_shouldThrowInvalidAppointmentStatusException() {
        // Arrange
        mockAppointment.setStatus(AppointmentStatus.LAB_SAMPLE_COLLECTION); // Wrong state
        when(appointmentRepository.findById("appt-123")).thenReturn(Optional.of(mockAppointment));

        // Act & Assert
        assertThatThrownBy(() -> controller.acceptLabSamples("appt-123", "user-123"))
            .isInstanceOf(InvalidAppointmentStatusException.class)
            .hasMessageContaining("No se pueden aceptar muestras");
        
        verify(appointmentRepository).findById("appt-123");
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("REQ-12.6: Should return 404 when appointment not found for accept")
    void acceptLabSamples_withNonExistentAppointment_shouldThrowRuntimeException() {
        // Arrange
        when(appointmentRepository.findById("non-existent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> controller.acceptLabSamples("non-existent", "user-123"))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Cita no encontrada");
        
        verify(appointmentRepository).findById("non-existent");
        verify(appointmentRepository, never()).save(any());
    }

    // ========== Task 4.3: PUT /api/clinical/appointments/{id}/lab/reject-samples ==========

    @Test
    @DisplayName("REQ-5.5: Should successfully reject lab samples and transition back to LAB_SAMPLE_COLLECTION")
    void rejectLabSamples_withValidState_shouldTransitionToLabSampleCollection() {
        // Arrange
        mockAppointment.setStatus(AppointmentStatus.LAB_SAMPLE_PENDING);
        when(appointmentRepository.findById("appt-123")).thenReturn(Optional.of(mockAppointment));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment saved = invocation.getArgument(0);
            return saved;
        });

        // Act
        ResponseEntity<AppointmentListItemResponse> response = 
            controller.rejectLabSamples("appt-123", "user-123");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(mockAppointment.getStatus()).isEqualTo(AppointmentStatus.LAB_SAMPLE_COLLECTION);
        verify(appointmentRepository).findById("appt-123");
        verify(appointmentRepository).save(mockAppointment);
    }

    @Test
    @DisplayName("REQ-9.3: Should return 400 when appointment is not in LAB_SAMPLE_PENDING state for reject")
    void rejectLabSamples_withInvalidState_shouldThrowInvalidAppointmentStatusException() {
        // Arrange
        mockAppointment.setStatus(AppointmentStatus.LAB_PROCESSING); // Wrong state
        when(appointmentRepository.findById("appt-123")).thenReturn(Optional.of(mockAppointment));

        // Act & Assert
        assertThatThrownBy(() -> controller.rejectLabSamples("appt-123", "user-123"))
            .isInstanceOf(InvalidAppointmentStatusException.class)
            .hasMessageContaining("No se pueden rechazar muestras");
        
        verify(appointmentRepository).findById("appt-123");
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("REQ-12.6: Should return 404 when appointment not found for reject")
    void rejectLabSamples_withNonExistentAppointment_shouldThrowRuntimeException() {
        // Arrange
        when(appointmentRepository.findById("non-existent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> controller.rejectLabSamples("non-existent", "user-123"))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Cita no encontrada");
        
        verify(appointmentRepository).findById("non-existent");
        verify(appointmentRepository, never()).save(any());
    }

    // ========== Task 4.4: PUT /api/clinical/appointments/{id}/lab/complete-processing ==========

    @Test
    @DisplayName("REQ-6.9: Should successfully complete lab processing and transition to LAB_RESULTS_READY")
    void completeLabProcessing_withValidState_shouldTransitionToLabResultsReady() {
        // Arrange
        mockAppointment.setStatus(AppointmentStatus.LAB_PROCESSING);
        when(appointmentRepository.findById("appt-123")).thenReturn(Optional.of(mockAppointment));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment saved = invocation.getArgument(0);
            return saved;
        });

        // Act
        ResponseEntity<AppointmentListItemResponse> response = 
            controller.completeLabProcessing("appt-123", "user-123");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(mockAppointment.getStatus()).isEqualTo(AppointmentStatus.LAB_RESULTS_READY);
        verify(appointmentRepository).findById("appt-123");
        verify(appointmentRepository).save(mockAppointment);
    }

    @Test
    @DisplayName("REQ-9.3: Should return 400 when appointment is not in LAB_PROCESSING state for complete")
    void completeLabProcessing_withInvalidState_shouldThrowInvalidAppointmentStatusException() {
        // Arrange
        mockAppointment.setStatus(AppointmentStatus.LAB_SAMPLE_PENDING); // Wrong state
        when(appointmentRepository.findById("appt-123")).thenReturn(Optional.of(mockAppointment));

        // Act & Assert
        assertThatThrownBy(() -> controller.completeLabProcessing("appt-123", "user-123"))
            .isInstanceOf(InvalidAppointmentStatusException.class)
            .hasMessageContaining("No se puede completar el procesamiento");
        
        verify(appointmentRepository).findById("appt-123");
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("REQ-12.6: Should return 404 when appointment not found for complete")
    void completeLabProcessing_withNonExistentAppointment_shouldThrowRuntimeException() {
        // Arrange
        when(appointmentRepository.findById("non-existent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> controller.completeLabProcessing("non-existent", "user-123"))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Cita no encontrada");
        
        verify(appointmentRepository).findById("non-existent");
        verify(appointmentRepository, never()).save(any());
    }

    // ========== Task 4.5: PUT /api/clinical/appointments/{id}/lab/send-to-doctor ==========

    @Test
    @DisplayName("REQ-7.5: Should successfully send lab results to doctor and transition to CONSULTATION")
    void sendLabResultsToDoctor_withValidState_shouldTransitionToConsultation() {
        // Arrange
        mockAppointment.setStatus(AppointmentStatus.LAB_RESULTS_READY);
        when(appointmentRepository.findById("appt-123")).thenReturn(Optional.of(mockAppointment));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment saved = invocation.getArgument(0);
            return saved;
        });

        // Act
        ResponseEntity<AppointmentListItemResponse> response = 
            controller.sendLabResultsToDoctor("appt-123", "user-123");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(mockAppointment.getStatus()).isEqualTo(AppointmentStatus.CONSULTATION);
        verify(appointmentRepository).findById("appt-123");
        verify(appointmentRepository).save(mockAppointment);
    }

    @Test
    @DisplayName("REQ-9.3: Should return 400 when appointment is not in LAB_RESULTS_READY state for send")
    void sendLabResultsToDoctor_withInvalidState_shouldThrowInvalidAppointmentStatusException() {
        // Arrange
        mockAppointment.setStatus(AppointmentStatus.LAB_PROCESSING); // Wrong state
        when(appointmentRepository.findById("appt-123")).thenReturn(Optional.of(mockAppointment));

        // Act & Assert
        assertThatThrownBy(() -> controller.sendLabResultsToDoctor("appt-123", "user-123"))
            .isInstanceOf(InvalidAppointmentStatusException.class)
            .hasMessageContaining("No se pueden enviar resultados al doctor");
        
        verify(appointmentRepository).findById("appt-123");
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("REQ-12.6: Should return 404 when appointment not found for send")
    void sendLabResultsToDoctor_withNonExistentAppointment_shouldThrowRuntimeException() {
        // Arrange
        when(appointmentRepository.findById("non-existent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> controller.sendLabResultsToDoctor("non-existent", "user-123"))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Cita no encontrada");
        
        verify(appointmentRepository).findById("non-existent");
        verify(appointmentRepository, never()).save(any());
    }
}
