package com.medframe.clinical.domain.port.in;

import com.medframe.clinical.domain.model.*;

import java.util.List;

public interface GetMedicalHistoryUseCase {

    record MedicalHistory(
        Object patient,
        List<Consultation> consultations,
        List<VitalSigns> vitalSigns,
        List<Prescription> prescriptions,
        List<LabOrder> labOrders
    ) {}

    MedicalHistory getMedicalHistory(String patientId, String requestingUserId, String userRole);
}
