package com.medframe.clinical.application.usecase;

import com.medframe.clinical.domain.model.Appointment;
import com.medframe.clinical.domain.port.out.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ListPendingTriageAppointmentsUseCaseImpl.
 * 
 * Tests validate that:
 * - Pending triage appointments can be retrieved
 * - Empty list is returned when no pending appointments exist
 * - Repository is called correctly
 * - All returned appointments are in ACTIVE status
 * 
 * Requirements: 4.1, 4.3, 4.4 (List Appointments Pending Triage)
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ListPendingTriageAppointmentsUseCase Implementation Tests")
class ListPendingTriageAppointmentsUseCaseImplTest {
    
    @Mock
    private AppointmentRepository appointmentRepository;
    
    @InjectMocks
    private ListPendingTriageAppointmentsUseCaseImpl listPendingTriageAppointmentsUseCase;
    
    private Appointment appointment1;
    private Appointment appointment2;
    private Appointment appointment3;
    
    @BeforeEach
    void setUp() {
        // Create sample appointments
        appointment1 = new Appointment();
        appointment1.setId("appt-001");
        appointment1.setPatientId("patient-123");
        appointment1.setDoctorId("doctor-456");
        appointment1.setAppointmentDate(LocalDate.now());
        appointment1.setAppointmentTime(LocalTime.of(10, 0));
        appointment1.setStatus(Appointment.AppointmentStatus.ACTIVE);
        appointment1.setNotes("Dolor de cabeza");
        
        appointment2 = new Appointment();
        appointment2.setId("appt-002");
        appointment2.setPatientId("patient-789");
        appointment2.setDoctorId("doctor-456");
        appointment2.setAppointmentDate(LocalDate.now());
        appointment2.setAppointmentTime(LocalTime.of(10, 30));
        appointment2.setStatus(Appointment.AppointmentStatus.ACTIVE);
        appointment2.setNotes("Fiebre");
        
        appointment3 = new Appointment();
        appointment3.setId("appt-003");
        appointment3.setPatientId("patient-321");
        appointment3.setDoctorId("doctor-789");
        appointment3.setAppointmentDate(LocalDate.now());
        appointment3.setAppointmentTime(LocalTime.of(11, 0));
        appointment3.setStatus(Appointment.AppointmentStatus.ACTIVE);
        appointment3.setNotes("Dolor de estómago");
    }
    
    @Test
    @DisplayName("Should return list of pending triage appointments")
    void shouldReturnListOfPendingTriageAppointments() {
        // Given: Multiple appointments are pending triage
        List<Appointment> pendingAppointments = Arrays.asList(appointment1, appointment2, appointment3);
        when(appointmentRepository.findPendingTriage())
            .thenReturn(pendingAppointments);
        
        // When: List pending triage appointments
        List<Appointment> result = listPendingTriageAppointmentsUseCase.listPendingTriageAppointments();
        
        // Then: Should return all pending appointments
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("appt-001", result.get(0).getId());
        assertEquals("appt-002", result.get(1).getId());
        assertEquals("appt-003", result.get(2).getId());
        
        // Verify all appointments are ACTIVE
        assertTrue(result.stream().allMatch(a -> a.getStatus() == Appointment.AppointmentStatus.ACTIVE));
        
        verify(appointmentRepository).findPendingTriage();
    }
    
    @Test
    @DisplayName("Should return empty list when no pending appointments exist")
    void shouldReturnEmptyListWhenNoPendingAppointmentsExist() {
        // Given: No appointments are pending triage
        when(appointmentRepository.findPendingTriage())
            .thenReturn(Collections.emptyList());
        
        // When: List pending triage appointments
        List<Appointment> result = listPendingTriageAppointmentsUseCase.listPendingTriageAppointments();
        
        // Then: Should return empty list
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(appointmentRepository).findPendingTriage();
    }
    
    @Test
    @DisplayName("Should call repository exactly once")
    void shouldCallRepositoryExactlyOnce() {
        // Given: Repository is configured
        when(appointmentRepository.findPendingTriage())
            .thenReturn(Arrays.asList(appointment1));
        
        // When: List pending triage appointments
        listPendingTriageAppointmentsUseCase.listPendingTriageAppointments();
        
        // Then: Should call repository exactly once
        verify(appointmentRepository, times(1)).findPendingTriage();
        verifyNoMoreInteractions(appointmentRepository);
    }
    
    @Test
    @DisplayName("Should return single appointment when only one is pending")
    void shouldReturnSingleAppointmentWhenOnlyOneIsPending() {
        // Given: Only one appointment is pending triage
        when(appointmentRepository.findPendingTriage())
            .thenReturn(Collections.singletonList(appointment1));
        
        // When: List pending triage appointments
        List<Appointment> result = listPendingTriageAppointmentsUseCase.listPendingTriageAppointments();
        
        // Then: Should return single appointment
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("appt-001", result.get(0).getId());
        assertEquals(Appointment.AppointmentStatus.ACTIVE, result.get(0).getStatus());
    }
    
    @Test
    @DisplayName("Should preserve appointment details in result")
    void shouldPreserveAppointmentDetailsInResult() {
        // Given: Appointment with specific details
        when(appointmentRepository.findPendingTriage())
            .thenReturn(Collections.singletonList(appointment1));
        
        // When: List pending triage appointments
        List<Appointment> result = listPendingTriageAppointmentsUseCase.listPendingTriageAppointments();
        
        // Then: Should preserve all appointment details
        Appointment resultAppointment = result.get(0);
        assertEquals("appt-001", resultAppointment.getId());
        assertEquals("patient-123", resultAppointment.getPatientId());
        assertEquals("doctor-456", resultAppointment.getDoctorId());
        assertEquals(LocalDate.now(), resultAppointment.getAppointmentDate());
        assertEquals(LocalTime.of(10, 0), resultAppointment.getAppointmentTime());
        assertEquals(Appointment.AppointmentStatus.ACTIVE, resultAppointment.getStatus());
        assertEquals("Dolor de cabeza", resultAppointment.getNotes());
    }
    
    @Test
    @DisplayName("Should handle large list of pending appointments")
    void shouldHandleLargeListOfPendingAppointments() {
        // Given: Large number of pending appointments
        List<Appointment> largeList = Arrays.asList(
            appointment1, appointment2, appointment3,
            appointment1, appointment2, appointment3,
            appointment1, appointment2, appointment3,
            appointment1
        );
        when(appointmentRepository.findPendingTriage())
            .thenReturn(largeList);
        
        // When: List pending triage appointments
        List<Appointment> result = listPendingTriageAppointmentsUseCase.listPendingTriageAppointments();
        
        // Then: Should return all appointments
        assertNotNull(result);
        assertEquals(10, result.size());
    }
}
