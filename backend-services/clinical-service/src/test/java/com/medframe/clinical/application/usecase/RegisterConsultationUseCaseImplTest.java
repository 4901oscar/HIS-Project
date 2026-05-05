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
        primaryDiagnosis = "G43.1";
        secondaryDiagnoses = Arrays.asList("R51", "R11");
        medicalNotes = "Paciente presenta migraña con aura típica";
        treatmentPlan = "Sumatriptán 50mg, reposo en ambiente oscuro";

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
        doNothing().when(permissionValidator).requireRole("DOCTOR");
        when(consultationManager.registerConsultation(
            patientId, doctorId, appointmentId, chiefComplaint, symptoms,
            primaryDiagnosis, secondaryDiagnoses, medicalNotes, treatmentPlan, false, false
        )).thenReturn(expectedConsultation);

        Consultation result = registerConsultationUseCase.registerConsultation(
            patientId, doctorId, appointmentId, chiefComplaint, symptoms,
            primaryDiagnosis, secondaryDiagnoses, medicalNotes, treatmentPlan, false, false
        );

        verify(permissionValidator).requireRole("DOCTOR");
        verify(consultationManager).registerConsultation(
            patientId, doctorId, appointmentId, chiefComplaint, symptoms,
            primaryDiagnosis, secondaryDiagnoses, medicalNotes, treatmentPlan, false, false
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
        doThrow(new ForbiddenException("No tiene permisos para realizar esta operación. Roles requeridos: DOCTOR"))
            .when(permissionValidator).requireRole("DOCTOR");

        ForbiddenException exception = assertThrows(
            ForbiddenException.class,
            () -> registerConsultationUseCase.registerConsultation(
                patientId, doctorId, appointmentId, chiefComplaint, symptoms,
                primaryDiagnosis, secondaryDiagnoses, medicalNotes, treatmentPlan, false, false
            )
        );

        assertTrue(exception.getMessage().contains("No tiene permisos"));

        verify(permissionValidator).requireRole("DOCTOR");
        verify(consultationManager, never()).registerConsultation(
            anyString(), anyString(), anyString(), anyString(), anyString(),
            anyString(), anyList(), anyString(), anyString(), anyBoolean(), anyBoolean()
        );
    }

    @Test
    @DisplayName("Should register consultation without appointment ID")
    void shouldRegisterConsultationWithoutAppointmentId() {
        doNothing().when(permissionValidator).requireRole("DOCTOR");
        when(consultationManager.registerConsultation(
            patientId, doctorId, null, chiefComplaint, symptoms,
            primaryDiagnosis, secondaryDiagnoses, medicalNotes, treatmentPlan, false, false
        )).thenReturn(expectedConsultation);

        Consultation result = registerConsultationUseCase.registerConsultation(
            patientId, doctorId, null, chiefComplaint, symptoms,
            primaryDiagnosis, secondaryDiagnoses, medicalNotes, treatmentPlan, false, false
        );

        verify(consultationManager).registerConsultation(
            patientId, doctorId, null, chiefComplaint, symptoms,
            primaryDiagnosis, secondaryDiagnoses, medicalNotes, treatmentPlan, false, false
        );
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should register consultation with multiple secondary diagnoses")
    void shouldRegisterConsultationWithMultipleSecondaryDiagnoses() {
        List<String> multipleDiagnoses = Arrays.asList("R51", "R11", "R42", "G44.2");

        doNothing().when(permissionValidator).requireRole("DOCTOR");
        when(consultationManager.registerConsultation(
            patientId, doctorId, appointmentId, chiefComplaint, symptoms,
            primaryDiagnosis, multipleDiagnoses, medicalNotes, treatmentPlan, false, false
        )).thenReturn(expectedConsultation);

        Consultation result = registerConsultationUseCase.registerConsultation(
            patientId, doctorId, appointmentId, chiefComplaint, symptoms,
            primaryDiagnosis, multipleDiagnoses, medicalNotes, treatmentPlan, false, false
        );

        verify(consultationManager).registerConsultation(
            patientId, doctorId, appointmentId, chiefComplaint, symptoms,
            primaryDiagnosis, multipleDiagnoses, medicalNotes, treatmentPlan, false, false
        );
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should call components in correct order")
    void shouldCallComponentsInCorrectOrder() {
        doNothing().when(permissionValidator).requireRole("DOCTOR");
        when(consultationManager.registerConsultation(
            patientId, doctorId, appointmentId, chiefComplaint, symptoms,
            primaryDiagnosis, secondaryDiagnoses, medicalNotes, treatmentPlan, false, false
        )).thenReturn(expectedConsultation);

        registerConsultationUseCase.registerConsultation(
            patientId, doctorId, appointmentId, chiefComplaint, symptoms,
            primaryDiagnosis, secondaryDiagnoses, medicalNotes, treatmentPlan, false, false
        );

        var inOrder = inOrder(permissionValidator, consultationManager);
        inOrder.verify(permissionValidator).requireRole("DOCTOR");
        inOrder.verify(consultationManager).registerConsultation(
            patientId, doctorId, appointmentId, chiefComplaint, symptoms,
            primaryDiagnosis, secondaryDiagnoses, medicalNotes, treatmentPlan, false, false
        );
    }

    @Test
    @DisplayName("Should handle consultation with minimal data")
    void shouldHandleConsultationWithMinimalData() {
        doNothing().when(permissionValidator).requireRole("DOCTOR");
        when(consultationManager.registerConsultation(
            patientId, doctorId, null, chiefComplaint, null,
            primaryDiagnosis, null, null, null, false, false
        )).thenReturn(expectedConsultation);

        Consultation result = registerConsultationUseCase.registerConsultation(
            patientId, doctorId, null, chiefComplaint, null,
            primaryDiagnosis, null, null, null, false, false
        );

        verify(consultationManager).registerConsultation(
            patientId, doctorId, null, chiefComplaint, null,
            primaryDiagnosis, null, null, null, false, false
        );
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should return consultation with generated ID")
    void shouldReturnConsultationWithGeneratedId() {
        Consultation consultationWithId = new Consultation();
        consultationWithId.setId("generated-consultation-id-456");
        consultationWithId.setPatientId(patientId);
        consultationWithId.setDoctorId(doctorId);
        consultationWithId.setPrimaryDiagnosis(primaryDiagnosis);

        doNothing().when(permissionValidator).requireRole("DOCTOR");
        when(consultationManager.registerConsultation(
            patientId, doctorId, appointmentId, chiefComplaint, symptoms,
            primaryDiagnosis, secondaryDiagnoses, medicalNotes, treatmentPlan, false, false
        )).thenReturn(consultationWithId);

        Consultation result = registerConsultationUseCase.registerConsultation(
            patientId, doctorId, appointmentId, chiefComplaint, symptoms,
            primaryDiagnosis, secondaryDiagnoses, medicalNotes, treatmentPlan, false, false
        );

        assertNotNull(result.getId());
        assertEquals("generated-consultation-id-456", result.getId());
    }

    @Test
    @DisplayName("Should propagate exceptions from ConsultationManager")
    void shouldPropagateExceptionsFromConsultationManager() {
        doNothing().when(permissionValidator).requireRole("DOCTOR");
        when(consultationManager.registerConsultation(
            patientId, doctorId, appointmentId, chiefComplaint, symptoms,
            primaryDiagnosis, secondaryDiagnoses, medicalNotes, treatmentPlan, false, false
        )).thenThrow(new IllegalArgumentException("Invalid diagnosis code"));

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> registerConsultationUseCase.registerConsultation(
                patientId, doctorId, appointmentId, chiefComplaint, symptoms,
                primaryDiagnosis, secondaryDiagnoses, medicalNotes, treatmentPlan, false, false
            )
        );

        assertEquals("Invalid diagnosis code", exception.getMessage());

        verify(permissionValidator).requireRole("DOCTOR");
        verify(consultationManager).registerConsultation(
            patientId, doctorId, appointmentId, chiefComplaint, symptoms,
            primaryDiagnosis, secondaryDiagnoses, medicalNotes, treatmentPlan, false, false
        );
    }
}
