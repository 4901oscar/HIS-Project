package com.medframe.clinical.domain.service;

import com.medframe.clinical.domain.exception.ForbiddenException;
import com.medframe.clinical.domain.model.Consultation;
import com.medframe.clinical.domain.port.in.GetMedicalHistoryUseCase;
import com.medframe.clinical.domain.port.out.*;
import net.jqwik.api.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Feature: clinical-service, Property 15: Patients Can Only Access Their Own Medical History
 */
class MedicalHistoryAggregatorTest {

    private MedicalHistoryAggregator buildAggregator() {
        PatientServiceClient patientClient = Mockito.mock(PatientServiceClient.class);
        ConsultationRepository consultationRepo = Mockito.mock(ConsultationRepository.class);
        VitalSignsRepository vitalSignsRepo = Mockito.mock(VitalSignsRepository.class);
        PrescriptionRepository prescriptionRepo = Mockito.mock(PrescriptionRepository.class);
        LabOrderRepository labOrderRepo = Mockito.mock(LabOrderRepository.class);

        when(patientClient.getPatient(anyString())).thenReturn(new Object());
        when(consultationRepo.findByPatientIdOrderByConsultationDateDesc(anyString()))
                .thenReturn(Collections.emptyList());
        when(vitalSignsRepo.findByPatientIdOrderByRecordedAtDesc(anyString()))
                .thenReturn(Collections.emptyList());
        when(prescriptionRepo.findByPatientIdOrderByIssuedAtDesc(anyString()))
                .thenReturn(Collections.emptyList());
        when(labOrderRepo.findByPatientIdOrderByOrderedAtDesc(anyString()))
                .thenReturn(Collections.emptyList());

        return new MedicalHistoryAggregator(
                patientClient, consultationRepo, vitalSignsRepo,
                prescriptionRepo, labOrderRepo);
    }

    @Test
    void patientCanAccessOwnHistory() {
        MedicalHistoryAggregator aggregator = buildAggregator();

        // Same ID — should succeed
        GetMedicalHistoryUseCase.MedicalHistory history =
                aggregator.getMedicalHistory("patient-123", "patient-123", "PATIENT");

        Assertions.assertThat(history).isNotNull();
        Assertions.assertThat(history.consultations()).isEmpty();
    }

    @Test
    void patientCannotAccessOtherPatientHistory() {
        MedicalHistoryAggregator aggregator = buildAggregator();

        Assertions.assertThatThrownBy(() ->
                aggregator.getMedicalHistory("other-patient", "patient-123", "PATIENT"))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("propio historial");
    }

    @Test
    void doctorCanAccessAnyPatientHistory() {
        MedicalHistoryAggregator aggregator = buildAggregator();

        // Doctor accessing any patient — should succeed
        GetMedicalHistoryUseCase.MedicalHistory history =
                aggregator.getMedicalHistory("any-patient", "doctor-456", "DOCTOR");

        Assertions.assertThat(history).isNotNull();
    }

    @Test
    void adminCanAccessAnyPatientHistory() {
        MedicalHistoryAggregator aggregator = buildAggregator();

        GetMedicalHistoryUseCase.MedicalHistory history =
                aggregator.getMedicalHistory("any-patient", "admin-1", "ADMIN");

        Assertions.assertThat(history).isNotNull();
    }
}
