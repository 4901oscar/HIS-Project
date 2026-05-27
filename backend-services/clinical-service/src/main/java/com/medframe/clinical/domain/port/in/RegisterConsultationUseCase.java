package com.medframe.clinical.domain.port.in;

import com.medframe.clinical.domain.model.Consultation;

import java.util.List;

public interface RegisterConsultationUseCase {
    Consultation registerConsultation(String patientId, String doctorId,
                                      String appointmentId, String chiefComplaint,
                                      String symptoms, String primaryDiagnosis,
                                      List<String> secondaryDiagnoses,
                                      String medicalNotes, String treatmentPlan,
                                      boolean hasLabOrders, boolean hasPrescription);
}
