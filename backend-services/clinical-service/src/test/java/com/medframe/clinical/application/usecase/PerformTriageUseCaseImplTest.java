package com.medframe.clinical.application.usecase;

import com.medframe.clinical.application.service.PermissionValidator;
import com.medframe.clinical.domain.exception.ForbiddenException;
import com.medframe.clinical.domain.exception.VitalSignsNotFoundException;
import com.medframe.clinical.domain.model.PriorityLevel;
import com.medframe.clinical.domain.model.Triage;
import com.medframe.clinical.domain.port.out.TriageRepository;
import com.medframe.clinical.domain.service.TriageEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PerformTriageUseCaseImpl.
 * 
 * Tests validate that:
 * - Only users with DOCTOR role can perform triage
 * - Business logic is delegated to TriageEngine
 * - Triage results are persisted via TriageRepository
 * - Exceptions from domain services are propagated correctly
 * 
 * Requirements: Requirement 1 (Manchester Triage), Requirement 9 (Permission Validation)
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PerformTriageUseCase Implementation Tests")
class PerformTriageUseCaseImplTest {
    
    @Mock
    private TriageEngine triageEngine;
    
    @Mock
    private TriageRepository triageRepository;
    
    @Mock
    private PermissionValidator permissionValidator;
    
    @InjectMocks
    private PerformTriageUseCaseImpl performTriageUseCase;
    
    private String patientId;
    private String doctorId;
    private String motifId;
    private List<String> discriminatorIds;
    private Triage expectedTriage;
    
    @BeforeEach
    void setUp() {
        patientId = "patient-123";
        doctorId = "doctor-456";
        motifId = "M01";
        discriminatorIds = Arrays.asList("D01", "D02");
        
        // Create a sample triage result
        expectedTriage = new Triage();
        expectedTriage.setId("triage-789");
        expectedTriage.setPatientId(patientId);
        expectedTriage.setDoctorId(doctorId);
        expectedTriage.setMotifId(motifId);
        expectedTriage.setDiscriminatorIds(discriminatorIds);
        expectedTriage.setPriorityLevel(PriorityLevel.ORANGE);
        expectedTriage.setMaxWaitTimeMinutes(10);
        expectedTriage.setPerformedAt(LocalDateTime.now());
        expectedTriage.setPerformedBy(doctorId);
    }
    
    @Test
    @DisplayName("Should successfully perform triage when user has DOCTOR role")
    void shouldSuccessfullyPerformTriageWhenUserHasDoctorRole() {
        // Given: User has DOCTOR role, engine returns triage, repository saves it
        doNothing().when(permissionValidator).requireRole("DOCTOR");
        when(triageEngine.performTriage(patientId, doctorId, motifId, discriminatorIds))
            .thenReturn(expectedTriage);
        when(triageRepository.save(any(Triage.class))).thenReturn(expectedTriage);
        
        // When: Perform triage
        Triage result = performTriageUseCase.performTriage(patientId, doctorId, motifId, discriminatorIds);
        
        // Then: Should validate permissions, delegate to engine, and persist
        verify(permissionValidator).requireRole("DOCTOR");
        verify(triageEngine).performTriage(patientId, doctorId, motifId, discriminatorIds);
        verify(triageRepository).save(expectedTriage);
        
        assertNotNull(result);
        assertEquals(expectedTriage.getId(), result.getId());
        assertEquals(expectedTriage.getPatientId(), result.getPatientId());
        assertEquals(expectedTriage.getPriorityLevel(), result.getPriorityLevel());
    }
    
    @Test
    @DisplayName("Should throw ForbiddenException when user does not have DOCTOR role")
    void shouldThrowForbiddenExceptionWhenUserDoesNotHaveDoctorRole() {
        // Given: User does not have DOCTOR role
        doThrow(new ForbiddenException("No tiene permisos para realizar esta operación. Roles requeridos: DOCTOR"))
            .when(permissionValidator).requireRole("DOCTOR");
        
        // When/Then: Should throw ForbiddenException
        ForbiddenException exception = assertThrows(
            ForbiddenException.class,
            () -> performTriageUseCase.performTriage(patientId, doctorId, motifId, discriminatorIds)
        );
        
        assertTrue(exception.getMessage().contains("No tiene permisos"));
        
        // Verify that engine and repository were never called
        verify(permissionValidator).requireRole("DOCTOR");
        verify(triageEngine, never()).performTriage(anyString(), anyString(), anyString(), anyList());
        verify(triageRepository, never()).save(any(Triage.class));
    }
    
    @Test
    @DisplayName("Should propagate VitalSignsNotFoundException from TriageEngine")
    void shouldPropagateVitalSignsNotFoundExceptionFromTriageEngine() {
        // Given: Permission validation passes but patient has no vital signs
        doNothing().when(permissionValidator).requireRole("DOCTOR");
        when(triageEngine.performTriage(patientId, doctorId, motifId, discriminatorIds))
            .thenThrow(new VitalSignsNotFoundException(
                "El paciente no tiene signos vitales registrados. " +
                "Por favor, capture los signos vitales antes de realizar el triaje."));
        
        // When/Then: Should propagate the exception
        VitalSignsNotFoundException exception = assertThrows(
            VitalSignsNotFoundException.class,
            () -> performTriageUseCase.performTriage(patientId, doctorId, motifId, discriminatorIds)
        );
        
        assertTrue(exception.getMessage().contains("signos vitales"));
        
        // Verify that permission was validated and engine was called
        verify(permissionValidator).requireRole("DOCTOR");
        verify(triageEngine).performTriage(patientId, doctorId, motifId, discriminatorIds);
        
        // Verify that repository was never called
        verify(triageRepository, never()).save(any(Triage.class));
    }
    
