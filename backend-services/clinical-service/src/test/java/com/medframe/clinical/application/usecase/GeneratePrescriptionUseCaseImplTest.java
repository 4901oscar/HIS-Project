package com.medframe.clinical.application.usecase;

import com.medframe.clinical.application.service.PermissionValidator;
import com.medframe.clinical.domain.exception.ForbiddenException;
import com.medframe.clinical.domain.model.Prescription;
import com.medframe.clinical.domain.service.PrescriptionGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GeneratePrescriptionUseCaseImpl.
 * 
 * Tests verify:
 * - DOCTOR role validation
 * - Delegation to PrescriptionGenerator domain service
 * - Proper exception handling for unauthorized access
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GeneratePrescriptionUseCaseImpl Tests")
class GeneratePrescriptionUseCaseImplTest {

    @Mock
    private PrescriptionGenerator prescriptionGenerator;

    @Mock
    private PermissionValidator permissionValidator;

    private GeneratePrescriptionUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new GeneratePrescriptionUseCaseImpl(prescriptionGenerator, permissionValidator);
    }

    @Test
    @DisplayName("Should generate prescription when user has DOCTOR role")
    void shouldGeneratePrescriptionWhenUserHasDoctorRole() {
        // Given
        String consultationId = "consultation-123";
        String patientId = "patient-456";
        String doctorId = "doctor-789";
        List<Prescription.Medication> medications = List.of(
            new Prescription.Medication("Amoxicillin", "500mg", "Every 8 hours", 7, "Oral", "Take with food")
        );

        Prescription expectedPrescription = new Prescription();
        expectedPrescription.setId("prescription-001");
        expectedPrescription.setConsultationId(consultationId);
        expectedPrescription.setPatientId(patientId);
        expectedPrescription.setDoctorId(doctorId);
        expectedPrescription.setPrescriptionCode("ABC12345");
        expectedPrescription.setMedications(medications);
        expectedPrescription.setStatus(Prescription.PrescriptionStatus.PENDING);
        expectedPrescription.setIssuedAt(LocalDateTime.now());
        expectedPrescription.setIssuedBy(doctorId);

        doNothing().when(permissionValidator).requireRole("DOCTOR");
        when(prescriptionGenerator.generatePrescription(consultationId, patientId, doctorId, medications))
            .thenReturn(expectedPrescription);

        // When
        Prescription result = useCase.generatePrescription(consultationId, patientId, doctorId, medications);

        // Then
        assertNotNull(result);
        assertEquals("prescription-001", result.getId());
        assertEquals("ABC12345", result.getPrescriptionCode());
        assertEquals(consultationId, result.getConsultationId());
        assertEquals(patientId, result.getPatientId());
        assertEquals(doctorId, result.getDoctorId());
        assertEquals(Prescription.PrescriptionStatus.PENDING, result.getStatus());
        assertEquals(1, result.getMedications().size());
        assertEquals("Amoxicillin", result.getMedications().get(0).getName());

        verify(permissionValidator).requireRole("DOCTOR");
        verify(prescriptionGenerator).generatePrescription(consultationId, patientId, doctorId, medications);
    }

    @Test
    @DisplayName("Should throw ForbiddenException when user does not have DOCTOR role")
    void shouldThrowForbiddenExceptionWhenUserDoesNotHaveDoctorRole() {
        // Given
        String consultationId = "consultation-123";
        String patientId = "patient-456";
        String doctorId = "doctor-789";
        List<Prescription.Medication> medications = List.of(
            new Prescription.Medication("Amoxicillin", "500mg", "Every 8 hours", 7, "Oral", "Take with food")
        );

        doThrow(new ForbiddenException("No tiene permisos para realizar esta operación. Roles requeridos: DOCTOR"))
            .when(permissionValidator).requireRole("DOCTOR");

        // When & Then
        ForbiddenException exception = assertThrows(ForbiddenException.class, () ->
            useCase.generatePrescription(consultationId, patientId, doctorId, medications)
        );

        assertEquals("No tiene permisos para realizar esta operación. Roles requeridos: DOCTOR", 
                     exception.getMessage());

        verify(permissionValidator).requireRole("DOCTOR");
        verify(prescriptionGenerator, never()).generatePrescription(anyString(), anyString(), anyString(), anyList());
    }

    @Test
    @DisplayName("Should generate prescription with multiple medications")
    void shouldGeneratePrescriptionWithMultipleMedications() {
        // Given
        String consultationId = "consultation-123";
        String patientId = "patient-456";
        String doctorId = "doctor-789";
        List<Prescription.Medication> medications = List.of(
            new Prescription.Medication("Amoxicillin", "500mg", "Every 8 hours", 7, "Oral", "Take with food"),
            new Prescription.Medication("Ibuprofen", "400mg", "Every 6 hours", 5, "Oral", "Take after meals"),
            new Prescription.Medication("Omeprazole", "20mg", "Once daily", 14, "Oral", "Take before breakfast")
        );

        Prescription expectedPrescription = new Prescription();
        expectedPrescription.setId("prescription-002");
        expectedPrescription.setPrescriptionCode("XYZ98765");
        expectedPrescription.setMedications(medications);
        expectedPrescription.setStatus(Prescription.PrescriptionStatus.PENDING);

        doNothing().when(permissionValidator).requireRole("DOCTOR");
        when(prescriptionGenerator.generatePrescription(consultationId, patientId, doctorId, medications))
            .thenReturn(expectedPrescription);

        // When
        Prescription result = useCase.generatePrescription(consultationId, patientId, doctorId, medications);

        // Then
        assertNotNull(result);
        assertEquals(3, result.getMedications().size());
        assertEquals("Amoxicillin", result.getMedications().get(0).getName());
        assertEquals("Ibuprofen", result.getMedications().get(1).getName());
        assertEquals("Omeprazole", result.getMedications().get(2).getName());

        verify(permissionValidator).requireRole("DOCTOR");
        verify(prescriptionGenerator).generatePrescription(consultationId, patientId, doctorId, medications);
    }

    @Test
    @DisplayName("Should validate DOCTOR role before delegating to domain service")
    void shouldValidateDoctorRoleBeforeDelegatingToDomainService() {
        // Given
        String consultationId = "consultation-123";
        String patientId = "patient-456";
        String doctorId = "doctor-789";
        List<Prescription.Medication> medications = List.of(
            new Prescription.Medication("Aspirin", "100mg", "Once daily", 30, "Oral", null)
        );

        Prescription expectedPrescription = new Prescription();
        expectedPrescription.setId("prescription-003");

        doNothing().when(permissionValidator).requireRole("DOCTOR");
        when(prescriptionGenerator.generatePrescription(consultationId, patientId, doctorId, medications))
            .thenReturn(expectedPrescription);

        // When
        useCase.generatePrescription(consultationId, patientId, doctorId, medications);

        // Then - verify order of operations
        var inOrder = inOrder(permissionValidator, prescriptionGenerator);
        inOrder.verify(permissionValidator).requireRole("DOCTOR");
        inOrder.verify(prescriptionGenerator).generatePrescription(consultationId, patientId, doctorId, medications);
    }
}
