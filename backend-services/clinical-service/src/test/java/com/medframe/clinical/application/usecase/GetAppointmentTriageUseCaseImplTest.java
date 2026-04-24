package com.medframe.clinical.application.usecase;

import com.medframe.clinical.domain.model.PriorityLevel;
import com.medframe.clinical.domain.model.Triage;
import com.medframe.clinical.domain.port.out.TriageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GetAppointmentTriageUseCaseImpl.
 * 
 * Tests validate that:
 * - Triage can be retrieved by appointmentId
 * - Empty result is returned when no triage exists for appointment
 * - Repository is called correctly
 * 
 * Requirements: 3.1, 3.2, 3.3 (Query Triage by Appointment)
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetAppointmentTriageUseCase Implementation Tests")
class GetAppointmentTriageUseCaseImplTest {
    
    @Mock
    private TriageRepository triageRepository;
    
    @InjectMocks
    private GetAppointmentTriageUseCaseImpl getAppointmentTriageUseCase;
    
    private String appointmentId;
    private Triage expectedTriage;
    
    @BeforeEach
    void setUp() {
        appointmentId = "appt-001";
        
        // Create a sample triage
        expectedTriage = new Triage();
        expectedTriage.setId("triage-789");
        expectedTriage.setAppointmentId(appointmentId);
        expectedTriage.setPatientId("patient-123");
        expectedTriage.setDoctorId("doctor-456");
        expectedTriage.setMotifId("M01");
        expectedTriage.setDiscriminatorIds(Arrays.asList("D01", "D02"));
        expectedTriage.setPriorityLevel(PriorityLevel.ORANGE);
        expectedTriage.setMaxWaitTimeMinutes(10);
        expectedTriage.setPerformedAt(LocalDateTime.now());
        expectedTriage.setPerformedBy("doctor-456");
    }
    
    @Test
    @DisplayName("Should return triage when it exists for appointment")
    void shouldReturnTriageWhenItExistsForAppointment() {
        // Given: Triage exists for the appointment
        when(triageRepository.findByAppointmentId(appointmentId))
            .thenReturn(Optional.of(expectedTriage));
        
        // When: Get appointment triage
        Optional<Triage> result = getAppointmentTriageUseCase.getAppointmentTriage(appointmentId);
        
        // Then: Should return the triage
        assertTrue(result.isPresent());
        assertEquals(expectedTriage.getId(), result.get().getId());
        assertEquals(expectedTriage.getAppointmentId(), result.get().getAppointmentId());
        assertEquals(expectedTriage.getPatientId(), result.get().getPatientId());
        assertEquals(expectedTriage.getPriorityLevel(), result.get().getPriorityLevel());
        
        verify(triageRepository).findByAppointmentId(appointmentId);
    }
    
    @Test
    @DisplayName("Should return empty when no triage exists for appointment")
    void shouldReturnEmptyWhenNoTriageExistsForAppointment() {
        // Given: No triage exists for the appointment
        when(triageRepository.findByAppointmentId(appointmentId))
            .thenReturn(Optional.empty());
        
        // When: Get appointment triage
        Optional<Triage> result = getAppointmentTriageUseCase.getAppointmentTriage(appointmentId);
        
        // Then: Should return empty
        assertFalse(result.isPresent());
        
        verify(triageRepository).findByAppointmentId(appointmentId);
    }
    
    @Test
    @DisplayName("Should call repository with correct appointmentId")
    void shouldCallRepositoryWithCorrectAppointmentId() {
        // Given: Repository is configured
        when(triageRepository.findByAppointmentId(appointmentId))
            .thenReturn(Optional.of(expectedTriage));
        
        // When: Get appointment triage
        getAppointmentTriageUseCase.getAppointmentTriage(appointmentId);
        
        // Then: Should call repository with exact appointmentId
        verify(triageRepository, times(1)).findByAppointmentId(appointmentId);
        verifyNoMoreInteractions(triageRepository);
    }
    
    @Test
    @DisplayName("Should handle different appointment IDs correctly")
    void shouldHandleDifferentAppointmentIdsCorrectly() {
        // Given: Different appointment IDs
        String appointmentId1 = "appt-001";
        String appointmentId2 = "appt-002";
        
        Triage triage1 = new Triage();
        triage1.setId("triage-001");
        triage1.setAppointmentId(appointmentId1);
        
        Triage triage2 = new Triage();
        triage2.setId("triage-002");
        triage2.setAppointmentId(appointmentId2);
        
        when(triageRepository.findByAppointmentId(appointmentId1))
            .thenReturn(Optional.of(triage1));
        when(triageRepository.findByAppointmentId(appointmentId2))
            .thenReturn(Optional.of(triage2));
        
        // When: Get triages for different appointments
        Optional<Triage> result1 = getAppointmentTriageUseCase.getAppointmentTriage(appointmentId1);
        Optional<Triage> result2 = getAppointmentTriageUseCase.getAppointmentTriage(appointmentId2);
        
        // Then: Should return correct triage for each appointment
        assertTrue(result1.isPresent());
        assertEquals("triage-001", result1.get().getId());
        assertEquals(appointmentId1, result1.get().getAppointmentId());
        
        assertTrue(result2.isPresent());
        assertEquals("triage-002", result2.get().getId());
        assertEquals(appointmentId2, result2.get().getAppointmentId());
    }
}
