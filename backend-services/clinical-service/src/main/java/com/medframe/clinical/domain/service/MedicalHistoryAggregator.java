package com.medframe.clinical.domain.service;

import com.medframe.clinical.domain.exception.ForbiddenException;
import com.medframe.clinical.domain.model.Consultation;
import com.medframe.clinical.domain.model.LabOrder;
import com.medframe.clinical.domain.model.Prescription;
import com.medframe.clinical.domain.model.VitalSigns;
import com.medframe.clinical.domain.port.in.GetMedicalHistoryUseCase;
import com.medframe.clinical.domain.port.out.*;

import java.util.List;

public class MedicalHistoryAggregator {

    private final PatientServiceClient patientServiceClient;
    private final ConsultationRepository consultationRepository;
    private final VitalSignsRepository vitalSignsRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final LabOrderRepository labOrderRepository;

    public MedicalHistoryAggregator(PatientServiceClient patientServiceClient,
                                     ConsultationRepository consultationRepository,
                                     VitalSignsRepository vitalSignsRepository,
                                     PrescriptionRepository prescriptionRepository,
                                     LabOrderRepository labOrderRepository) {
        this.patientServiceClient = patientServiceClient;
        this.consultationRepository = consultationRepository;
        this.vitalSignsRepository = vitalSignsRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.labOrderRepository = labOrderRepository;
    }

    public GetMedicalHistoryUseCase.MedicalHistory getMedicalHistory(
            String patientId, String requestingUserId, String userRole) {

        // PATIENT role can only access their own history
        if ("PATIENT".equals(userRole) && !patientId.equals(requestingUserId)) {
            throw new ForbiddenException(
                    "Los pacientes solo pueden ver su propio historial clínico.");
        }

        // Get patient demographics (CERO JOINs — HTTP call)
        Object patient = patientServiceClient.getPatient(patientId);

        // Aggregate clinical data — all ordered by date DESC
        List<Consultation> consultations =
                consultationRepository.findByPatientIdOrderByConsultationDateDesc(patientId);

        List<VitalSigns> vitalSigns =
                vitalSignsRepository.findByPatientIdOrderByRecordedAtDesc(patientId);

        List<Prescription> prescriptions =
                prescriptionRepository.findByPatientIdOrderByIssuedAtDesc(patientId);

        List<LabOrder> labOrders =
                labOrderRepository.findByPatientIdOrderByOrderedAtDesc(patientId);

        return new GetMedicalHistoryUseCase.MedicalHistory(
                patient, consultations, vitalSigns, prescriptions, labOrders);
    }
}
