package com.medframe.clinical.domain.port.in;

import com.medframe.clinical.domain.model.Prescription;

import java.util.List;

public interface GeneratePrescriptionUseCase {
    Prescription generatePrescription(String consultationId, String patientId,
                                      String doctorId,
                                      List<Prescription.Medication> medications);
}
