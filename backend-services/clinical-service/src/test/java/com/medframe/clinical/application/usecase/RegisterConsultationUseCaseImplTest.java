package com.medframe.clinical.application.usecase;

import com.medframe.clinical.application.service.PermissionValidator;
import com.medframe.clinical.domain.exception.ForbiddenException;
import com.medframe.clinical.domain.model.Consultation;
import com.medframe.clinical.domain.service.ConsultationManager;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RegisterConsultationUseCaseImpl.
 * 
 * Tests validate that:
 * - Only users with DOCTOR role can register consultations
 * - Business logic is delegated to ConsultationManager
 * - Consultation data is properly passed through
 * - Exceptions from domain services are propagated correctly
 * 
 * Requirements: Requirement 4 (Medical Consultation), Requirement 9 (Permission Validation)
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterConsultationUseCase Implementation Tests")
class RegisterConsultationUseCaseImplTest {
    
    @Mock
    private ConsultationManager consultationManager;
    
    @Mock
    private PermissionValidator permissionValidator;
    
    @InjectMocks
    private RegisterConsultationUseCaseImpl registerConsultationUseCase;
    
    private String patientId;
    private String doctorId;
    private String appointmentId;
    private String chiefComplaint;
    private String symptoms;
    private String primaryDiagnosis;
    private List<String> secondaryDiagnoses;
    private String medicalNotes;
    private String treatmentPlan;
    private Consultation expectedConsultation;
    
    @BeforeEach
    void setUp() {
        patientId = "patient-123";
        doctorId = "doctor-456";
        appointmentId = "appointment-789";
        chiefComplaint = "Dolor de cabeza intenso";
        symptoms = "Cefalea, náuseas, fotofobia";
        primaryDiagnosis = "G43.1";  // CIE-10: Migraña con aura
        secondaryDiagnoses = Arrays.asList("R51", "R11");  // Cefalea, Náuseas
        medicalNotes = "Paciente presenta migraña con aura típica";
        treatmentPlan = "Sumatriptán 50mg, reposo en ambiente oscuro";
        
        // Create a sample consultation result
        expectedConsultation = new Consultation();
        expectedConsultation.setId("consultation-001");
        expectedConsultation.setPatientId(patientId);
        expectedConsultation.setDoctorId(doctorId);
        expectedConsultation.setAppointmentId(appointmentId);
        expectedConsultation.setChiefComplaint(chiefComplaint);
        expectedConsultation.setSymptoms(symptoms);
        expectedConsultation.setPrimaryDiagnosis(primaryDiagnosis);
        expectedConsultation.setSecondaryDiagnoses(secondaryDiagnoses);
        expectedConsultation.setMedicalNotes(medicalNotes);
        expectedConsultation.setTreatmentPlan(treatmentPlan);
        expectedConsultation.setConsultationDate(LocalDateTime.now());
        expectedConsultation.setPerformedBy(doctorId);
    }
    
    @Test
    @DisplayName("Should successfully register consultation when user has DOCTOR role")
    void shouldSuccessfullyRegisterConsultationWhenUserHasDoctorRole() {
        // Given: User has DOCTOR role and manager returns consultation
        doNothing().when(permissionValidator).requireRole("DOCTOR");
        when(consultationManager.registerConsultation(
            patientId, doctorId, appointmentId, chiefComplaint, symptoms,
            primaryDiagnosis, secondaryDiagnoses, medicalNotes, treatmentPlan
        )).thenReturn(expectedConsultation);
        
        // When: Register consultation
        Consultation result = registerConsultationUseCase.registerConsultation(
            patientId, doctorId, appointmentId, chiefComplaint, symptoms,
            primaryDiagnosis, secondaryDiagnoses, medicalNotes, treatmentPlan
        );
        
        // Then: Should validate permissions and delegate to manager
        verify(permissionValidator).requireRole("DOCTOR");
        verify(consultationManager).registerConsultation(
            patientId, doctorId, appointmentId, chiefComplaint, symptoms,
            primaryDiagnosis, secondaryDiagnoses, medicalNotes, treatmentPlan
        );
        
        assertNotNull(result);
        assertEquals(expectedConsultation.getId(), result.getId());
        assertEquals(expectedConsultation.getPatientId(), result.getPatientId());
        assertEquals(expectedConsultation.getDoctorId(), result.getDoctorId());
        assertEquals(expectedConsultation.getPrimaryDiagnosis(), result.getPrimaryDiagnosis());
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
            () -> registerConsultationUseCase.registerConsultation(
                patientId, doctorId, appointmentId, chiefComplaint, symptoms,
                primaryDiagnosis, secondaryDiagnoses, medicalNotes, treatmentPlan
            )
        );
        