    @Test
    @DisplayName("Should propagate IllegalArgumentException from TriageEngine for invalid discriminators")
    void shouldPropagateIllegalArgumentExceptionFromTriageEngine() {
        // Given: Permission validation passes but discriminators are invalid
        doNothing().when(permissionValidator).requireRole("DOCTOR");
        when(triageEngine.performTriage(patientId, doctorId, motifId, discriminatorIds))
            .thenThrow(new IllegalArgumentException(
                "No se encontraron discriminadores válidos. " +
                "Seleccione al menos un discriminador del catálogo Manchester."));
        
        // When/Then: Should propagate the exception
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> performTriageUseCase.performTriage(patientId, doctorId, motifId, discriminatorIds)
        );
        
        assertTrue(exception.getMessage().contains("discriminadores válidos"));
        
        // Verify that permission was validated and engine was called
        verify(permissionValidator).requireRole("DOCTOR");
        verify(triageEngine).performTriage(patientId, doctorId, motifId, discriminatorIds);
        
        // Verify that repository was never called
        verify(triageRepository, never()).save(any(Triage.class));
    }
    
    @Test
    @DisplayName("Should persist triage with correct priority level")
    void shouldPersistTriageWithCorrectPriorityLevel() {
        // Given: User has DOCTOR role and engine calculates RED priority
        Triage redTriage = new Triage();
        redTriage.setPatientId(patientId);
        redTriage.setDoctorId(doctorId);
        redTriage.setPriorityLevel(PriorityLevel.RED);
        redTriage.setMaxWaitTimeMinutes(0);
        
        doNothing().when(permissionValidator).requireRole("DOCTOR");
        when(triageEngine.performTriage(patientId, doctorId, motifId, discriminatorIds))
            .thenReturn(redTriage);
        when(triageRepository.save(any(Triage.class))).thenReturn(redTriage);
        
        // When: Perform triage
        Triage result = performTriageUseCase.performTriage(patientId, doctorId, motifId, discriminatorIds);
        
        // Then: Should persist triage with RED priority
        verify(triageRepository).save(redTriage);
        assertEquals(PriorityLevel.RED, result.getPriorityLevel());
        assertEquals(0, result.getMaxWaitTimeMinutes());
    }
    
    @Test
    @DisplayName("Should call components in correct order")
    void shouldCallComponentsInCorrectOrder() {
        // Given: All components are properly configured
        doNothing().when(permissionValidator).requireRole("DOCTOR");
        when(triageEngine.performTriage(patientId, doctorId, motifId, discriminatorIds))
            .thenReturn(expectedTriage);
        when(triageRepository.save(any(Triage.class))).thenReturn(expectedTriage);
        
        // When: Perform triage
        performTriageUseCase.performTriage(patientId, doctorId, motifId, discriminatorIds);
        
        // Then: Should call in order: permission validation → engine → repository
        var inOrder = inOrder(permissionValidator, triageEngine, triageRepository);
        inOrder.verify(permissionValidator).requireRole("DOCTOR");
        inOrder.verify(triageEngine).performTriage(patientId, doctorId, motifId, discriminatorIds);
        inOrder.verify(triageRepository).save(expectedTriage);
    }
    
    @Test
    @DisplayName("Should handle multiple discriminators correctly")
    void shouldHandleMultipleDiscriminatorsCorrectly() {
        // Given: Multiple discriminators are provided
        List<String> multipleDiscriminators = Arrays.asList("D01", "D02", "D03", "D04");
        
        doNothing().when(permissionValidator).requireRole("DOCTOR");
        when(triageEngine.performTriage(patientId, doctorId, motifId, multipleDiscriminators))
            .thenReturn(expectedTriage);
        when(triageRepository.save(any(Triage.class))).thenReturn(expectedTriage);
        
        // When: Perform triage with multiple discriminators
        Triage result = performTriageUseCase.performTriage(patientId, doctorId, motifId, multipleDiscriminators);
        
        // Then: Should successfully process all discriminators
        verify(triageEngine).performTriage(patientId, doctorId, motifId, multipleDiscriminators);
        assertNotNull(result);
    }
    
    @Test
    @DisplayName("Should return persisted triage with generated ID")
    void shouldReturnPersistedTriageWithGeneratedId() {
        // Given: Repository generates an ID when saving
        Triage triageWithoutId = new Triage();
        triageWithoutId.setPatientId(patientId);
        triageWithoutId.setDoctorId(doctorId);
        triageWithoutId.setPriorityLevel(PriorityLevel.YELLOW);
        
        Triage triageWithId = new Triage();
        triageWithId.setId("generated-id-123");
        triageWithId.setPatientId(patientId);
        triageWithId.setDoctorId(doctorId);
        triageWithId.setPriorityLevel(PriorityLevel.YELLOW);
        
        doNothing().when(permissionValidator).requireRole("DOCTOR");
        when(triageEngine.performTriage(patientId, doctorId, motifId, discriminatorIds))
            .thenReturn(triageWithoutId);
        when(triageRepository.save(triageWithoutId)).thenReturn(triageWithId);
        
        // When: Perform triage
        Triage result = performTriageUseCase.performTriage(patientId, doctorId, motifId, discriminatorIds);
        
        // Then: Should return triage with generated ID
        assertNotNull(result.getId());
        assertEquals("generated-id-123", result.getId());
    }
}