        assertTrue(exception.getMessage().contains("No tiene permisos"));
        
        // Verify that manager was never called
        verify(permissionValidator).requireRole("DOCTOR");
        verify(consultationManager, never()).registerConsultation(
            anyString(), anyString(), anyString(), anyString(), anyString(),
            anyString(), anyList(), anyString(), anyString()
        );
    }
    
    @Test
    @DisplayName("Should register consultation without appointment ID")
    void shouldRegisterConsultationWithoutAppointmentId() {
        // Given: Consultation without linked appointment
        doNothing().when(permissionValidator).requireRole("DOCTOR");
        when(consultationManager.registerConsultation(
            patientId, doctorId, null, chiefComplaint, symptoms,
            primaryDiagnosis, secondaryDiagnoses, medicalNotes, treatmentPlan
        )).thenReturn(expectedConsultation);
        
        // When: Register consultation without appointment ID
        Consultation result = registerConsultationUseCase.registerConsultation(
            patientId, doctorId, null, chiefComplaint, symptoms,
            primaryDiagnosis, secondaryDiagnoses, medicalNotes, treatmentPlan
        );
        
        // Then: Should successfully register consultation
        verify(consultationManager).registerConsultation(
            patientId, doctorId, null, chiefComplaint, symptoms,
            primaryDiagnosis, secondaryDiagnoses, medicalNotes, treatmentPlan
        );
        assertNotNull(result);
    }
    
    @Test
    @DisplayName("Should register consultation with multiple secondary diagnoses")
    void shouldRegisterConsultationWithMultipleSecondaryDiagnoses() {
        // Given: Consultation with multiple secondary diagnoses
        List<String> multipleDiagnoses = Arrays.asList("R51", "R11", "R42", "G44.2");
        
        doNothing().when(permissionValidator).requireRole("DOCTOR");
        when(consultationManager.registerConsultation(
            patientId, doctorId, appointmentId, chiefComplaint, symptoms,
            primaryDiagnosis, multipleDiagnoses, medicalNotes, treatmentPlan
        )).thenReturn(expectedConsultation);
        
        // When: Register consultation with multiple secondary diagnoses
        Consultation result = registerConsultationUseCase.registerConsultation(
            patientId, doctorId, appointmentId, chiefComplaint, symptoms,
            primaryDiagnosis, multipleDiagnoses, medicalNotes, treatmentPlan
        );
        
        // Then: Should successfully process all diagnoses
        verify(consultationManager).registerConsultation(
            patientId, doctorId, appointmentId, chiefComplaint, symptoms,
            primaryDiagnosis, multipleDiagnoses, medicalNotes, treatmentPlan
        );
        assertNotNull(result);
    }
    
    @Test
    @DisplayName("Should call components in correct order")
    void shouldCallComponentsInCorrectOrder() {
        // Given: All components are properly configured
        doNothing().when(permissionValidator).requireRole("DOCTOR");
        when(consultationManager.registerConsultation(
            patientId, doctorId, appointmentId, chiefComplaint, symptoms,
            primaryDiagnosis, secondaryDiagnoses, medicalNotes, treatmentPlan
        )).thenReturn(expectedConsultation);
        
        // When: Register consultation
        registerConsultationUseCase.registerConsultation(
            patientId, doctorId, appointmentId, chiefComplaint, symptoms,
            primaryDiagnosis, secondaryDiagnoses, medicalNotes, treatmentPlan
        );
        
        // Then: Should call in order: permission validation → manager
        var inOrder = inOrder(permissionValidator, consultationManager);
        inOrder.verify(permissionValidator).requireRole("DOCTOR");
        inOrder.verify(consultationManager).registerConsultation(
            patientId, doctorId, appointmentId, chiefComplaint, symptoms,
            primaryDiagnosis, secondaryDiagnoses, medicalNotes, treatmentPlan
        );
    }
    
    @Test
    @DisplayName("Should handle consultation with minimal data")
    void shouldHandleConsultationWithMinimalData() {
        // Given: Consultation with only required fields
        doNothing().when(permissionValidator).requireRole("DOCTOR");
        when(consultationManager.registerConsultation(
            patientId, doctorId, null, chiefComplaint, null,
            primaryDiagnosis, null, null, null
        )).thenReturn(expectedConsultation);
        
        // When: Register consultation with minimal data
        Consultation result = registerConsultationUseCase.registerConsultation(
            patientId, doctorId, null, chiefComplaint, null,
            primaryDiagnosis, null, null, null
        );
        
        // Then: Should successfully register consultation
        verify(consultationManager).registerConsultation(
            patientId, doctorId, null, chiefComplaint, null,
            primaryDiagnosis, null, null, null
        );
        assertNotNull(result);
    }
    
    @Test
    @DisplayName("Should return consultation with generated ID")
    void shouldReturnConsultationWithGeneratedId() {
        // Given: Manager generates an ID when saving
        Consultation consultationWithId = new Consultation();
        consultationWithId.setId("generated-consultation-id-456");
        consultationWithId.setPatientId(patientId);
        consultationWithId.setDoctorId(doctorId);
        consultationWithId.setPrimaryDiagnosis(primaryDiagnosis);
        
        doNothing().when(permissionValidator).requireRole("DOCTOR");
        when(consultationManager.registerConsultation(
            patientId, doctorId, appointmentId, chiefComplaint, symptoms,
            primaryDiagnosis, secondaryDiagnoses, medicalNotes, treatmentPlan
        )).thenReturn(consultationWithId);
        
        // When: Register consultation
        Consultation result = registerConsultationUseCase.registerConsultation(
            patientId, doctorId, appointmentId, chiefComplaint, symptoms,
            primaryDiagnosis, secondaryDiagnoses, medicalNotes, treatmentPlan
        );
        
        // Then: Should return consultation with generated ID
        assertNotNull(result.getId());
        assertEquals("generated-consultation-id-456", result.getId());
    }
    
    @Test
    @DisplayName("Should propagate exceptions from ConsultationManager")
    void shouldPropagateExceptionsFromConsultationManager() {
        // Given: Manager throws an exception
        doNothing().when(permissionValidator).requireRole("DOCTOR");
        when(consultationManager.registerConsultation(
            patientId, doctorId, appointmentId, chiefComplaint, symptoms,
            primaryDiagnosis, secondaryDiagnoses, medicalNotes, treatmentPlan
        )).thenThrow(new IllegalArgumentException("Invalid diagnosis code"));
        
        // When/Then: Should propagate the exception
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> registerConsultationUseCase.registerConsultation(
                patientId, doctorId, appointmentId, chiefComplaint, symptoms,
                primaryDiagnosis, secondaryDiagnoses, medicalNotes, treatmentPlan
            )
        );
        
        assertEquals("Invalid diagnosis code", exception.getMessage());
        
        // Verify that permission was validated and manager was called
        verify(permissionValidator).requireRole("DOCTOR");
        verify(consultationManager).registerConsultation(
            patientId, doctorId, appointmentId, chiefComplaint, symptoms,
            primaryDiagnosis, secondaryDiagnoses, medicalNotes, treatmentPlan
        );
    }
}
